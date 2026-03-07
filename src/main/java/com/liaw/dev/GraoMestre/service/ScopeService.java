package com.liaw.dev.GraoMestre.service;

import com.liaw.dev.GraoMestre.dto.request.ScopeRequestDTO;
import com.liaw.dev.GraoMestre.dto.response.ScopeResponseDTO;
import com.liaw.dev.GraoMestre.entity.Scope;
import com.liaw.dev.GraoMestre.exception.exceptions.EntityNotFoundException;
import com.liaw.dev.GraoMestre.mapper.ScopeMapper;
import com.liaw.dev.GraoMestre.repository.ScopeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ScopeService {

    @Autowired
    private ScopeRepository scopeRepository;

    @Transactional(readOnly = true)
    public List<ScopeResponseDTO> findAllScopes() {
        List<Scope> scopes = scopeRepository.findAll();
        return ScopeMapper.toResponseDTOList(scopes);
    }

    @Transactional(readOnly = true)
    public ScopeResponseDTO findScopeById(Long id) {
        Scope scope = scopeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Escopo não encontrado com ID: " + id));
        return ScopeMapper.toResponseDTO(scope);
    }

    @Transactional
    public ScopeResponseDTO createScope(ScopeRequestDTO scopeRequestDTO) {
        Scope scope = ScopeMapper.toEntity(scopeRequestDTO);
        scope = scopeRepository.save(scope);
        return ScopeMapper.toResponseDTO(scope);
    }

    @Transactional
    public ScopeResponseDTO updateScope(Long id, ScopeRequestDTO scopeRequestDTO) {
        Scope scope = scopeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Escopo não encontrado com ID: " + id));

        ScopeMapper.updateEntityFromDto(scopeRequestDTO, scope);
        scope = scopeRepository.save(scope);
        return ScopeMapper.toResponseDTO(scope);
    }

    @Transactional
    public void deleteScope(Long id) {
        if (!scopeRepository.existsById(id)) {
            throw new EntityNotFoundException("Escopo não encontrado com ID: " + id);
        }

        scopeRepository.deleteById(id);
    }
}