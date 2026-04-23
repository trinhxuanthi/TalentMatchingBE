package com.xuanthi.talentmatchingbe.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "candidate_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nối 1-1 với bảng User
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "standard_title")
    private String standardTitle;

    @Column(name = "skills", columnDefinition = "TEXT")
    private String skills;

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;
}