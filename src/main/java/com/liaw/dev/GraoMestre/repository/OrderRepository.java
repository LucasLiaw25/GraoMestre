package com.liaw.dev.GraoMestre.repository;

import com.liaw.dev.GraoMestre.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
