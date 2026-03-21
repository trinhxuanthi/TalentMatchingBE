package com.xuanthi.talentmatchingbe.repository;

import com.xuanthi.talentmatchingbe.entity.SkillAlias;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SkillAliasRepository extends JpaRepository<SkillAlias, Long> {
    // JpaRepository đã hỗ trợ sẵn findAll()
}