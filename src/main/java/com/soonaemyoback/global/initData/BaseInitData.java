package com.soonaemyoback.global.initData;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.soonaemyoback.domain.member.admin.entity.Admin;
import com.soonaemyoback.domain.member.admin.entity.AdminRole;
import com.soonaemyoback.domain.member.admin.repository.AdminRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BaseInitData implements ApplicationRunner {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${init.root-admin.login-id}")
    private String rootLoginId;

    @Value("${init.root-admin.password}")
    private String rootPassword;

    @Value("${init.root-admin.name}")
    private String rootName;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedRootAdmin();
    }

    private void seedRootAdmin() {
        if (adminRepository.existsByLoginId(rootLoginId)) {
            return;
        }
        Admin root = Admin.create(rootLoginId, passwordEncoder.encode(rootPassword), rootName, AdminRole.ROOT);
        adminRepository.save(root);
    }
}
