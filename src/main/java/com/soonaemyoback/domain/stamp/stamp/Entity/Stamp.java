package com.soonaemyoback.domain.stamp.stamp.Entity;

import com.soonaemyoback.domain.member.member.entity.Member;
import com.soonaemyoback.global.jpa.entity.BaseEntity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Stamp extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private Integer year;
    private Integer semester;
    private Integer feedStampNum = 0;
    private Integer exStampNum = 0;
    private Integer presentCount = 0;
}
