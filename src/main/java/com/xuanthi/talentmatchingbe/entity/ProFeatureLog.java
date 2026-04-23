package com.xuanthi.talentmatchingbe.entity;

import com.xuanthi.talentmatchingbe.enums.ProFeature;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "pro_feature_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProFeatureLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "feature_name", nullable = false)
    private ProFeature featureName;

    @CreationTimestamp
    @Column(name = "used_at", updatable = false)
    private LocalDateTime usedAt;
}