package com.xuanthi.talentmatchingbe.service;

import com.xuanthi.talentmatchingbe.entity.SkillAlias;
import com.xuanthi.talentmatchingbe.repository.SkillAliasRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SkillAliasService {

    private final SkillAliasRepository skillAliasRepository;

    @Cacheable("aliases")
    public Map<String, String> getDynamicAliasesMap() {
        return skillAliasRepository.findAll().stream()
                .collect(Collectors.toMap(
                        SkillAlias::getAlias,
                        SkillAlias::getNormalizedName,
                        (existing, replacement) -> existing // Nếu trùng key thì lấy cái đầu tiên
                ));
    }
}