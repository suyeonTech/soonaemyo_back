package com.soonaemyoback.domain.member.member.dto;

import java.util.List;

public record MemberBatchCreateRequest(
        List<MemberCreateRequest> members, Integer currentYear, Integer currentSemester) {}
