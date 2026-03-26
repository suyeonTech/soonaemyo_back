package com.soonaemyoback.domain.member.member.entity;

import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDate;

import com.soonaemyoback.global.jpa.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Member extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String studentNum;

    private String profilePhoto;

    @Column(nullable = false)
    private Integer totalStampCount = 0;

    @Column(nullable = false)
    private Integer joinedYear;

    @Column(nullable = false)
    private Integer joinedSemester;

    private LocalDate withdrawnAt;

    public static Member create(String name, String studentNum, Integer joinedYear, Integer joinedSemester) {
        Member member = new Member();
        member.name = name;
        member.studentNum = studentNum;
        member.joinedYear = joinedYear;
        member.joinedSemester = joinedSemester;
        member.totalStampCount = 0;
        return member;
    }

    public void incrementTotalStampCount() {
        this.totalStampCount++;
    }

    public void decrementTotalStampCount() {
        if (this.totalStampCount <= 0) {
            throw new IllegalStateException("누적 스탬프 수가 이미 0입니다.");
        }
        this.totalStampCount--;
    }
}
