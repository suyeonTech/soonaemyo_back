package com.soonaemyoback.domain.member.member.repository;

import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.soonaemyoback.domain.member.member.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByStudentNum(String studentNum);

    @Query("SELECT COUNT(m) > 0 FROM Member m WHERE m.studentNum IN :studentNums")
    boolean existsAnyByStudentNumIn(@Param("studentNums") Collection<String> studentNums);
}
