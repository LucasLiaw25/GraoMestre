package com.liaw.dev.GraoMestre.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tb_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;
    private String password;
    private String name;
    private String phone;

    @Column(unique = true)
    private String cpf;

    @Column(name = "register_date")
    private LocalDateTime registerDate;

    private Boolean active = false;

    @ManyToMany
    @JoinTable(
            name = "user_scope",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "scope_id")
    )
    private List<Scope> scopes;

    @PrePersist
    protected void onCreate(){
        registerDate = LocalDateTime.now();
    }
}
