package com.soonaemyoback.domain.stamp.stamp.service;

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

import com.soonaemyoback.domain.member.admin.entity.Admin;
import com.soonaemyoback.domain.member.admin.entity.AdminRole;
import com.soonaemyoback.domain.member.admin.repository.AdminRepository;
import com.soonaemyoback.domain.member.member.entity.Member;
import com.soonaemyoback.domain.stamp.giveStamp.entity.GiveStamp;
import com.soonaemyoback.domain.stamp.giveStamp.repository.GiveStampRepository;
import com.soonaemyoback.domain.stamp.stamp.dto.MemberStampResponse;
import com.soonaemyoback.domain.stamp.stamp.dto.StampSummaryResponse;
import com.soonaemyoback.domain.stamp.stamp.entity.Stamp;
import com.soonaemyoback.domain.stamp.stamp.entity.StampKind;
import com.soonaemyoback.domain.stamp.stamp.repository.StampRepository;

@ExtendWith(MockitoExtension.class)
class StampServiceTest {

    @Mock
    private StampRepository stampRepository;

    @Mock
    private GiveStampRepository giveStampRepository;

    @Mock
    private AdminRepository adminRepository;

    @InjectMocks
    private StampService stampService;

    private Member member(String name, String studentNum) {
        return Member.create(name, studentNum, 2024, 1);
    }

    private Stamp stamp(Member member) {
        return Stamp.create(member, 2025, 1);
    }

    private Admin admin() {
        return Admin.create("admin1", "encoded", "관리자", AdminRole.ADMIN);
    }

    // ── getMemberStampList ────────────────────────────────────────────────────

    @Test
    @DisplayName("이름으로만 조회 - 해당 이름의 모든 학기 스탬프 반환")
    void getMemberStampList_byNameOnly() {
        Stamp s1 = Stamp.create(member("홍길동", "2024001"), 2024, 2);
        Stamp s2 = Stamp.create(member("홍길동", "2024001"), 2025, 1);
        given(stampRepository.searchStamps(null, null, "홍길동")).willReturn(List.of(s1, s2));

        List<MemberStampResponse> result = stampService.getMemberStampList("홍길동", null, null);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).year()).isEqualTo(2024);
        assertThat(result.get(0).semester()).isEqualTo(2);
        assertThat(result.get(0).feedStampNum()).isEqualTo(0);
        assertThat(result.get(0).exStampNum()).isEqualTo(0);
    }

    @Test
    @DisplayName("이름 + 연도·학기 조합 조회")
    void getMemberStampList_byNameAndYearSemester() {
        Stamp s = Stamp.create(member("홍길동", "2024001"), 2025, 1);
        given(stampRepository.searchStamps(2025, 1, "홍길동")).willReturn(List.of(s));

        List<MemberStampResponse> result = stampService.getMemberStampList("홍길동", 2025, 1);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).year()).isEqualTo(2025);
        assertThat(result.get(0).semester()).isEqualTo(1);
    }

    @Test
    @DisplayName("이름 없이 호출 시 IllegalArgumentException 발생")
    void getMemberStampList_noName_throws() {
        assertThatThrownBy(() -> stampService.getMemberStampList(null, 2025, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이름");
    }

    @Test
    @DisplayName("공백 이름으로 호출 시 IllegalArgumentException 발생")
    void getMemberStampList_blankName_throws() {
        assertThatThrownBy(() -> stampService.getMemberStampList("   ", 2025, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이름");
    }

    // ── getStampList ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("연도·학기로만 조회")
    void getStampList_byYearAndSemester() {
        Stamp s1 = stamp(member("홍길동", "2024001"));
        Stamp s2 = stamp(member("김철수", "2024002"));
        given(stampRepository.searchStamps(2025, 1, null)).willReturn(List.of(s1, s2));

        List<StampSummaryResponse> result = stampService.getStampList(2025, 1, null);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).memberName()).isEqualTo("홍길동");
        assertThat(result.get(0).feedStampNum()).isEqualTo(0);
    }

    @Test
    @DisplayName("이름만으로 조회")
    void getStampList_byNameOnly() {
        Stamp s = stamp(member("홍길동", "2024001"));
        given(stampRepository.searchStamps(null, null, "홍")).willReturn(List.of(s));

        List<StampSummaryResponse> result = stampService.getStampList(null, null, "홍");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).memberName()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("연도·학기 + 이름 조합 조회")
    void getStampList_byYearSemesterAndName() {
        Stamp s = stamp(member("홍길동", "2024001"));
        given(stampRepository.searchStamps(2025, 1, "홍")).willReturn(List.of(s));

        List<StampSummaryResponse> result = stampService.getStampList(2025, 1, "홍");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).memberName()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("공백 이름 입력 시 name=null로 처리되어 전체 조회")
    void getStampList_blankName_treatedAsNull() {
        Stamp s1 = stamp(member("홍길동", "2024001"));
        Stamp s2 = stamp(member("김철수", "2024002"));
        given(stampRepository.searchStamps(2025, 1, null)).willReturn(List.of(s1, s2));

        List<StampSummaryResponse> result = stampService.getStampList(2025, 1, "   ");

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("결과가 없으면 빈 리스트 반환")
    void getStampList_empty() {
        given(stampRepository.searchStamps(2099, 2, null)).willReturn(List.of());

        assertThat(stampService.getStampList(2099, 2, null)).isEmpty();
    }

    // ── giveStamp ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("FOOD 스탬프 부여 시 feedStampNum +1, totalStampCount +1")
    void giveStamp_food_success() {
        Member member = member("홍길동", "2024001");
        Stamp stamp = stamp(member);
        Admin admin = admin();

        given(stampRepository.findByIdWithMember(1L)).willReturn(Optional.of(stamp));
        given(adminRepository.findById(10L)).willReturn(Optional.of(admin));
        given(giveStampRepository.save(any(GiveStamp.class))).willAnswer(inv -> inv.getArgument(0));

        StampSummaryResponse result = stampService.giveStamp(1L, StampKind.FOOD, 10L);

        assertThat(result.feedStampNum()).isEqualTo(1);
        assertThat(result.exStampNum()).isEqualTo(0);
        assertThat(member.getTotalStampCount()).isEqualTo(1);
        verify(giveStampRepository).save(any(GiveStamp.class));
    }

    @Test
    @DisplayName("EXTRA 스탬프 부여 시 exStampNum +1, totalStampCount +1")
    void giveStamp_extra_success() {
        Member member = member("김철수", "2024002");
        Stamp stamp = stamp(member);
        Admin admin = admin();

        given(stampRepository.findByIdWithMember(2L)).willReturn(Optional.of(stamp));
        given(adminRepository.findById(10L)).willReturn(Optional.of(admin));
        given(giveStampRepository.save(any(GiveStamp.class))).willAnswer(inv -> inv.getArgument(0));

        StampSummaryResponse result = stampService.giveStamp(2L, StampKind.EXTRA, 10L);

        assertThat(result.feedStampNum()).isEqualTo(0);
        assertThat(result.exStampNum()).isEqualTo(1);
        assertThat(member.getTotalStampCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("존재하지 않는 stampId로 부여 시 NoSuchElementException 발생")
    void giveStamp_stampNotFound_throws() {
        given(stampRepository.findByIdWithMember(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> stampService.giveStamp(999L, StampKind.FOOD, 1L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("999");
    }
}
