package com.liaw.dev.GraoMestre.entity;

import com.liaw.dev.GraoMestre.enums.PaymentMethod;
import com.liaw.dev.GraoMestre.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tb_payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "order_id")
    @OneToOne(fetch = FetchType.LAZY)
    private Order order;

    private LocalDateTime dateCreated;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    private String txId;

    private BigDecimal totalPrice;

    @Column(name = "mp_payment_id")
    private String mpPaymentId;

    @Column(name = "mp_preference_id")
    private String mpPreferenceId;

    @Column(name = "qr_code_base64", columnDefinition = "TEXT")
    private String qrCodeBase64;

    @Column(name = "qr_code_text")
    private String qrCodeText;

    @Column(name = "payment_url")
    private String paymentUrl;

    @Column(name = "date_of_expiration")
    private LocalDateTime dateOfExpiration;

    @Column(name = "date_approved")
    private LocalDateTime dateApproved;

    @PrePersist
    protected void onCreate(){
        if(dateCreated == null){
            dateCreated = LocalDateTime.now();
        }
    }

}
