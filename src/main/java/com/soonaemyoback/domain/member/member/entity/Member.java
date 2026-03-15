package com.soonaemyoback.domain.member.member.entity;

import java.time.LocalDate;

import com.soonaemyoback.global.jpa.entity.BaseEntity;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Member extends BaseEntity {
    private String name;
    private String studentNum;
    private String profilePhoto; // 파일 경로 저장
    private Integer totalStampCount = 0;

    private Integer joinedYear;
    private Integer joinedSemester;
    private LocalDate withdrawnAt; // 탈퇴 시기 (Nullable)
}
