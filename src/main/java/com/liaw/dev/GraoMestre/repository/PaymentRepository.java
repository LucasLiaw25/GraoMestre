package com.liaw.dev.GraoMestre.repository;

import com.liaw.dev.GraoMestre.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByMpPaymentId(String mpPaymentId);
    Optional<Payment> findByMpPreferenceId(String mpPreferenceId);
}
