package com.soonaemyoback.domain.member.admin.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.soonaemyoback.domain.member.admin.dto.AdminCreateRequest;
import com.soonaemyoback.domain.member.admin.dto.AdminLoginRequest;
import com.soonaemyoback.domain.member.admin.dto.ChangePasswordRequest;
import com.soonaemyoback.domain.member.admin.entity.AdminRole;
import com.soonaemyoback.domain.member.admin.repository.AdminRepository;

import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AdminRepository adminRepository;

    @Value("${init.root-admin.login-id}")
    private String rootLoginId;

    @Value("${init.root-admin.password}")
    private String rootPassword;

    private MockHttpSession rootSession;

    @BeforeEach
    void loginAsRoot() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AdminLoginRequest(rootLoginId, rootPassword))))
                .andExpect(status().isOk())
                .andReturn();

        rootSession = (MockHttpSession) result.getRequest().getSession();
    }

    @Test
    @DisplayName("ROOT 로그인 성공 시 세션이 생성되고 role이 반환된다")
    void login_success_createsSessionAndReturnsRole() throws Exception {
        assertThat(rootSession).isNotNull();
        assertThat(rootSession.getAttribute("SPRING_SECURITY_CONTEXT")).isNotNull();

        mockMvc.perform(post("/api/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AdminLoginRequest(rootLoginId, rootPassword))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ROOT"));
    }

    @Test
    @DisplayName("ADMIN 로그인 성공 시 role=ADMIN 반환")
    void login_admin_returnsAdminRole() throws Exception {
        AdminCreateRequest createRequest = new AdminCreateRequest("role_check_admin", "pass1234", "역할확인관리자");
        mockMvc.perform(post("/api/admins")
                .session(rootSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)));

        mockMvc.perform(post("/api/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(new AdminLoginRequest("role_check_admin", "pass1234"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인 시 401 반환")
    void login_wrongPassword_returns401() throws Exception {
        mockMvc.perform(post("/api/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AdminLoginRequest(rootLoginId, "wrongpassword"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("인증 없이 /api/admins 접근 시 401 반환")
    void listAdmins_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/admins")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("ROOT 권한으로 ADMIN 생성 성공")
    void createAdmin_asRoot_returns201() throws Exception {
        AdminCreateRequest request = new AdminCreateRequest("newadmin", "pass1234", "새관리자");

        mockMvc.perform(post("/api/admins")
                        .session(rootSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.loginId").value("newadmin"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @DisplayName("ADMIN 권한으로 /api/admins 접근 시 403 반환")
    void listAdmins_asAdmin_returns403() throws Exception {
        // ROOT가 ADMIN 계정 생성
        AdminCreateRequest createRequest = new AdminCreateRequest("plain_admin", "pass1234", "일반관리자");
        mockMvc.perform(post("/api/admins")
                .session(rootSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)));

        // ADMIN으로 로그인
        MvcResult adminLoginResult = mockMvc.perform(post("/api/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AdminLoginRequest("plain_admin", "pass1234"))))
                .andReturn();
        MockHttpSession adminSession =
                (MockHttpSession) adminLoginResult.getRequest().getSession();

        // ADMIN으로 목록 조회 시도 → 403
        mockMvc.perform(get("/api/admins").session(adminSession)).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ROOT 권한으로 ADMIN 삭제 성공")
    void deleteAdmin_asRoot_returns204() throws Exception {
        // ADMIN 생성
        AdminCreateRequest createRequest = new AdminCreateRequest("to_delete", "pass1234", "삭제대상");
        MvcResult createResult = mockMvc.perform(post("/api/admins")
                        .session(rootSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn();

        Long adminId = objectMapper
                .readTree(createResult.getResponse().getContentAsString())
                .get("id")
                .longValue();

        mockMvc.perform(delete("/api/admins/{id}", adminId).session(rootSession))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("ROOT 본인 삭제 시도 시 400 반환")
    void deleteAdmin_rootCannotDeleteItself() throws Exception {
        Long rootId = adminRepository
                .findByLoginId(rootLoginId)
                .map(admin -> admin.getId())
                .orElseThrow();

        mockMvc.perform(delete("/api/admins/{id}", rootId).session(rootSession)).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("로그아웃 후 재접근 시 401 반환")
    void logout_thenAccessReturns401() throws Exception {
        mockMvc.perform(post("/api/admin/logout").session(rootSession)).andExpect(status().isOk());

        mockMvc.perform(get("/api/admins").session(rootSession)).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("ROOT로 전체 관리자 목록 조회 성공")
    void listAdmins_asRoot_returnsAll() throws Exception {
        mockMvc.perform(get("/api/admins").session(rootSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].role").value(AdminRole.ROOT.name()));
    }

    // ── PATCH /api/admin/password ─────────────────────────────────────────────

    @Test
    @DisplayName("비밀번호 변경 성공 시 204 반환")
    void changePassword_success_returns204() throws Exception {
        mockMvc.perform(patch("/api/admin/password")
                        .session(rootSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ChangePasswordRequest(rootPassword, "newPassword1!", "newPassword1!"))))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("변경된 비밀번호로 다시 로그인 성공")
    void changePassword_thenLoginWithNewPassword_success() throws Exception {
        String newPassword = "newPassword1!";

        mockMvc.perform(patch("/api/admin/password")
                .session(rootSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new ChangePasswordRequest(rootPassword, newPassword, newPassword))));

        mockMvc.perform(post("/api/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AdminLoginRequest(rootLoginId, newPassword))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ROOT"));
    }

    @Test
    @DisplayName("현재 비밀번호가 틀리면 400 반환")
    void changePassword_wrongCurrentPassword_returns400() throws Exception {
        mockMvc.perform(patch("/api/admin/password")
                        .session(rootSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ChangePasswordRequest("wrongPassword", "newPassword1!", "newPassword1!"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("새 비밀번호와 확인 비밀번호가 다르면 400 반환")
    void changePassword_mismatchedNewPasswords_returns400() throws Exception {
        mockMvc.perform(patch("/api/admin/password")
                        .session(rootSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ChangePasswordRequest(rootPassword, "newPassword1!", "differentPassword!"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("비로그인 상태에서 비밀번호 변경 시 401 반환")
    void changePassword_noAuth_returns401() throws Exception {
        mockMvc.perform(patch("/api/admin/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ChangePasswordRequest(rootPassword, "newPassword1!", "newPassword1!"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("ADMIN 권한으로 비밀번호 변경 시 403 반환")
    void changePassword_asAdmin_returns403() throws Exception {
        AdminCreateRequest createRequest = new AdminCreateRequest("pw_test_admin", "pass1234", "비밀번호테스트관리자");
        mockMvc.perform(post("/api/admins")
                .session(rootSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)));

        MvcResult adminLoginResult = mockMvc.perform(post("/api/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AdminLoginRequest("pw_test_admin", "pass1234"))))
                .andReturn();
        MockHttpSession adminSession =
                (MockHttpSession) adminLoginResult.getRequest().getSession();

        mockMvc.perform(patch("/api/admin/password")
                        .session(adminSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ChangePasswordRequest("pass1234", "newPassword1!", "newPassword1!"))))
                .andExpect(status().isForbidden());
    }
}
