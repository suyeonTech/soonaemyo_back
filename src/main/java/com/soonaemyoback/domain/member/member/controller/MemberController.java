package com.soonaemyoback.domain.member.member.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.soonaemyoback.domain.member.member.dto.MemberBatchCreateRequest;
import com.soonaemyoback.domain.member.member.dto.MemberResponse;
import com.soonaemyoback.domain.member.member.service.MemberService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/members")
    public ResponseEntity<List<MemberResponse>> registerMembers(@RequestBody MemberBatchCreateRequest request) {
        return ResponseEntity.status(201).body(memberService.registerMembers(request));
    }
}
