package com.xuanthi.talentmatchingbe.service;

import com.xuanthi.talentmatchingbe.dto.job.JobMatchResponse;
import com.xuanthi.talentmatchingbe.entity.JobMatch;
import com.xuanthi.talentmatchingbe.repository.JobMatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobMatchService {

    private final JobMatchRepository jobMatchRepository;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * 1. ỨNG VIÊN: Lấy danh sách Job phù hợp nhất với mình
     */
    public List<JobMatchResponse.ForCandidate> getRecommendedJobs(Long candidateId) {
        // Lấy từ DB, Repository đã sắp xếp sẵn điểm từ cao xuống thấp
        List<JobMatch> matches = jobMatchRepository.findByCandidateIdOrderByMatchScoreDesc(candidateId);

        return matches.stream().map(match -> JobMatchResponse.ForCandidate.builder()
                .matchId(match.getId())
                .jobId(match.getJob().getId())
                .jobTitle(match.getJob().getTitle())
                .matchScore(match.getMatchScore())
                .skillScore(match.getSkillScore())
                .titleScore(match.getTitleScore())
                .expScore(match.getExpScore())
                .matchedAt(match.getUpdatedAt().format(formatter))
                .build()
        ).collect(Collectors.toList());
    }
}