package com.xuanthi.talentmatchingbe.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "skill_aliases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillAlias {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String alias; // Từ viết tắt: js, k8s, aws...

    @Column(nullable = false)
    private String normalizedName; // Từ chuẩn: javascript, kubernetes...
}