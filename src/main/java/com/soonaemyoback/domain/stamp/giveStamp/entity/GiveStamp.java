package com.soonaemyoback.domain.stamp.giveStamp.entity;

import com.soonaemyoback.domain.member.admin.entity.Admin;
import com.soonaemyoback.domain.member.member.entity.Member;
import com.soonaemyoback.global.jpa.entity.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class GiveStamp extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private Admin admin;

    private String stampKind;

    /** 스탬프 변동 수 : 부여 +1, 삭제 -1 */
    private Integer delta;

    public static GiveStamp create(Member member, Admin admin, String stampKind, int delta) {
        GiveStamp giveStamp = new GiveStamp();
        giveStamp.member = member;
        giveStamp.admin = admin;
        giveStamp.stampKind = stampKind;
        giveStamp.delta = delta;
        return giveStamp;
    }
}
