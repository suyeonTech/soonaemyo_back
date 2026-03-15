package com.soonaemyoback.domain.member.admin.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.soonaemyoback.global.jpa.entity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor
public class Admin extends BaseEntity {
    private String name;
    private String role; // String으로 변경 제안드렸던 부분입니다.
}
