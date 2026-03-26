package com.soonaemyoback.domain.stamp.stamp.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.soonaemyoback.domain.member.admin.entity.Admin;
import com.soonaemyoback.domain.member.admin.repository.AdminRepository;
import com.soonaemyoback.domain.member.member.entity.Member;
import com.soonaemyoback.domain.stamp.giveStamp.entity.GiveStamp;
import com.soonaemyoback.domain.stamp.giveStamp.repository.GiveStampRepository;
import com.soonaemyoback.domain.stamp.stamp.dto.MemberStampResponse;
import com.soonaemyoback.domain.stamp.stamp.dto.StampSummaryResponse;
import com.soonaemyoback.domain.stamp.stamp.entity.Stamp;
import com.soonaemyoback.domain.stamp.stamp.entity.StampKind;
import com.soonaemyoback.domain.stamp.stamp.repository.StampRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StampService {

    private final StampRepository stampRepository;
    private final GiveStampRepository giveStampRepository;
    private final AdminRepository adminRepository;

    public List<MemberStampResponse> getMemberStampList(
            String name, String studentNum, Integer year, Integer semester) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("이름을 입력해주세요.");
        }
        if (studentNum == null || studentNum.isBlank()) {
            throw new IllegalArgumentException("학번을 입력해주세요.");
        }
        return stampRepository.searchStamps(year, semester, name, studentNum).stream()
                .map(MemberStampResponse::from)
                .toList();
    }

    public List<StampSummaryResponse> getStampList(Integer year, Integer semester, String name) {
        return stampRepository
                .searchStamps(year, semester, (name != null && name.isBlank()) ? null : name, null)
                .stream()
                .map(StampSummaryResponse::from)
                .toList();
    }

    @Transactional
    public StampSummaryResponse giveStamp(Long stampId, StampKind stampKind, Long adminId) {
        Stamp stamp = stampRepository
                .findByIdWithMember(stampId)
                .orElseThrow(() -> new NoSuchElementException("스탬프 기록을 찾을 수 없습니다: " + stampId));

        Admin admin = adminRepository
                .findById(adminId)
                .orElseThrow(() -> new NoSuchElementException("관리자를 찾을 수 없습니다: " + adminId));

        Member member = stamp.getMember();

        if (stampKind == StampKind.FOOD) {
            stamp.incrementFeedStampNum();
        } else {
            stamp.incrementExStampNum();
        }

        member.incrementTotalStampCount();

        giveStampRepository.save(GiveStamp.create(member, admin, stampKind.name(), 1));

        return StampSummaryResponse.from(stamp);
    }

    @Transactional
    public StampSummaryResponse deleteStamp(Long stampId, StampKind stampKind, Long adminId) {
        Stamp stamp = stampRepository
                .findByIdWithMember(stampId)
                .orElseThrow(() -> new NoSuchElementException("스탬프 기록을 찾을 수 없습니다: " + stampId));

        Admin admin = adminRepository
                .findById(adminId)
                .orElseThrow(() -> new NoSuchElementException("관리자를 찾을 수 없습니다: " + adminId));

        Member member = stamp.getMember();

        if (stampKind == StampKind.FOOD) {
            stamp.decrementFeedStampNum();
        } else {
            stamp.decrementExStampNum();
        }

        member.decrementTotalStampCount();

        giveStampRepository.save(GiveStamp.create(member, admin, stampKind.name(), -1));

        return StampSummaryResponse.from(stamp);
    }
}
