package com.linkly.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "app_users") // 'user' is often a reserved keyword in Postgres
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    // We'll use a simple role string for now (e.g., "ROLE_USER")
    private String role = "ROLE_USER";
}