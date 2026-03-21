package com.soonaemyoback.domain.member.admin.dto;

import java.time.LocalDateTime;

import com.soonaemyoback.domain.member.admin.entity.Admin;

public record AdminResponse(Long id, String loginId, String name, String role, LocalDateTime createdAt) {

    public static AdminResponse from(Admin admin) {
        return new AdminResponse(
                admin.getId(),
                admin.getLoginId(),
                admin.getName(),
                admin.getRole().name(),
                admin.getCreatedAt());
    }
}
