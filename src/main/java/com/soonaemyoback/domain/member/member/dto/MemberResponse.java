package com.soonaemyoback.domain.member.member.dto;

import java.time.LocalDateTime;

import com.soonaemyoback.domain.member.member.entity.Member;

public record MemberResponse(
        Long id,
        String name,
        String studentNum,
        Integer joinedYear,
        Integer joinedSemester,
        Integer totalStampCount,
        LocalDateTime createdAt) {

    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getName(),
                member.getStudentNum(),
                member.getJoinedYear(),
                member.getJoinedSemester(),
                member.getTotalStampCount(),
                member.getCreatedAt());
    }
}
