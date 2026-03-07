package com.liaw.dev.GraoMestre.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Fetch;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tb_address")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String street;
    private String number;
    private String complement;
    private String state;
    private String city;
    private String cep;
    private Boolean isDefault;

    @ManyToOne(fetch = FetchType.LAZY)
    public User user;

}
