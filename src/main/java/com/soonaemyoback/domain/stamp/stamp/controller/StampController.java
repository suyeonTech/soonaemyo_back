package com.soonaemyoback.domain.stamp.stamp.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.soonaemyoback.domain.stamp.giveStamp.dto.GiveStampRequest;
import com.soonaemyoback.domain.stamp.stamp.dto.MemberStampResponse;
import com.soonaemyoback.domain.stamp.stamp.dto.StampSummaryResponse;
import com.soonaemyoback.domain.stamp.stamp.service.StampService;
import com.soonaemyoback.global.security.AdminUserDetails;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class StampController {

    private static final int COOKIE_MAX_AGE = 30 * 24 * 60 * 60; // 30일

    private final StampService stampService;

    //관리자의 스탬프 조회(부원검색)
    @GetMapping("/stamps")
    public ResponseEntity<List<StampSummaryResponse>> getStamps(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer semester,
            @RequestParam(required = false) String name,
            HttpServletResponse response) {

        updateFilterCookie(response, "stamp_filter_year", year);

        updateFilterCookie(response, "stamp_filter_semester", semester);

        return ResponseEntity.ok(stampService.getStampList(year, semester, name));
    }

    // 쿠키 처리를 위한 헬퍼 메서드
    private void updateFilterCookie(HttpServletResponse response, String cookieName, Object value) {
        Cookie cookie = new Cookie(cookieName, value != null ? String.valueOf(value) : "");
        cookie.setPath("/");

        if (value != null) {
            cookie.setMaxAge(COOKIE_MAX_AGE);
        } else {
            cookie.setMaxAge(0);
        }

        response.addCookie(cookie);
    }

    //부원의 스탬프 조회(이름, 학번 필요)
    @GetMapping("/member/stamps")
    public ResponseEntity<List<MemberStampResponse>> getMemberStamps(
            @RequestParam String name,
            @RequestParam String studentNum,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer semester) {

        return ResponseEntity.ok(stampService.getMemberStampList(name, studentNum, year, semester));
    }

    //스탬프 부여
    @PostMapping("/stamps/{stampId}/give")
    public ResponseEntity<StampSummaryResponse> giveStamp(
            @PathVariable Long stampId,
            @RequestBody GiveStampRequest request,
            @AuthenticationPrincipal AdminUserDetails adminUserDetails) {

        return ResponseEntity.ok(stampService.giveStamp(stampId, request.stampKind(), adminUserDetails.getAdminId()));
    }

    //스탬프 박탈
    @PostMapping("/stamps/{stampId}/revoke")
    public ResponseEntity<StampSummaryResponse> revokeStamp(
            @PathVariable Long stampId,
            @RequestBody GiveStampRequest request,
            @AuthenticationPrincipal AdminUserDetails adminUserDetails) {

        return ResponseEntity.ok(stampService.deleteStamp(stampId, request.stampKind(), adminUserDetails.getAdminId()));
    }
}
