package com.liaw.dev.GraoMestre.controller;

import com.liaw.dev.GraoMestre.dto.request.OrderItemRequestDTO;
import com.liaw.dev.GraoMestre.dto.request.OrderRequestDTO;
import com.liaw.dev.GraoMestre.dto.response.OrderResponseDTO;
import com.liaw.dev.GraoMestre.enums.OrderStatus;
import com.liaw.dev.GraoMestre.enums.PaymentMethod;
import com.liaw.dev.GraoMestre.enums.TimePeriod;
import com.liaw.dev.GraoMestre.service.OrderService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('SCOPE_USER', 'SCOPE_ADMIN')")
    public ResponseEntity<OrderResponseDTO> createOrder(@Valid @RequestBody OrderRequestDTO orderRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(orderRequestDTO));
    }

    @PostMapping("/{orderId}/finalize-payment")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_USER')")
    public ResponseEntity<OrderResponseDTO> finalizeOrderPayment(
            @PathVariable Long orderId,
            @RequestParam PaymentMethod paymentMethod) {
        OrderResponseDTO finalizedOrder = orderService.finalizeOrderPayment(orderId, paymentMethod);
        return ResponseEntity.ok(finalizedOrder);
    }


    @PostMapping("/{orderId}/items")
    @PreAuthorize("hasAnyAuthority('SCOPE_USER', 'SCOPE_ADMIN')")
    public ResponseEntity<OrderResponseDTO> addItemToOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderItemRequestDTO itemRequestDTO) {
        return ResponseEntity.ok(orderService.addItemToOrder(orderId, itemRequestDTO));
    }

    @DeleteMapping("/{orderId}/items/{orderItemId}")
    @PreAuthorize("hasAnyAuthority('SCOPE_USER', 'SCOPE_ADMIN')")
    public ResponseEntity<OrderResponseDTO> removeItemFromOrder(
            @PathVariable Long orderId,
            @PathVariable Long orderItemId) {
        return ResponseEntity.ok(orderService.removeItemFromOrder(orderId, orderItemId));
    }

    @PutMapping("/{orderId}/items/{orderItemId}/quantity")
    @PreAuthorize("hasAnyAuthority('SCOPE_USER', 'SCOPE_ADMIN')")
    public ResponseEntity<OrderResponseDTO> updateOrderItemQuantity(
            @PathVariable Long orderId,
            @PathVariable Long orderItemId,
            @RequestParam @Positive(message = "Quantidade deve ser positiva") Integer quantity) {
        return ResponseEntity.ok(orderService.updateOrderItemQuantity(orderId, orderItemId, quantity));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyAuthority('SCOPE_USER', 'SCOPE_ADMIN')")
    public ResponseEntity<List<OrderResponseDTO>> getMyOrderHistory(Pageable pageable) {
        return ResponseEntity.ok(orderService.getMyOrderHistory(pageable));
    }

    @GetMapping("/my/{orderId}")
    @PreAuthorize("hasAnyAuthority('SCOPE_USER', 'SCOPE_ADMIN')")
    public ResponseEntity<OrderResponseDTO> getMyOrderDetails(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getMyOrderDetails(orderId));
    }

    @GetMapping("/my/status/{status}")
    @PreAuthorize("hasAnyAuthority('SCOPE_USER', 'SCOPE_ADMIN')")
    public ResponseEntity<List<OrderResponseDTO>> getMyOrdersByStatus(@PathVariable OrderStatus status) {
        return ResponseEntity.ok(orderService.getMyOrdersByStatus(status));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER')")
    public ResponseEntity<Page<OrderResponseDTO>> getAllOrders(Pageable pageable) {
        return ResponseEntity.ok(orderService.getAllOrders(pageable));
    }

    @GetMapping("/filter")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER', 'SCOPE_USER')")
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
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER', 'SCOPE_USER')")
    public ResponseEntity<OrderResponseDTO> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus newStatus) {
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, newStatus));
    }

    @GetMapping("/today")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_USER')")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersForToday() {
        List<OrderResponseDTO> orders = orderService.findOrdersForToday();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/yesterday")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_USER')")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersForYesterday() {
        List<OrderResponseDTO> orders = orderService.findOrdersForYesterday();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/this-week")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_USER')")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersForThisWeek() {
        List<OrderResponseDTO> orders = orderService.findOrdersForThisWeek();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/last-week")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_USER')")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersForLastWeek() {
        List<OrderResponseDTO> orders = orderService.findOrdersForLastWeek();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/this-month")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_USER')")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersForThisMonth() {
        List<OrderResponseDTO> orders = orderService.findOrdersForThisMonth();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/last-month")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_USER')")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersForLastMonth() {
        List<OrderResponseDTO> orders = orderService.findOrdersForLastMonth();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/custom")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_USER')")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersForCustomPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<OrderResponseDTO> orders = orderService.findOrdersForCustomPeriod(startDate, endDate);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/by-period")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_USER')")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByTimePeriod(
            @RequestParam TimePeriod period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<OrderResponseDTO> orders = orderService.findOrdersByTimePeriod(period, startDate, endDate);
        return ResponseEntity.ok(orders);
    }
}