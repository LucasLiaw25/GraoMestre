package com.liaw.dev.GraoMestre.service;

import com.liaw.dev.GraoMestre.dto.request.AddressRequestDTO;
import com.liaw.dev.GraoMestre.dto.response.AddressResponseDTO;
import com.liaw.dev.GraoMestre.entity.Address;
import com.liaw.dev.GraoMestre.entity.User;
import com.liaw.dev.GraoMestre.exception.exceptions.EntityNotFoundException;
import com.liaw.dev.GraoMestre.mapper.AddressMapper;
import com.liaw.dev.GraoMestre.repository.AddressRepository;
import com.liaw.dev.GraoMestre.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressService {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<AddressResponseDTO> findAllAddresses() {
        List<Address> addresses = addressRepository.findAll();
        return AddressMapper.toResponseDTOList(addresses);
    }

    @Transactional(readOnly = true)
    public AddressResponseDTO findAddressById(Long id) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Endereço não encontrado com ID: " + id));
        return AddressMapper.toResponseDTO(address);
    }

    @Transactional(readOnly = true)
    public List<AddressResponseDTO> findAddressesByUserId(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com ID: " + userId));
        List<Address> addresses = addressRepository.findByUser_Id(userId);
        return AddressMapper.toResponseDTOList(addresses);
    }

    @Transactional
    public AddressResponseDTO createAddress(AddressRequestDTO addressRequestDTO) {
        Address address = AddressMapper.toEntity(addressRequestDTO);

        User user = userRepository.findById(addressRequestDTO.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com ID: " + addressRequestDTO.getUserId()));
        address.setUser(user);

        address = addressRepository.save(address);
        return AddressMapper.toResponseDTO(address);
    }

    @Transactional
    public AddressResponseDTO updateAddress(Long id, AddressRequestDTO addressRequestDTO) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Endereço não encontrado com ID: " + id));

        AddressMapper.updateEntityFromDto(addressRequestDTO, address);

        if (!address.getUser().getId().equals(addressRequestDTO.getUserId())) {
            User newUser = userRepository.findById(addressRequestDTO.getUserId())
                    .orElseThrow(() -> new EntityNotFoundException("Novo usuário não encontrado com ID: " + addressRequestDTO.getUserId()));
            address.setUser(newUser);
        }

        address = addressRepository.save(address);
        return AddressMapper.toResponseDTO(address);
    }

    @Transactional
    public void deleteAddress(Long id) {
        if (!addressRepository.existsById(id)) {
            throw new EntityNotFoundException("Endereço não encontrado com ID: " + id);
        }
        addressRepository.deleteById(id);
    }
}