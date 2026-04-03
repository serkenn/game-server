package com.strategygames.api.service;

import com.strategygames.api.model.ActiveDebuff;
import com.strategygames.api.model.DecisionExecution;
import com.strategygames.api.repository.ActiveDebuffRepository;
import com.strategygames.api.repository.DecisionExecutionRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
public class DecisionService {

    // Decision definitions: id -> {cooldownHours, effectDurationHours, debuffToRemove}
    // Based on doc: 05_decision_system.md
    private static final Map<String, DecisionDef> DECISIONS = Map.ofEntries(
        Map.entry("EMERGENCY_REPAIR",  new DecisionDef(24,  6, "INFRA_PENALTY")),
        Map.entry("MEDICAL_RESPONSE",  new DecisionDef(12,  6, "PRODUCTION_PENALTY")),
        Map.entry("COMMS_REBUILD",     new DecisionDef(48, 12, "COMMS_DISRUPTION")),
        Map.entry("AIR_DEFENSE",       new DecisionDef(72, 24, null)),
        Map.entry("FORTIFY",           new DecisionDef(24, 12, null)),
        Map.entry("MOBILIZE",          new DecisionDef(168, 48, null)),
        Map.entry("INSPIRE_CITIZENS",  new DecisionDef(12,  6, "MORALE_PENALTY")),
        Map.entry("RATIONING",         new DecisionDef(48, 24, null)),
        Map.entry("EMERGENCY_TAX",     new DecisionDef(168, 0, null)),
        Map.entry("SANCTION",          new DecisionDef(168, 72, null)),
        Map.entry("REQUEST_AID",       new DecisionDef(72,  0, null)),
        Map.entry("CEASEFIRE",         new DecisionDef(48,  0, null))
    );

    private final DecisionExecutionRepository executionRepo;
    private final ActiveDebuffRepository debuffRepo;
    private final RconService rconService;

    public DecisionService(DecisionExecutionRepository executionRepo,
                           ActiveDebuffRepository debuffRepo,
                           RconService rconService) {
        this.executionRepo = executionRepo;
        this.debuffRepo = debuffRepo;
        this.rconService = rconService;
    }

    public Map<String, DecisionDef> getAllDecisions() {
        return DECISIONS;
    }

    /**
     * Lists decisions available (not on cooldown) for a nation.
     */
    @Transactional(readOnly = true)
    public List<String> getAvailable(String nationId) {
        Instant now = Instant.now();
        return DECISIONS.keySet().stream()
                .filter(id -> !executionRepo.existsByNationIdAndDecisionIdAndCooldownEndsAtAfter(nationId, id, now))
                .toList();
    }

    /**
     * Executes a decision for the given nation.
     */
    @Transactional
    public DecisionExecution execute(String nationId, String decisionId,
                                     String playerUuid, String targetNationId) {
        DecisionDef def = DECISIONS.get(decisionId);
        if (def == null) {
            throw new IllegalArgumentException("Unknown decision: " + decisionId);
        }

        Instant now = Instant.now();
        if (executionRepo.existsByNationIdAndDecisionIdAndCooldownEndsAtAfter(nationId, decisionId, now)) {
            throw new IllegalStateException("Decision is on cooldown: " + decisionId);
        }

        // Apply effect: remove the associated debuff if present
        if (def.debuffToRemove() != null) {
            debuffRepo.deleteByNationIdAndDebuffType(nationId, def.debuffToRemove());
        }

        // Record execution
        DecisionExecution exec = executionRepo.findByNationIdAndDecisionId(nationId, decisionId)
                .orElseGet(DecisionExecution::new);
        exec.setNationId(nationId);
        exec.setDecisionId(decisionId);
        exec.setExecutedAt(now);
        exec.setCooldownEndsAt(now.plus(def.cooldownHours(), ChronoUnit.HOURS));
        exec.setEffectEndsAt(def.effectDurationHours() > 0
                ? now.plus(def.effectDurationHours(), ChronoUnit.HOURS) : null);
        exec.setPlayerUuid(playerUuid);
        DecisionExecution saved = executionRepo.save(exec);

        rconService.sendCommand("tell " + playerUuid
                + " [StrategyGames] Decision executed: " + decisionId);

        return saved;
    }

    public record DecisionDef(int cooldownHours, int effectDurationHours, String debuffToRemove) {}
}
