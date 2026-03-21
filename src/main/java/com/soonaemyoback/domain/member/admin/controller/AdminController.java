package com.soonaemyoback.domain.member.admin.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.soonaemyoback.domain.member.admin.dto.AdminCreateRequest;
import com.soonaemyoback.domain.member.admin.dto.AdminLoginRequest;
import com.soonaemyoback.domain.member.admin.dto.AdminResponse;
import com.soonaemyoback.domain.member.admin.service.AdminService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/admin/login")
    public ResponseEntity<Void> login(@RequestBody AdminLoginRequest request, HttpServletRequest httpRequest) {
        adminService.login(request, httpRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/admins")
    public ResponseEntity<AdminResponse> createAdmin(@RequestBody AdminCreateRequest request) {
        return ResponseEntity.status(201).body(adminService.createAdmin(request));
    }

    @DeleteMapping("/admins/{id}")
    public ResponseEntity<Void> deleteAdmin(@PathVariable Long id) {
        adminService.deleteAdmin(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admins")
    public ResponseEntity<List<AdminResponse>> listAdmins() {
        return ResponseEntity.ok(adminService.listAdmins());
    }
}
