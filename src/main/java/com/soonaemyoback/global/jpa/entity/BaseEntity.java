package com.soonaemyoback.global.jpa.entity;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDateTime;
import java.util.Objects;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass // 엔티티의 부모 클래스에는 이걸 달아야 한다.
@EntityListeners(AuditingEntityListener.class)
@Getter
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Setter(PROTECTED)
    private Long id;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime modifiedAt;

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        BaseEntity that = (BaseEntity) object;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
