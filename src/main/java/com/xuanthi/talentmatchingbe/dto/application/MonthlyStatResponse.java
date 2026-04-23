package com.xuanthi.talentmatchingbe.dto.application;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO cho thống kê theo tháng
 * Sử dụng cho biểu đồ thống kê
 */
@Data
@AllArgsConstructor
public class MonthlyStatResponse {

    /**
     * Tháng (format: YYYY-MM)
     */
    private String month;

    /**
     * Số lượng
     */
    private long count;
}