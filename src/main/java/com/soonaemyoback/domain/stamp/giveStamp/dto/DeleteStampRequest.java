package com.soonaemyoback.domain.stamp.giveStamp.dto;

import com.soonaemyoback.domain.stamp.stamp.entity.StampKind;

public record DeleteStampRequest(Long memberId, StampKind stampKind, Integer year, Integer semester) {}
