package com.soonaemyoback.domain.member.admin.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.soonaemyoback.domain.member.admin.entity.Admin;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByLoginId(String loginId);

    boolean existsByLoginId(String loginId);
}
