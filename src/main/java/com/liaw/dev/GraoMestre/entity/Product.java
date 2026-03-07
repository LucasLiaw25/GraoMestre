package com.liaw.dev.GraoMestre.entity;

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
@Table(name = "tb_product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer storage;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "register_date")
    private LocalDateTime registerDate;

    private BigDecimal price;
    private Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    private Category category;

    @PrePersist
    protected void onCreate(){
        registerDate = LocalDateTime.now();
    }

}
