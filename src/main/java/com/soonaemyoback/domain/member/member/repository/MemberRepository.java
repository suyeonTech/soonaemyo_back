package com.soonaemyoback.domain.member.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.soonaemyoback.domain.member.member.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByStudentNum(String studentNum);
}
