package com.xuanthi.talentmatchingbe.repository;

import com.xuanthi.talentmatchingbe.entity.AiSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AiSettingRepository extends JpaRepository<AiSetting, Long> {
}