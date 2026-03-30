package com.xuanthi.talentmatchingbe.dto.ai;


import lombok.Data;
import java.util.List;

@Data
public class QuickMatchRequest {
    private List<String> cvUrls;
    private String jdUrl;
    private String jdText;
    private String customRules;
    private Long jobId;
}