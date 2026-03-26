package com.soonaemyoback.domain.stamp.stamp.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.soonaemyoback.domain.member.admin.dto.AdminCreateRequest;
import com.soonaemyoback.domain.member.admin.dto.AdminLoginRequest;
import com.soonaemyoback.domain.member.member.dto.MemberBatchCreateRequest;
import com.soonaemyoback.domain.member.member.dto.MemberCreateRequest;
import com.soonaemyoback.domain.stamp.giveStamp.dto.GiveStampRequest;
import com.soonaemyoback.domain.stamp.stamp.entity.StampKind;

import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class StampControllerTest {

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

    /** 테스트에서 사용할 스탬프 ID (setup에서 채워짐) */
    private Long stampId;

    @BeforeEach
    void setup() throws Exception {
        // ROOT 로그인
        MvcResult rootResult = mockMvc.perform(post("/api/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AdminLoginRequest(rootLoginId, rootPassword))))
                .andExpect(status().isOk())
                .andReturn();
        rootSession = (MockHttpSession) rootResult.getRequest().getSession();

        // ROOT가 ADMIN 생성
        mockMvc.perform(post("/api/admins")
                .session(rootSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new AdminCreateRequest("stamp_admin", "pass1234", "스탬프관리자"))));

        // ADMIN 로그인
        MvcResult adminResult = mockMvc.perform(post("/api/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AdminLoginRequest("stamp_admin", "pass1234"))))
                .andExpect(status().isOk())
                .andReturn();
        adminSession = (MockHttpSession) adminResult.getRequest().getSession();

        // 회원 2명 등록 — 이름을 달리해 이름 검색 테스트에 활용
        // 홍길동(2024001), 김철수(2024002)
        MemberBatchCreateRequest batchRequest = new MemberBatchCreateRequest(
                List.of(
                        new MemberCreateRequest("홍길동", "2024001", 2024, 1),
                        new MemberCreateRequest("김철수", "2024002", 2024, 2)),
                2025,
                1);
        mockMvc.perform(post("/api/members")
                .session(adminSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(batchRequest)));

        // 첫 번째 스탬프 ID·회원 ID 조회 (giveStamp / deleteStamp 테스트용)
        MvcResult stampsResult = mockMvc.perform(get("/api/stamps")
                        .session(adminSession)
                        .param("year", "2025")
                        .param("semester", "1"))
                .andReturn();
        stampId = objectMapper
                .readTree(stampsResult.getResponse().getContentAsString())
                .get(0)
                .get("stampId")
                .asLong();
    }

    // ── GET /api/stamps — 인증 ─────────────────────────────────────────────────

    @Test
    @DisplayName("인증 없이 조회 시 401 반환")
    void getStamps_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/stamps").param("year", "2025").param("semester", "1"))
                .andExpect(status().isUnauthorized());
    }

    // ── GET /api/stamps — 연도·학기 검색 ─────────────────────────────────────

    @Test
    @DisplayName("연도·학기로만 조회 시 해당 시기 전체 반환 및 쿠키 저장")
    void getStamps_byYearAndSemester_withCookies() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/stamps")
                        .session(adminSession)
                        .param("year", "2025")
                        .param("semester", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andReturn();

        MockHttpServletResponse response = result.getResponse();
        assertThat(response.getCookie("stamp_filter_year").getValue()).isEqualTo("2025");
        assertThat(response.getCookie("stamp_filter_semester").getValue()).isEqualTo("1");
    }

    @Test
    @DisplayName("ROOT로 연도·학기 조회 성공")
    void getStamps_asRoot_byYearAndSemester() throws Exception {
        mockMvc.perform(get("/api/stamps")
                        .session(rootSession)
                        .param("year", "2025")
                        .param("semester", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("일치하는 데이터 없는 연도·학기 조회 시 빈 배열 반환")
    void getStamps_byYearAndSemester_noData() throws Exception {
        mockMvc.perform(get("/api/stamps")
                        .session(adminSession)
                        .param("year", "2099")
                        .param("semester", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ── GET /api/stamps — 이름 검색 ──────────────────────────────────────────

    @Test
    @DisplayName("이름만으로 조회 시 해당 이름 포함 결과 반환, 쿠키 미저장")
    void getStamps_byNameOnly_returnsMatchedAndNoCookies() throws Exception {
        MvcResult result = mockMvc.perform(
                        get("/api/stamps").session(adminSession).param("name", "홍길동"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].memberName").value("홍길동"))
                .andReturn();

        // 이름만 검색할 때 year/semester 쿠키가 설정되지 않아야 한다
        assertThat(result.getResponse().getCookie("stamp_filter_year")).isNull();
        assertThat(result.getResponse().getCookie("stamp_filter_semester")).isNull();
    }

    @Test
    @DisplayName("이름 부분 일치 검색")
    void getStamps_byPartialName() throws Exception {
        mockMvc.perform(get("/api/stamps").session(adminSession).param("name", "홍"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].memberName").value("홍길동"));
    }

    @Test
    @DisplayName("이름이 일치하지 않으면 빈 배열 반환")
    void getStamps_byName_noMatch() throws Exception {
        mockMvc.perform(get("/api/stamps").session(adminSession).param("name", "없는이름"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ── GET /api/stamps — 연도·학기 + 이름 복합 검색 ──────────────────────────

    @Test
    @DisplayName("연도·학기 + 이름 복합 조회 — 일치하는 결과 반환")
    void getStamps_byYearSemesterAndName_matched() throws Exception {
        mockMvc.perform(get("/api/stamps")
                        .session(adminSession)
                        .param("year", "2025")
                        .param("semester", "1")
                        .param("name", "홍"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].memberName").value("홍길동"));
    }

    @Test
    @DisplayName("연도·학기 + 이름 복합 조회 — 연도는 맞지만 이름 불일치 시 빈 배열")
    void getStamps_byYearSemesterAndName_nameMismatch() throws Exception {
        mockMvc.perform(get("/api/stamps")
                        .session(adminSession)
                        .param("year", "2025")
                        .param("semester", "1")
                        .param("name", "없는이름"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("응답 필드 확인 (stampId·이름·학번·스탬프 카운트)")
    void getStamps_responseFields() throws Exception {
        mockMvc.perform(get("/api/stamps")
                        .session(adminSession)
                        .param("year", "2025")
                        .param("semester", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].stampId").isNumber())
                .andExpect(jsonPath("$[0].memberName").isString())
                .andExpect(jsonPath("$[0].studentNum").isString())
                .andExpect(jsonPath("$[0].feedStampNum").value(0))
                .andExpect(jsonPath("$[0].exStampNum").value(0))
                .andExpect(jsonPath("$[0].presentCount").value(0));
    }

    // ── POST /api/stamps/{id}/give ────────────────────────────────────────────

    @Test
    @DisplayName("인증 없이 스탬프 부여 시 401 반환")
    void giveStamp_noAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/stamps/{stampId}/give", stampId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GiveStampRequest(StampKind.FOOD))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("FOOD 스탬프 부여 시 feedStampNum +1 반영")
    void giveStamp_food_returnsFeedStampNumIncremented() throws Exception {
        mockMvc.perform(post("/api/stamps/{stampId}/give", stampId)
                        .session(adminSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GiveStampRequest(StampKind.FOOD))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.feedStampNum").value(1))
                .andExpect(jsonPath("$.exStampNum").value(0));
    }

    @Test
    @DisplayName("EXTRA 스탬프 부여 시 exStampNum +1 반영")
    void giveStamp_extra_returnsExStampNumIncremented() throws Exception {
        mockMvc.perform(post("/api/stamps/{stampId}/give", stampId)
                        .session(adminSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GiveStampRequest(StampKind.EXTRA))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.feedStampNum").value(0))
                .andExpect(jsonPath("$.exStampNum").value(1));
    }

    @Test
    @DisplayName("존재하지 않는 stampId로 부여 시 404 반환")
    void giveStamp_notFound_returns404() throws Exception {
        mockMvc.perform(post("/api/stamps/{stampId}/give", 99999L)
                        .session(adminSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GiveStampRequest(StampKind.FOOD))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("ROOT로 스탬프 부여 성공")
    void giveStamp_asRoot_success() throws Exception {
        mockMvc.perform(post("/api/stamps/{stampId}/give", stampId)
                        .session(rootSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GiveStampRequest(StampKind.FOOD))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.feedStampNum").value(1));
    }

    // ── DELETE /api/stamps/give ───────────────────────────────────────────────

    @Test
    @DisplayName("인증 없이 스탬프 삭제 시 401 반환")
    void deleteStamp_noAuth_returns401() throws Exception {
        mockMvc.perform(delete("/api/stamps/{stampId}/give", stampId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GiveStampRequest(StampKind.FOOD))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("FOOD 스탬프 부여 후 삭제 시 feedStampNum 다시 0으로")
    void deleteStamp_food_afterGive_returnsZero() throws Exception {
        mockMvc.perform(post("/api/stamps/{stampId}/give", stampId)
                .session(adminSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new GiveStampRequest(StampKind.FOOD))));

        mockMvc.perform(delete("/api/stamps/{stampId}/give", stampId)
                        .session(adminSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GiveStampRequest(StampKind.FOOD))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.feedStampNum").value(0))
                .andExpect(jsonPath("$.exStampNum").value(0));
    }

    @Test
    @DisplayName("EXTRA 스탬프 부여 후 삭제 시 exStampNum 다시 0으로")
    void deleteStamp_extra_afterGive_returnsZero() throws Exception {
        mockMvc.perform(post("/api/stamps/{stampId}/give", stampId)
                .session(adminSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new GiveStampRequest(StampKind.EXTRA))));

        mockMvc.perform(delete("/api/stamps/{stampId}/give", stampId)
                        .session(adminSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GiveStampRequest(StampKind.EXTRA))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exStampNum").value(0));
    }

    @Test
    @DisplayName("스탬프 수가 0인 상태에서 삭제 시 400 반환")
    void deleteStamp_alreadyZero_returns400() throws Exception {
        mockMvc.perform(delete("/api/stamps/{stampId}/give", stampId)
                        .session(adminSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GiveStampRequest(StampKind.FOOD))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("존재하지 않는 stampId로 삭제 시 404 반환")
    void deleteStamp_stampNotFound_returns404() throws Exception {
        mockMvc.perform(delete("/api/stamps/{stampId}/give", 99999L)
                        .session(adminSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GiveStampRequest(StampKind.FOOD))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("ROOT로 스탬프 삭제 성공")
    void deleteStamp_asRoot_success() throws Exception {
        mockMvc.perform(post("/api/stamps/{stampId}/give", stampId)
                .session(rootSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new GiveStampRequest(StampKind.FOOD))));

        mockMvc.perform(delete("/api/stamps/{stampId}/give", stampId)
                        .session(rootSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GiveStampRequest(StampKind.FOOD))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.feedStampNum").value(0));
    }
}
