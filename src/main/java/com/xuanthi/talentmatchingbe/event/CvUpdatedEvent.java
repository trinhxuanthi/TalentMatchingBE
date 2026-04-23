package com.xuanthi.talentmatchingbe.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Class này đóng vai trò như một "Phong thư" chứa ID của ứng viên
 * để truyền từ luồng chính (Main thread) sang luồng chạy ngầm (Async thread)
 */
@Getter
@AllArgsConstructor
public class CvUpdatedEvent {
    private Long candidateId;
    private String cvUrl;
}