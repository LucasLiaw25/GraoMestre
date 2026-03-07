package com.liaw.dev.GraoMestre.mapper;


import com.liaw.dev.GraoMestre.dto.request.UserRegisterRequestDTO;
import com.liaw.dev.GraoMestre.dto.request.UserRequestDTO;
import com.liaw.dev.GraoMestre.dto.response.UserResponseDTO;
import com.liaw.dev.GraoMestre.entity.User;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class UserMapper {

    public static User toEntity(UserRegisterRequestDTO dto) {
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setName(dto.getName());
        user.setPhone(dto.getPhone());
        user.setPassword(dto.getPassword());
        user.setActive(false);
        return user;
    }

    public static UserResponseDTO toResponseDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setName(user.getName());
        dto.setPhone(user.getPhone());
        dto.setRegisterDate(user.getRegisterDate());
        dto.setActive(user.getActive());
        if (user.getScopes() != null && !user.getScopes().isEmpty()) {
            dto.setScopes(user.getScopes().stream()
                    .map(ScopeMapper::toResponseDTO)
                    .collect(Collectors.toList()));
        } else {
            dto.setScopes(Collections.emptyList());
        }
        return dto;
    }

    public static List<UserResponseDTO> toResponseDTOList(List<User> users) {
        if (users == null || users.isEmpty()) {
            return Collections.emptyList();
        }
        return users.stream()
                .map(UserMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public static void updateEntityFromDto(UserRequestDTO dto, User user) {
        user.setEmail(dto.getEmail());
        user.setName(dto.getName());
        user.setPhone(dto.getPhone());
        user.setPassword(dto.getPassword());
        if (dto.getActive() != null) {
            user.setActive(dto.getActive());
        }

    }
}