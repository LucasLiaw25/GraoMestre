package com.liaw.dev.GraoMestre.repository;

import com.liaw.dev.GraoMestre.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
