package com.soonaemyoback.domain.member.admin.entity;

import com.soonaemyoback.global.jpa.entity.BaseEntity;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Admin extends BaseEntity {
    private String name;
    private String role; // String으로 변경 제안드렸던 부분입니다.
}
