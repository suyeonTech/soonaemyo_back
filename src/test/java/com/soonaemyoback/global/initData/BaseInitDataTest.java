package com.soonaemyoback.global.initData;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.soonaemyoback.domain.member.admin.entity.Admin;
import com.soonaemyoback.domain.member.admin.entity.AdminRole;
import com.soonaemyoback.domain.member.admin.repository.AdminRepository;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BaseInitDataTest {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private BaseInitData baseInitData;

    @Value("${init.root-admin.login-id}")
    private String rootLoginId;

    @Value("${init.root-admin.password}")
    private String rootPassword;

    @Test
    @DisplayName("앱 시작 시 ROOT 관리자가 자동 생성된다")
    void rootAdminIsSeeded() {
        Admin root = adminRepository.findByLoginId(rootLoginId).orElseThrow();

        assertThat(root.getRole()).isEqualTo(AdminRole.ROOT);
        assertThat(passwordEncoder.matches(rootPassword, root.getPassword())).isTrue();
        assertThat(root.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("BaseInitData를 여러 번 실행해도 ROOT 관리자는 1개만 존재한다 (idempotent)")
    void seedingIsIdempotent() throws Exception {
        baseInitData.run(null);
        baseInitData.run(null);

        long rootCount = adminRepository.findAll().stream()
                .filter(a -> a.getRole() == AdminRole.ROOT)
                .count();

        assertThat(rootCount).isEqualTo(1);
    }
}
