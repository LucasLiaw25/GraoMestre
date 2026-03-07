package com.liaw.dev.GraoMestre.mapper;


import com.liaw.dev.GraoMestre.dto.request.ScopeRequestDTO;
import com.liaw.dev.GraoMestre.dto.response.ScopeResponseDTO;
import com.liaw.dev.GraoMestre.entity.Scope;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ScopeMapper {

    public static Scope toEntity(ScopeRequestDTO dto) {
        Scope scope = new Scope();
        scope.setName(dto.getName());
        scope.setDescription(dto.getDescription());
        return scope;
    }

    public static ScopeResponseDTO toResponseDTO(Scope scope) {
        ScopeResponseDTO dto = new ScopeResponseDTO();
        dto.setId(scope.getId());
        dto.setName(scope.getName());
        dto.setDescription(scope.getDescription());
        return dto;
    }

    public static List<ScopeResponseDTO> toResponseDTOList(List<Scope> scopes) {
        if (scopes == null || scopes.isEmpty()) {
            return Collections.emptyList();
        }
        return scopes.stream()
                .map(ScopeMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public static void updateEntityFromDto(ScopeRequestDTO dto, Scope scope) {
        scope.setName(dto.getName());
        scope.setDescription(dto.getDescription());
    }
}