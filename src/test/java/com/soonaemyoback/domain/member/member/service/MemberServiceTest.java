package com.soonaemyoback.domain.member.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.soonaemyoback.domain.member.member.dto.MemberBatchCreateRequest;
import com.soonaemyoback.domain.member.member.dto.MemberCreateRequest;
import com.soonaemyoback.domain.member.member.dto.MemberResponse;
import com.soonaemyoback.domain.member.member.repository.MemberRepository;
import com.soonaemyoback.domain.stamp.stamp.entity.Stamp;
import com.soonaemyoback.domain.stamp.stamp.repository.StampRepository;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private StampRepository stampRepository;

    @InjectMocks
    private MemberService memberService;

    private MemberCreateRequest req(String studentNum) {
        return new MemberCreateRequest("홍길동", studentNum, 2024, 1);
    }

    private MemberBatchCreateRequest batchReq(List<MemberCreateRequest> members) {
        return new MemberBatchCreateRequest(members, 2025, 1);
    }

    @Test
    @DisplayName("3명 정상 등록 후 스탬프도 저장된다")
    void registerMembers_success() {
        List<MemberCreateRequest> list = List.of(req("2024001"), req("2024002"), req("2024003"));
        given(memberRepository.existsByStudentNum(anyString())).willReturn(false);
        given(memberRepository.saveAll(anyList())).willAnswer(inv -> inv.getArgument(0));
        given(stampRepository.saveAll(anyList())).willAnswer(inv -> inv.getArgument(0));

        List<MemberResponse> result = memberService.registerMembers(batchReq(list));

        assertThat(result).hasSize(3);
        verify(memberRepository).saveAll(anyList());
        verify(stampRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("등록 시 스탬프의 모든 카운트가 0으로 초기화된다")
    void registerMembers_stampInitializedToZero() {
        List<MemberCreateRequest> list = List.of(req("2024001"));
        given(memberRepository.existsByStudentNum(anyString())).willReturn(false);
        given(memberRepository.saveAll(anyList())).willAnswer(inv -> inv.getArgument(0));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Stamp>> captor = ArgumentCaptor.forClass(List.class);
        given(stampRepository.saveAll(captor.capture())).willAnswer(inv -> inv.getArgument(0));

        memberService.registerMembers(batchReq(list));

        Stamp stamp = captor.getValue().get(0);
        assertThat(stamp.getFeedStampNum()).isEqualTo(0);
        assertThat(stamp.getExStampNum()).isEqualTo(0);
        assertThat(stamp.getPresentCount()).isEqualTo(0);
        assertThat(stamp.getYear()).isEqualTo(2025);
        assertThat(stamp.getSemester()).isEqualTo(1);
    }

    @Test
    @DisplayName("빈 리스트 전송 시 예외 발생")
    void registerMembers_emptyList_throws() {
        assertThatThrownBy(() -> memberService.registerMembers(batchReq(List.of())))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("6명 초과 시 예외 발생")
    void registerMembers_over5_throws() {
        List<MemberCreateRequest> list =
                List.of(req("2024001"), req("2024002"), req("2024003"), req("2024004"), req("2024005"), req("2024006"));

        assertThatThrownBy(() -> memberService.registerMembers(batchReq(list)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("DB에 이미 존재하는 studentNum 시 예외 발생")
    void registerMembers_existingStudentNum_throws() {
        List<MemberCreateRequest> list = List.of(req("2024001"));
        given(memberRepository.existsByStudentNum("2024001")).willReturn(true);

        assertThatThrownBy(() -> memberService.registerMembers(batchReq(list)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("2024001");
    }

    @Test
    @DisplayName("요청 내 중복 studentNum 시 예외 발생")
    void registerMembers_duplicateStudentNumInRequest_throws() {
        List<MemberCreateRequest> list = List.of(req("2024001"), req("2024001"));

        assertThatThrownBy(() -> memberService.registerMembers(batchReq(list)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("정상 등록 시 saveAll이 호출된다")
    void registerMembers_callsSaveAll() {
        List<MemberCreateRequest> list = List.of(req("2024001"), req("2024002"));
        given(memberRepository.existsByStudentNum(anyString())).willReturn(false);
        given(memberRepository.saveAll(anyList())).willAnswer(inv -> inv.getArgument(0));
        given(stampRepository.saveAll(anyList())).willAnswer(inv -> inv.getArgument(0));

        memberService.registerMembers(batchReq(list));

        verify(memberRepository).saveAll(anyList());
        verify(stampRepository).saveAll(anyList());
    }
}
