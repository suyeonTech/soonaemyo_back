package com.soonaemyoback.domain.member.member.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soonaemyoback.domain.member.admin.dto.AdminCreateRequest;
import com.soonaemyoback.domain.member.admin.dto.AdminLoginRequest;
import com.soonaemyoback.domain.member.member.dto.MemberBatchCreateRequest;
import com.soonaemyoback.domain.member.member.dto.MemberCreateRequest;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${init.root-admin.login-id}")
    private String rootLoginId;

    @Value("${init.root-admin.password}")
    private String rootPassword;

    private MockHttpSession rootSession;
    private MockHttpSession adminSession;

    @BeforeEach
    void setup() throws Exception {
        MvcResult rootResult = mockMvc.perform(post("/api/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AdminLoginRequest(rootLoginId, rootPassword))))
                .andExpect(status().isOk())
                .andReturn();
        rootSession = (MockHttpSession) rootResult.getRequest().getSession();

        mockMvc.perform(post("/api/admins")
                .session(rootSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new AdminCreateRequest("test_admin", "pass1234", "테스트관리자"))));

        MvcResult adminResult = mockMvc.perform(post("/api/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AdminLoginRequest("test_admin", "pass1234"))))
                .andExpect(status().isOk())
                .andReturn();
        adminSession = (MockHttpSession) adminResult.getRequest().getSession();
    }

    private MemberCreateRequest member(String studentNum) {
        return new MemberCreateRequest("홍길동", studentNum, 2024, 1);
    }

    private MemberBatchCreateRequest batch(List<MemberCreateRequest> members) {
        return new MemberBatchCreateRequest(members, 2025, 1);
    }

    @Test
    @DisplayName("인증 없이 회원 등록 시 401 반환")
    void registerMembers_noAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(batch(List.of(member("2024001"))))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("ADMIN으로 1명 등록 성공 및 스탬프 생성 확인")
    void registerMembers_asAdmin_singleMember_returns201() throws Exception {
        mockMvc.perform(post("/api/members")
                        .session(adminSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(batch(List.of(member("2024001"))))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].studentNum").value("2024001"));
    }

    @Test
    @DisplayName("ROOT로 5명 등록 성공")
    void registerMembers_asRoot_fiveMembers_returns201() throws Exception {
        List<MemberCreateRequest> members =
                IntStream.rangeClosed(1, 5).mapToObj(i -> member("202400" + i)).toList();

        mockMvc.perform(post("/api/members")
                        .session(rootSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(batch(members))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(5));
    }

    @Test
    @DisplayName("6명 등록 시도 시 400 반환")
    void registerMembers_sixMembers_returns400() throws Exception {
        List<MemberCreateRequest> members =
                IntStream.rangeClosed(1, 6).mapToObj(i -> member("202400" + i)).toList();

        mockMvc.perform(post("/api/members")
                        .session(rootSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(batch(members))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("빈 리스트 전송 시 400 반환")
    void registerMembers_emptyList_returns400() throws Exception {
        mockMvc.perform(post("/api/members")
                        .session(rootSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(batch(List.of()))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("동일 studentNum 중복 등록 시 400 반환")
    void registerMembers_duplicateStudentNum_returns400() throws Exception {
        mockMvc.perform(post("/api/members")
                .session(rootSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(batch(List.of(member("2024999"))))));

        mockMvc.perform(post("/api/members")
                        .session(rootSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(batch(List.of(member("2024999"))))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("요청 내 중복 studentNum 시 400 반환")
    void registerMembers_duplicateStudentNumInRequest_returns400() throws Exception {
        mockMvc.perform(post("/api/members")
                        .session(rootSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(batch(List.of(member("2024001"), member("2024001"))))))
                .andExpect(status().isBadRequest());
    }
}
