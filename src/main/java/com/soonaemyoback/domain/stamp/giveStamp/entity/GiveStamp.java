package com.soonaemyoback.domain.stamp.giveStamp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.soonaemyoback.domain.member.admin.entity.Admin;
import com.soonaemyoback.domain.member.member.entity.Member;
import com.soonaemyoback.global.jpa.entity.BaseEntity;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class GiveStamp extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private Admin admin;

    private String stampKind; // "FOOD", "EXTRA" 등
}
