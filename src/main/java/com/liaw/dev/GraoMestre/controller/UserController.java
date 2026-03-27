package com.liaw.dev.GraoMestre.controller;

import com.liaw.dev.GraoMestre.dto.request.UserLoginRequestDTO;
import com.liaw.dev.GraoMestre.dto.request.UserRegisterRequestDTO;
import com.liaw.dev.GraoMestre.dto.request.UserRequestDTO;
import com.liaw.dev.GraoMestre.dto.response.AuthResponseDTO;
import com.liaw.dev.GraoMestre.dto.response.UserResponseDTO;
import com.liaw.dev.GraoMestre.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> registerUser(@Valid @RequestBody UserRegisterRequestDTO userRegisterRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.registerUser(userRegisterRequestDTO));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> loginUser(@Valid @RequestBody UserLoginRequestDTO userLoginRequestDTO) {
        return ResponseEntity.ok(userService.loginUser(userLoginRequestDTO));
    }

    @GetMapping("/activate")
    public ResponseEntity<String> activateUser(@RequestParam String token) {
        userService.activateUser(token);
        return ResponseEntity.ok(
                "Your account have been successfully activated"
        );
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER')")
    public ResponseEntity<List<UserResponseDTO>> findAllUsers() {
        return ResponseEntity.ok(userService.findAllUsers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER', 'SCOPE_USER')")
    public ResponseEntity<UserResponseDTO> findUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findUserById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER', 'SCOPE_USER')")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Long id, @Valid @RequestBody UserRequestDTO userRequestDTO) {
        return ResponseEntity.ok(userService.updateUser(id, userRequestDTO));
    }

    @PostMapping("/change-password/{email}")
    public void changePasswordEmail(@PathVariable String email){
        userService.changePassword(email);
    }

    @PutMapping("/password")
    public ResponseEntity<UserResponseDTO> updateUserPassword(@RequestParam String token, @RequestBody Map<String, String> passwordMap) {
        String newPassword = passwordMap.get("newPassword");
        return ResponseEntity.ok(userService.updateUserPassword(token, newPassword));
    }

    @PutMapping("/{id}/scopes")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<UserResponseDTO> updateUserScopes(@PathVariable Long id, @RequestBody List<Long> scopeIds) {
        return ResponseEntity.ok(userService.updateUserScopes(id, scopeIds));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}