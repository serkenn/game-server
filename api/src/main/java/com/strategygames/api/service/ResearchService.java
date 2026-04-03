package com.strategygames.api.service;

import com.strategygames.api.model.NationStats;
import com.strategygames.api.model.ResearchProgress;
import com.strategygames.api.model.ResearchProgress.ResearchStatus;
import com.strategygames.api.model.Technology;
import com.strategygames.api.repository.NationStatsRepository;
import com.strategygames.api.repository.ResearchProgressRepository;
import com.strategygames.api.repository.TechnologyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ResearchService {

    private static final Logger log = LoggerFactory.getLogger(ResearchService.class);

    // Trait bonus multipliers for each category
    private static final Map<String, Map<String, Double>> TRAIT_BONUSES = Map.of(
        "MILITARY",  Map.of("MILITARY", 1.30, "SCIENCE", 1.0, "INDUSTRY", 1.0),
        "SCIENCE",   Map.of("MILITARY", 1.0, "SCIENCE", 1.30, "INDUSTRY", 1.0),
        "INDUSTRY",  Map.of("MILITARY", 1.0, "SCIENCE", 1.0, "INDUSTRY", 1.30),
        "TRADE",     Map.of("ECONOMY", 1.20, "DIPLOMACY", 1.20)
    );

    private static final int CANDIDATE_COUNT = 4;

    private final TechnologyRepository techRepo;
    private final ResearchProgressRepository progressRepo;
    private final NationStatsRepository nationRepo;
    private final RconService rconService;

    public ResearchService(TechnologyRepository techRepo,
                           ResearchProgressRepository progressRepo,
                           NationStatsRepository nationRepo,
                           RconService rconService) {
        this.techRepo = techRepo;
        this.progressRepo = progressRepo;
        this.nationRepo = nationRepo;
        this.rconService = rconService;
    }

    /**
     * Returns a weighted-random list of researchable technology candidates for the nation.
     */
    @Transactional(readOnly = true)
    public List<Technology> getCandidates(String nationId) {
        NationStats nation = nationRepo.findById(nationId)
                .orElseThrow(() -> new IllegalArgumentException("Nation not found: " + nationId));

        Set<String> completed = progressRepo.findByNationIdAndStatus(nationId, ResearchStatus.COMPLETED)
                .stream().map(ResearchProgress::getTechnologyId).collect(Collectors.toSet());

        List<Technology> all = techRepo.findAll();
        List<Technology> eligible = all.stream()
                .filter(t -> !completed.contains(t.getId()))
                .filter(t -> completed.containsAll(t.getPrerequisiteIds()))
                .collect(Collectors.toList());

        return weightedSample(eligible, nation.getTrait(), CANDIDATE_COUNT);
    }

    /**
     * Starts research on the given technology for the nation.
     */
    @Transactional
    public ResearchProgress startResearch(String nationId, String technologyId, String playerUuid) {
        NationStats nation = nationRepo.findById(nationId)
                .orElseThrow(() -> new IllegalArgumentException("Nation not found: " + nationId));
        Technology tech = techRepo.findById(technologyId)
                .orElseThrow(() -> new IllegalArgumentException("Technology not found: " + technologyId));

        if (progressRepo.existsByNationIdAndTechnologyIdAndStatus(nationId, technologyId, ResearchStatus.COMPLETED)) {
            throw new IllegalStateException("Technology already researched: " + technologyId);
        }
        if (!progressRepo.findByNationIdAndStatus(nationId, ResearchStatus.IN_PROGRESS).isEmpty()) {
            throw new IllegalStateException("Research already in progress for nation: " + nationId);
        }
        if (nation.getResearchPoints() < tech.getResearchCost()) {
            throw new IllegalStateException("Insufficient research points");
        }

        nation.setResearchPoints(nation.getResearchPoints() - tech.getResearchCost());
        nationRepo.save(nation);

        Instant now = Instant.now();
        ResearchProgress progress = new ResearchProgress();
        progress.setNationId(nationId);
        progress.setTechnologyId(technologyId);
        progress.setStatus(ResearchStatus.IN_PROGRESS);
        progress.setStartedAt(now);
        progress.setCompletesAt(now.plusSeconds(tech.getResearchTimeSeconds()));
        progress.setPlayerUuid(playerUuid);
        return progressRepo.save(progress);
    }

    /**
     * Scheduler: checks for completed research every 30 seconds and notifies via RCON.
     */
    @Scheduled(fixedDelay = 30_000)
    @Transactional
    public void processCompletedResearch() {
        List<ResearchProgress> ready = progressRepo.findByStatusAndCompletesAtBefore(
                ResearchStatus.IN_PROGRESS, Instant.now());
        for (ResearchProgress p : ready) {
            p.setStatus(ResearchStatus.COMPLETED);
            progressRepo.save(p);
            log.info("Research completed: nation={} tech={}", p.getNationId(), p.getTechnologyId());
            if (p.getPlayerUuid() != null) {
                rconService.sendCommand(
                    "tell " + p.getPlayerUuid() + " [StrategyGames] Research complete: " + p.getTechnologyId());
            }
        }
    }

    private List<Technology> weightedSample(List<Technology> candidates, String trait, int count) {
        if (candidates.isEmpty()) return List.of();
        Map<String, Double> bonuses = TRAIT_BONUSES.getOrDefault(trait, Map.of());
        Random rng = new Random();
        List<Technology> pool = new ArrayList<>(candidates);
        List<Technology> result = new ArrayList<>();
        int take = Math.min(count, pool.size());

        for (int i = 0; i < take; i++) {
            double[] weights = pool.stream()
                    .mapToDouble(t -> t.getWeightModifier() * bonuses.getOrDefault(t.getCategory(), 1.0))
                    .toArray();
            double total = Arrays.stream(weights).sum();
            double r = rng.nextDouble() * total;
            int idx = 0;
            for (; idx < weights.length - 1; idx++) {
                r -= weights[idx];
                if (r <= 0) break;
            }
            result.add(pool.remove(idx));
        }
        return result;
    }
}
