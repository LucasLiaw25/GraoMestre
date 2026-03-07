package com.liaw.dev.GraoMestre.mapper;

import com.liaw.dev.GraoMestre.dto.request.AddressRequestDTO;
import com.liaw.dev.GraoMestre.dto.response.AddressResponseDTO;
import com.liaw.dev.GraoMestre.entity.Address;
import com.liaw.dev.GraoMestre.entity.User; // Importar User para o stub

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AddressMapper {

    public static Address toEntity(AddressRequestDTO dto) {
        Address address = new Address();
        address.setStreet(dto.getStreet());
        address.setNumber(dto.getNumber());
        address.setComplement(dto.getComplement());
        address.setState(dto.getState());
        address.setCity(dto.getCity());
        address.setCep(dto.getCep());
        if (dto.getIsDefault() != null) {
            address.setIsDefault(dto.getIsDefault());
        }

        return address;
    }

    public static AddressResponseDTO toResponseDTO(Address address) {
        AddressResponseDTO dto = new AddressResponseDTO();
        dto.setId(address.getId());
        dto.setStreet(address.getStreet());
        dto.setNumber(address.getNumber());
        dto.setComplement(address.getComplement());
        dto.setState(address.getState());
        dto.setCity(address.getCity());
        dto.setCep(address.getCep());
        dto.setIsDefault(address.getIsDefault());
        if (address.getUser() != null) {
            dto.setUserId(address.getUser().getId());
        }
        return dto;
    }

    public static List<AddressResponseDTO> toResponseDTOList(List<Address> addresses) {
        if (addresses == null || addresses.isEmpty()) {
            return Collections.emptyList();
        }
        return addresses.stream()
                .map(AddressMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public static void updateEntityFromDto(AddressRequestDTO dto, Address address) {
        address.setStreet(dto.getStreet());
        address.setNumber(dto.getNumber());
        address.setComplement(dto.getComplement());
        address.setState(dto.getState());
        address.setCity(dto.getCity());
        address.setCep(dto.getCep());
        if (dto.getIsDefault() != null) {
            address.setIsDefault(dto.getIsDefault());
        }

    }
}