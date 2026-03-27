package com.soonaemyoback.domain.stamp.stamp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.soonaemyoback.domain.stamp.stamp.entity.Stamp;

public interface StampRepository extends JpaRepository<Stamp, Long> {

    @Query("SELECT s FROM Stamp s JOIN FETCH s.member m "
            + "WHERE (:year IS NULL OR s.year = :year) "
            + "AND (:semester IS NULL OR s.semester = :semester) "
            + "AND (:name IS NULL OR m.name LIKE CONCAT('%', :name, '%')) "
            + "AND (:studentNum IS NULL OR m.studentNum = :studentNum) "
            + "ORDER BY m.name")
    List<Stamp> searchStamps(
            @Param("year") Integer year,
            @Param("semester") Integer semester,
            @Param("name") String name,
            @Param("studentNum") String studentNum);

    @Query("SELECT s FROM Stamp s JOIN FETCH s.member WHERE s.id = :id")
    Optional<Stamp> findByIdWithMember(@Param("id") Long id);

    @Query("SELECT s FROM Stamp s JOIN FETCH s.member m "
            + "WHERE m.id = :memberId AND s.year = :year AND s.semester = :semester")
    Optional<Stamp> findByMemberIdAndYearAndSemester(
            @Param("memberId") Long memberId, @Param("year") Integer year, @Param("semester") Integer semester);
}
