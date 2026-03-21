package com.soonaemyoback.domain.stamp.stamp.entity;

import com.soonaemyoback.domain.member.member.entity.Member;
import com.soonaemyoback.global.jpa.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

    @Column(name = "stamp_year")
    private Integer year;

    @Column(name = "stamp_semester")
    private Integer semester;

    private Integer feedStampNum = 0;
    private Integer exStampNum = 0;
    private Integer presentCount = 0;

    public static Stamp create(Member member, Integer year, Integer semester) {
        Stamp stamp = new Stamp();
        stamp.member = member;
        stamp.year = year;
        stamp.semester = semester;
        stamp.feedStampNum = 0;
        stamp.exStampNum = 0;
        stamp.presentCount = 0;
        return stamp;
    }

    public void incrementFeedStampNum() {
        this.feedStampNum++;
    }

    public void incrementExStampNum() {
        this.exStampNum++;
    }
}
