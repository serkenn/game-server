package com.strategygames.api.service;

import com.strategygames.api.model.ActiveDebuff;
import com.strategygames.api.model.NationStats;
import com.strategygames.api.repository.ActiveDebuffRepository;
import com.strategygames.api.repository.NationStatsRepository;
import com.strategygames.api.repository.ResearchProgressRepository;
import com.strategygames.api.model.ResearchProgress.ResearchStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NationService {

    private final NationStatsRepository nationRepo;
    private final ResearchProgressRepository researchRepo;
    private final ActiveDebuffRepository debuffRepo;

    public NationService(NationStatsRepository nationRepo,
                         ResearchProgressRepository researchRepo,
                         ActiveDebuffRepository debuffRepo) {
        this.nationRepo = nationRepo;
        this.researchRepo = researchRepo;
        this.debuffRepo = debuffRepo;
    }

    /**
     * Returns the current stats for a nation including active debuffs.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getStats(String nationId) {
        NationStats stats = nationRepo.findById(nationId)
                .orElseThrow(() -> new IllegalArgumentException("Nation not found: " + nationId));

        List<String> completedTechs = researchRepo
                .findByNationIdAndStatus(nationId, ResearchStatus.COMPLETED)
                .stream().map(p -> p.getTechnologyId()).toList();

        List<ActiveDebuff> debuffs = debuffRepo.findByNationIdAndExpiresAtAfter(nationId, Instant.now());

        double effectiveProduction = stats.getProductionModifier()
                - debuffs.stream()
                    .filter(d -> "PRODUCTION_PENALTY".equals(d.getDebuffType()))
                    .mapToDouble(ActiveDebuff::getMagnitude).sum();
        double effectiveMorale = stats.getMorale()
                - debuffs.stream()
                    .filter(d -> "MORALE_PENALTY".equals(d.getDebuffType()))
                    .mapToDouble(d -> d.getMagnitude() * 100).sum();

        return Map.of(
            "nationId", stats.getNationId(),
            "nationName", stats.getNationName(),
            "trait", stats.getTrait(),
            "researchPoints", stats.getResearchPoints(),
            "prestige", stats.getPrestige(),
            "effectiveProductionModifier", Math.max(0, effectiveProduction),
            "effectiveMorale", Math.max(0, effectiveMorale),
            "infraRate", stats.getInfraRate(),
            "completedTechnologies", completedTechs,
            "activeDebuffs", debuffs.stream().map(d -> Map.of(
                "type", d.getDebuffType(),
                "magnitude", d.getMagnitude(),
                "expiresAt", d.getExpiresAt()
            )).toList()
        );
    }

    /**
     * Scheduler: cleans up expired debuffs every 5 minutes.
     */
    @Scheduled(fixedDelay = 300_000)
    @Transactional
    public void cleanExpiredDebuffs() {
        List<ActiveDebuff> expired = debuffRepo.findByExpiresAtBefore(Instant.now());
        if (!expired.isEmpty()) {
            debuffRepo.deleteAll(expired);
        }
    }
}
