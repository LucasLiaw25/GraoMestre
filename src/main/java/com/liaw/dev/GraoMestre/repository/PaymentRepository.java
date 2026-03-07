package com.liaw.dev.GraoMestre.repository;

import com.liaw.dev.GraoMestre.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
