package com.liaw.dev.GraoMestre.repository;

import com.liaw.dev.GraoMestre.entity.Order;
import com.liaw.dev.GraoMestre.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser_Id(Long userId);
    List<Order> findByUser_IdAndOrderStatus(Long userId, OrderStatus status);
    List<Order> findByOrderStatus(OrderStatus status);
    Page<Order> findAll(Pageable pageable);
    Page<Order> findByOrderStatusAndOrderDateBetweenAndUser_Id(
            OrderStatus status, LocalDateTime startDate, LocalDateTime endDate, Long userId, Pageable pageable);
    Page<Order> findByOrderStatusAndOrderDateBetween(OrderStatus status, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    Page<Order> findByOrderStatusAndUser_Id(OrderStatus status, Long userId, Pageable pageable);
    Page<Order> findByOrderDateBetweenAndUser_Id(LocalDateTime startDate, LocalDateTime endDate, Long userId, Pageable pageable);
    Page<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    Page<Order> findByUser_Id(Long userId, Pageable pageable);
    Page<Order> findByOrderStatus(OrderStatus status, Pageable pageable);
}