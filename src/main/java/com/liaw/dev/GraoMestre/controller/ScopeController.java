package com.liaw.dev.GraoMestre.controller;

import com.liaw.dev.GraoMestre.dto.request.ScopeRequestDTO;
import com.liaw.dev.GraoMestre.dto.response.ScopeResponseDTO;
import com.liaw.dev.GraoMestre.service.ScopeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/scopes")
@RequiredArgsConstructor
public class ScopeController {

    private final ScopeService scopeService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER')")
    public ResponseEntity<List<ScopeResponseDTO>> findAllScopes() {
        return ResponseEntity.ok(scopeService.findAllScopes());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER')")
    public ResponseEntity<ScopeResponseDTO> findScopeById(@PathVariable Long id) {
        return ResponseEntity.ok(scopeService.findScopeById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<ScopeResponseDTO> createScope(@Valid @RequestBody ScopeRequestDTO scopeRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(scopeService.createScope(scopeRequestDTO));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<ScopeResponseDTO> updateScope(@PathVariable Long id, @Valid @RequestBody ScopeRequestDTO scopeRequestDTO) {
        return ResponseEntity.ok(scopeService.updateScope(id, scopeRequestDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<Void> deleteScope(@PathVariable Long id) {
        scopeService.deleteScope(id);
        return ResponseEntity.noContent().build();
    }
}