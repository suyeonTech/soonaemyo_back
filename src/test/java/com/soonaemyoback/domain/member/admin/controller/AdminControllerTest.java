package com.soonaemyoback.domain.member.admin.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    @DisplayName("ROOT 로그인 성공 시 세션이 생성된다")
    void login_success_createsSession() {
        assertThat(rootSession).isNotNull();
        assertThat(rootSession.getAttribute("SPRING_SECURITY_CONTEXT")).isNotNull();
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
}
