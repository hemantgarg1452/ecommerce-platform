package com.company.ecommerce.auth.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
@Builder
@Data
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, length = 500)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiryDate;
    private boolean revoked = false;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
