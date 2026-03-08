package com.liaw.dev.GraoMestre.controller;

import com.liaw.dev.GraoMestre.dto.request.OrderRequestDTO;
import com.liaw.dev.GraoMestre.dto.response.OrderResponseDTO;
import com.liaw.dev.GraoMestre.enums.OrderStatus;
import com.liaw.dev.GraoMestre.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('SCOPE_USER')") // Users/Drivers can create orders
    public ResponseEntity<OrderResponseDTO> createOrder(@Valid @RequestBody OrderRequestDTO orderRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(orderRequestDTO));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyAuthority('SCOPE_USER')") // Users/Drivers can view their own orders
    public ResponseEntity<List<OrderResponseDTO>> getMyOrderHistory() {
        return ResponseEntity.ok(orderService.getMyOrderHistory());
    }

    @GetMapping("/my/{orderId}")
    @PreAuthorize("hasAnyAuthority('SCOPE_USER')") // Users/Drivers can view details of their own order
    public ResponseEntity<OrderResponseDTO> getMyOrderDetails(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getMyOrderDetails(orderId));
    }

    @GetMapping("/my/status/{status}")
    @PreAuthorize("hasAnyAuthority('SCOPE_USER')") // Users/Drivers can filter their own orders by status
    public ResponseEntity<List<OrderResponseDTO>> getMyOrdersByStatus(@PathVariable OrderStatus status) {
        return ResponseEntity.ok(orderService.getMyOrdersByStatus(status));
    }

    // Admin/Manager endpoints
    @GetMapping
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER')")
    public ResponseEntity<Page<OrderResponseDTO>> getAllOrders(Pageable pageable) {
        return ResponseEntity.ok(orderService.getAllOrders(pageable));
    }

    @GetMapping("/filter")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER')")
    public ResponseEntity<Page<OrderResponseDTO>> filterOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) Long userId,
            Pageable pageable) {
        return ResponseEntity.ok(orderService.filterOrders(status, startDate, endDate, userId, pageable));
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER')")
    public ResponseEntity<OrderResponseDTO> getOrderDetailsForAdmin(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrderDetailsForAdmin(orderId));
    }

    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER')")
    public ResponseEntity<OrderResponseDTO> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus newStatus) {
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, newStatus));
    }
}