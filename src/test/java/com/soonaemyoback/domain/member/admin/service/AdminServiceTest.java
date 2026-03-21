package com.soonaemyoback.domain.member.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.soonaemyoback.domain.member.admin.dto.AdminCreateRequest;
import com.soonaemyoback.domain.member.admin.dto.AdminResponse;
import com.soonaemyoback.domain.member.admin.entity.Admin;
import com.soonaemyoback.domain.member.admin.entity.AdminRole;
import com.soonaemyoback.domain.member.admin.repository.AdminRepository;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AdminService adminService;

    @Test
    @DisplayName("정상적인 ADMIN 생성")
    void createAdmin_success() {
        AdminCreateRequest request = new AdminCreateRequest("admin1", "pass123", "홍길동");
        given(adminRepository.existsByLoginId("admin1")).willReturn(false);
        given(passwordEncoder.encode("pass123")).willReturn("encoded");
        given(adminRepository.save(any(Admin.class))).willAnswer(inv -> inv.getArgument(0));

        AdminResponse response = adminService.createAdmin(request);

        assertThat(response.loginId()).isEqualTo("admin1");
        assertThat(response.name()).isEqualTo("홍길동");
        assertThat(response.role()).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("중복 loginId로 ADMIN 생성 시 예외 발생")
    void createAdmin_duplicateLoginId_throws() {
        AdminCreateRequest request = new AdminCreateRequest("admin1", "pass123", "홍길동");
        given(adminRepository.existsByLoginId("admin1")).willReturn(true);

        assertThatThrownBy(() -> adminService.createAdmin(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("admin1");
    }

    @Test
    @DisplayName("createAdmin은 항상 ADMIN role로 저장한다")
    void createAdmin_roleIsAlwaysAdmin() {
        AdminCreateRequest request = new AdminCreateRequest("admin1", "pass123", "홍길동");
        given(adminRepository.existsByLoginId("admin1")).willReturn(false);
        given(passwordEncoder.encode(any())).willReturn("encoded");
        given(adminRepository.save(any(Admin.class))).willAnswer(inv -> inv.getArgument(0));

        AdminResponse response = adminService.createAdmin(request);

        assertThat(response.role()).isEqualTo(AdminRole.ADMIN.name());
    }

    @Test
    @DisplayName("일반 ADMIN 삭제 성공")
    void deleteAdmin_success() {
        Admin admin = Admin.create("admin1", "encoded", "홍길동", AdminRole.ADMIN);
        given(adminRepository.findById(1L)).willReturn(Optional.of(admin));

        adminService.deleteAdmin(1L);

        verify(adminRepository).delete(admin);
    }

    @Test
    @DisplayName("ROOT 관리자 삭제 시도 시 예외 발생")
    void deleteAdmin_rootCannotBeDeleted() {
        Admin root = Admin.create("root", "encoded", "Root Admin", AdminRole.ROOT);
        given(adminRepository.findById(1L)).willReturn(Optional.of(root));

        assertThatThrownBy(() -> adminService.deleteAdmin(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ROOT");
    }

    @Test
    @DisplayName("존재하지 않는 ID로 삭제 시 예외 발생")
    void deleteAdmin_notFound_throws() {
        given(adminRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.deleteAdmin(999L)).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("전체 관리자 목록 반환")
    void listAdmins_returnsAll() {
        Admin root = Admin.create("root", "encoded", "Root Admin", AdminRole.ROOT);
        Admin admin = Admin.create("admin1", "encoded", "홍길동", AdminRole.ADMIN);
        given(adminRepository.findAll()).willReturn(List.of(root, admin));

        List<AdminResponse> result = adminService.listAdmins();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(AdminResponse::loginId).containsExactly("root", "admin1");
    }
}
