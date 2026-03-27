package com.soonaemyoback.domain.member.admin.dto;

public record ChangePasswordRequest(String currentPassword, String newPassword, String confirmPassword) {}
