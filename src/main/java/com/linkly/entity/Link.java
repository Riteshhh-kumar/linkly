package com.linkly.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "links")
@Data
public class Link {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String shortCode;

    @Column(nullable = false)
    private String longUrl;

    private LocalDateTime createdAt = LocalDateTime.now();

    // NEW: Link this to a User
    // Many links can belong to One user.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // This creates a 'user_id' column in the 'links' table
    private User user;
}