package com.soonaemyoback.domain.member.admin.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.soonaemyoback.domain.member.admin.dto.AdminCreateRequest;
import com.soonaemyoback.domain.member.admin.dto.AdminLoginRequest;
import com.soonaemyoback.domain.member.admin.dto.AdminResponse;
import com.soonaemyoback.domain.member.admin.entity.Admin;
import com.soonaemyoback.domain.member.admin.entity.AdminRole;
import com.soonaemyoback.domain.member.admin.repository.AdminRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public void login(AdminLoginRequest request, HttpServletRequest httpRequest) {
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(request.loginId(), request.password());
        Authentication auth = authenticationManager.authenticate(token);

        SecurityContextHolder.getContext().setAuthentication(auth);
        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
    }

    @Transactional
    public AdminResponse createAdmin(AdminCreateRequest request) {
        if (adminRepository.existsByLoginId(request.loginId())) {
            throw new IllegalArgumentException("이미 사용 중인 loginId입니다: " + request.loginId());
        }
        Admin admin = Admin.create(
                request.loginId(), passwordEncoder.encode(request.password()), request.name(), AdminRole.ADMIN);
        return AdminResponse.from(adminRepository.save(admin));
    }

    @Transactional
    public void deleteAdmin(Long adminId) {
        Admin admin = adminRepository
                .findById(adminId)
                .orElseThrow(() -> new NoSuchElementException("Admin not found: " + adminId));
        if (admin.getRole() == AdminRole.ROOT) {
            throw new IllegalStateException("ROOT 관리자는 삭제할 수 없습니다.");
        }
        adminRepository.delete(admin);
    }

    public List<AdminResponse> listAdmins() {
        return adminRepository.findAll().stream().map(AdminResponse::from).toList();
    }
}
