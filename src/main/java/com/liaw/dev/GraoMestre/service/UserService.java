package com.liaw.dev.GraoMestre.service;

import com.liaw.dev.GraoMestre.dto.request.UserLoginRequestDTO;
import com.liaw.dev.GraoMestre.dto.request.UserRegisterRequestDTO;
import com.liaw.dev.GraoMestre.dto.request.UserRequestDTO;
import com.liaw.dev.GraoMestre.dto.response.AuthResponseDTO;
import com.liaw.dev.GraoMestre.dto.response.UserResponseDTO;
import com.liaw.dev.GraoMestre.entity.Scope;
import com.liaw.dev.GraoMestre.entity.User;
import com.liaw.dev.GraoMestre.entity.VerificationToken;
import com.liaw.dev.GraoMestre.exception.exceptions.BadCredentialsException;
import com.liaw.dev.GraoMestre.exception.exceptions.ConflitException;
import com.liaw.dev.GraoMestre.exception.exceptions.EntityNotFoundException;
import com.liaw.dev.GraoMestre.mapper.UserMapper;
import com.liaw.dev.GraoMestre.repository.ScopeRepository;
import com.liaw.dev.GraoMestre.repository.UserRepository;
import com.liaw.dev.GraoMestre.repository.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ScopeRepository scopeRepository;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Value("${app.base-url}")
    private String appBaseUrl;

    @Transactional
    public UserResponseDTO registerUser(UserRegisterRequestDTO userRegisterRequestDTO) {
        if (userRepository.findByEmail(userRegisterRequestDTO.getEmail()).isPresent()) {
            throw new ConflitException("Email já cadastrado.");
        }

        User user = UserMapper.toEntity(userRegisterRequestDTO);

        user.setPassword(passwordEncoder.encode(userRegisterRequestDTO.getPassword()));

        Scope defaultScope = scopeRepository.findByName("USER")
                .orElseGet(() -> {
                    Scope newScope = new Scope(null, "USER", "Usuário padrão do sistema");
                    return scopeRepository.save(newScope);
                });
        user.setScopes(Collections.singletonList(defaultScope));
        user = userRepository.save(user);

        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(24);
        VerificationToken verificationToken = new VerificationToken(token, user, expiryDate);
        verificationTokenRepository.save(verificationToken);

        String activationLink = "http://" + appBaseUrl + "/api/users/activate?token=" + token;
        emailService.sendActivationEmail(user.getEmail(), activationLink);
        return UserMapper.toResponseDTO(user);
    }

    @Transactional(readOnly = true)
    public AuthResponseDTO loginUser(UserLoginRequestDTO userLoginRequestDTO) {
        User user = userRepository.findByEmail(userLoginRequestDTO.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Email ou senha inválidos."));

        if (!user.getActive()) {
            throw new BadCredentialsException("Usuário inativo. Por favor, ative sua conta através do e-mail.");
        }

        if (!passwordEncoder.matches(userLoginRequestDTO.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Email ou senha inválidos.");
        }

        String token = generateJwtToken(user);

        return new AuthResponseDTO(token, UserMapper.toResponseDTO(user));
    }

    @Transactional
    public void activateUser(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token de ativação inválido."));

        if (verificationToken.isExpired()) {
            verificationTokenRepository.delete(verificationToken);
            throw new BadCredentialsException("Token de ativação expirado. Por favor, solicite um novo.");
        }

        User user = verificationToken.getUser();

        if (user.getActive()) {
            verificationTokenRepository.delete(verificationToken);
            throw new IllegalStateException("Usuário já está ativo.");
        }

        user.setActive(true);
        user = userRepository.save(user);

        verificationTokenRepository.delete(verificationToken);

    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> findAllUsers() {
        List<User> users = userRepository.findAll();
        return UserMapper.toResponseDTOList(users);
    }

    @Transactional(readOnly = true)
    public UserResponseDTO findUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com ID: " + id));
        return UserMapper.toResponseDTO(user);
    }

    @Transactional
    public UserResponseDTO updateUser(Long id, UserRequestDTO userRequestDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com ID: " + id));

        if (!user.getEmail().equals(userRequestDTO.getEmail()) &&
                userRepository.findByEmail(userRequestDTO.getEmail()).isPresent()) {
            throw new BadCredentialsException("Novo email já cadastrado por outro usuário.");
        }

        UserMapper.updateEntityFromDto(userRequestDTO, user);

        if (userRequestDTO.getScopeIds() != null) {
            List<Scope> newScopes = scopeRepository.findAllById(userRequestDTO.getScopeIds());
            user.setScopes(newScopes);
        }

        user = userRepository.save(user);
        return UserMapper.toResponseDTO(user);
    }

    @Transactional
    public UserResponseDTO updateUserPassword(Long id, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com ID: " + id));

        if (newPassword == null || newPassword.trim().isEmpty() || newPassword.length() < 6) {
            throw new BadCredentialsException("A nova senha deve ter no mínimo 6 caracteres.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user = userRepository.save(user);
        return UserMapper.toResponseDTO(user);
    }

    @Transactional
    public UserResponseDTO updateUserScopes(Long userId, List<Long> scopeIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com ID: " + userId));

        List<Scope> newScopes = scopeRepository.findAllById(scopeIds);
        if (newScopes.size() != scopeIds.size()) {
            throw new EntityNotFoundException("Um ou mais escopos fornecidos não foram encontrados.");
        }
        user.setScopes(newScopes);
        user = userRepository.save(user);
        return UserMapper.toResponseDTO(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com ID: " + id));
        // Opcional: Remover tokens de verificação associados antes de deletar o usuário
        verificationTokenRepository.deleteByUser(user);
        userRepository.delete(user);
    }

    private String generateJwtToken(User user) {
        Instant now = Instant.now();
        long expiry = 36000L; // 10 horas de validade para o token

        String scopes = user.getScopes().stream()
                .map(Scope::getName)
                .collect(Collectors.joining(" "));

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiry))
                .subject(user.getEmail()) // O subject do token é o email do usuário
                .claim("id", user.getId()) // Adiciona o ID do usuário como claim
                .claim("scope", scopes) // Adiciona os escopos como uma claim
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}