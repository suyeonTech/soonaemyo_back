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

    @GetMapping("/stamps")
    public ResponseEntity<List<StampSummaryResponse>> getStamps(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer semester,
            @RequestParam(required = false) String name,
            HttpServletResponse response) {

        if (year != null) {
            Cookie yearCookie = new Cookie("stamp_filter_year", String.valueOf(year));
            yearCookie.setPath("/");
            yearCookie.setMaxAge(COOKIE_MAX_AGE);
            response.addCookie(yearCookie);
        }

        if (semester != null) {
            Cookie semCookie = new Cookie("stamp_filter_semester", String.valueOf(semester));
            semCookie.setPath("/");
            semCookie.setMaxAge(COOKIE_MAX_AGE);
            response.addCookie(semCookie);
        }

        return ResponseEntity.ok(stampService.getStampList(year, semester, name));
    }

    @GetMapping("/member/stamps")
    public ResponseEntity<List<MemberStampResponse>> getMemberStamps(
            @RequestParam String name,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer semester) {

        return ResponseEntity.ok(stampService.getMemberStampList(name, year, semester));
    }

    @PostMapping("/stamps/{stampId}/give")
    public ResponseEntity<StampSummaryResponse> giveStamp(
            @PathVariable Long stampId,
            @RequestBody GiveStampRequest request,
            @AuthenticationPrincipal AdminUserDetails adminUserDetails) {

        return ResponseEntity.ok(stampService.giveStamp(stampId, request.stampKind(), adminUserDetails.getAdminId()));
    }
}
