package com.soonaemyoback.domain.stamp.stamp.dto;

import com.soonaemyoback.domain.stamp.stamp.entity.Stamp;

public record MemberStampResponse(Integer year, Integer semester, Integer feedStampNum, Integer exStampNum) {

    public static MemberStampResponse from(Stamp stamp) {
        return new MemberStampResponse(
                stamp.getYear(), stamp.getSemester(), stamp.getFeedStampNum(), stamp.getExStampNum());
    }
}
