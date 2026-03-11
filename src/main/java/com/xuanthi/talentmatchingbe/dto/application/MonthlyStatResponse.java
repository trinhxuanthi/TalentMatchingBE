package com.xuanthi.talentmatchingbe.dto.application;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MonthlyStatResponse {
    private String month;
    private long count;
}