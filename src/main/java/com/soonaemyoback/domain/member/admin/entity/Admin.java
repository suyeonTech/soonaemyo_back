package com.soonaemyoback.domain.member.admin.entity;

import static lombok.AccessLevel.PROTECTED;

import com.soonaemyoback.global.jpa.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Admin extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String loginId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdminRole role;

    public static Admin create(String loginId, String encodedPassword, String name, AdminRole role) {
        Admin admin = new Admin();
        admin.loginId = loginId;
        admin.password = encodedPassword;
        admin.name = name;
        admin.role = role;
        return admin;
    }
}
