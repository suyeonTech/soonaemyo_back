package com.soonaemyoback.domain.stamp.stamp.dto;

import com.soonaemyoback.domain.stamp.stamp.entity.Stamp;

public record StampSummaryResponse(
        Long stampId,
        String memberName,
        String studentNum,
        Integer feedStampNum,
        Integer exStampNum,
        Integer presentCount) {

    public static StampSummaryResponse from(Stamp stamp) {
        return new StampSummaryResponse(
                stamp.getId(),
                stamp.getMember().getName(),
                stamp.getMember().getStudentNum(),
                stamp.getFeedStampNum(),
                stamp.getExStampNum(),
                stamp.getPresentCount());
    }
}
