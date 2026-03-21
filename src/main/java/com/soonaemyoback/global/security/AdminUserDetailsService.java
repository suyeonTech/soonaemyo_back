package com.soonaemyoback.global.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.soonaemyoback.domain.member.admin.repository.AdminRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminUserDetailsService implements UserDetailsService {

    private final AdminRepository adminRepository;

    @Override
    public UserDetails loadUserByUsername(String loginId) {
        return adminRepository
                .findByLoginId(loginId)
                .map(AdminUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("Admin not found: " + loginId));
    }
}
