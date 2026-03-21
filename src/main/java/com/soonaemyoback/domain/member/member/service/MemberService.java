package com.soonaemyoback.domain.member.member.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.soonaemyoback.domain.member.member.dto.MemberBatchCreateRequest;
import com.soonaemyoback.domain.member.member.dto.MemberCreateRequest;
import com.soonaemyoback.domain.member.member.dto.MemberResponse;
import com.soonaemyoback.domain.member.member.entity.Member;
import com.soonaemyoback.domain.member.member.repository.MemberRepository;
import com.soonaemyoback.domain.stamp.stamp.entity.Stamp;
import com.soonaemyoback.domain.stamp.stamp.repository.StampRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final StampRepository stampRepository;

    @Transactional
    public List<MemberResponse> registerMembers(MemberBatchCreateRequest request) {
        List<MemberCreateRequest> members = request.members();

        if (members == null || members.isEmpty() || members.size() > 5) {
            throw new IllegalArgumentException("회원은 한 번에 1명 이상 5명 이하로 등록할 수 있습니다.");
        }

        // 요청 내 studentNum 중복 체크
        long distinctCount =
                members.stream().map(MemberCreateRequest::studentNum).distinct().count();
        if (distinctCount != members.size()) {
            throw new IllegalArgumentException("요청 내에 중복된 학번이 존재합니다.");
        }

        // DB 중복 체크
        for (MemberCreateRequest req : members) {
            if (memberRepository.existsByStudentNum(req.studentNum())) {
                throw new IllegalArgumentException("이미 등록된 학번입니다: " + req.studentNum());
            }
        }

        List<Member> savedMembers = memberRepository.saveAll(members.stream()
                .map(req -> Member.create(req.name(), req.studentNum(), req.joinedYear(), req.joinedSemester()))
                .collect(Collectors.toList()));

        List<Stamp> stamps = savedMembers.stream()
                .map(member -> Stamp.create(member, request.currentYear(), request.currentSemester()))
                .collect(Collectors.toList());
        stampRepository.saveAll(stamps);

        return savedMembers.stream().map(MemberResponse::from).toList();
    }
}
