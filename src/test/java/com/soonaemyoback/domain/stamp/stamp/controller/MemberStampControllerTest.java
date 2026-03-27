package com.soonaemyoback.domain.stamp.stamp.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

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
import com.soonaemyoback.domain.member.admin.dto.AdminLoginRequest;
import com.soonaemyoback.domain.member.member.dto.MemberBatchCreateRequest;
import com.soonaemyoback.domain.member.member.dto.MemberCreateRequest;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MemberStampControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${init.root-admin.login-id}")
    private String rootLoginId;

    @Value("${init.root-admin.password}")
    private String rootPassword;

    @BeforeEach
    void setup() throws Exception {
        // ROOT 로그인 후 회원 2명 등록 (2025-1학기 스탬프판 생성)
        MvcResult rootResult = mockMvc.perform(post("/api/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AdminLoginRequest(rootLoginId, rootPassword))))
                .andExpect(status().isOk())
                .andReturn();
        MockHttpSession rootSession = (MockHttpSession) rootResult.getRequest().getSession();

        MemberBatchCreateRequest batchRequest = new MemberBatchCreateRequest(
                List.of(
                        new MemberCreateRequest("홍길동", "2024001", 2024, 1),
                        new MemberCreateRequest("김철수", "2024002", 2024, 2)),
                2025,
                1);
        mockMvc.perform(post("/api/members")
                .session(rootSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(batchRequest)));
    }

    @Test
    @DisplayName("비로그인 상태에서 이름 + 학번으로 조회 성공")
    void getMemberStamps_byNameAndStudentNum_noAuth_success() throws Exception {
        mockMvc.perform(get("/api/member/stamps").param("name", "홍길동").param("studentNum", "2024001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].year").value(2025))
                .andExpect(jsonPath("$[0].semester").value(1))
                .andExpect(jsonPath("$[0].feedStampNum").value(0))
                .andExpect(jsonPath("$[0].exStampNum").value(0));
    }

    @Test
    @DisplayName("이름 + 학번 + 연도·학기 복합 조회 성공")
    void getMemberStamps_byNameStudentNumAndYearSemester_success() throws Exception {
        mockMvc.perform(get("/api/member/stamps")
                        .param("name", "홍길동")
                        .param("studentNum", "2024001")
                        .param("year", "2025")
                        .param("semester", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].year").value(2025));
    }

    @Test
    @DisplayName("이름은 맞지만 학번이 틀리면 빈 배열 반환")
    void getMemberStamps_wrongStudentNum_returnsEmpty() throws Exception {
        mockMvc.perform(get("/api/member/stamps").param("name", "홍길동").param("studentNum", "9999999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("존재하지 않는 이름과 학번으로 조회하면 빈 배열 반환")
    void getMemberStamps_noMatch_returnsEmpty() throws Exception {
        mockMvc.perform(get("/api/member/stamps").param("name", "없는사람").param("studentNum", "9999999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("이름 파라미터 없이 요청 시 400 반환")
    void getMemberStamps_missingName_returns400() throws Exception {
        mockMvc.perform(get("/api/member/stamps").param("studentNum", "2024001"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("학번 파라미터 없이 요청 시 400 반환")
    void getMemberStamps_missingStudentNum_returns400() throws Exception {
        mockMvc.perform(get("/api/member/stamps").param("name", "홍길동")).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("응답에 stampId·memberName·studentNum·presentCount가 포함되지 않는다")
    void getMemberStamps_responseDoesNotExposeAdminFields() throws Exception {
        mockMvc.perform(get("/api/member/stamps").param("name", "홍길동").param("studentNum", "2024001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].stampId").doesNotExist())
                .andExpect(jsonPath("$[0].memberName").doesNotExist())
                .andExpect(jsonPath("$[0].studentNum").doesNotExist())
                .andExpect(jsonPath("$[0].presentCount").doesNotExist());
    }
}
