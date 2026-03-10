package com.liaw.dev.GraoMestre.controller;

import com.liaw.dev.GraoMestre.dto.request.AddressRequestDTO;
import com.liaw.dev.GraoMestre.dto.response.AddressResponseDTO;
import com.liaw.dev.GraoMestre.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER')")
    public ResponseEntity<List<AddressResponseDTO>> findAllAddresses() {
        return ResponseEntity.ok(addressService.findAllAddresses());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER', 'SCOPE_USER')")
    public ResponseEntity<AddressResponseDTO> findAddressById(@PathVariable Long id) {
        return ResponseEntity.ok(addressService.findAddressById(id));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER', 'SCOPE_USER')")
    public ResponseEntity<List<AddressResponseDTO>> findAddressesByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(addressService.findAddressesByUserId(userId));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER', 'SCOPE_USER')") // User can create their own address
    public ResponseEntity<AddressResponseDTO> createAddress(@Valid @RequestBody AddressRequestDTO addressRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(addressService.createAddress(addressRequestDTO));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER', 'SCOPE_USER')") // User can update their own address
    public ResponseEntity<AddressResponseDTO> updateAddress(@PathVariable Long id, @Valid @RequestBody AddressRequestDTO addressRequestDTO) {
        return ResponseEntity.ok(addressService.updateAddress(id, addressRequestDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER', 'SCOPE_USER')") // User can delete their own address
    public ResponseEntity<Void> deleteAddress(@PathVariable Long id) {
        addressService.deleteAddress(id);
        return ResponseEntity.noContent().build();
    }
}