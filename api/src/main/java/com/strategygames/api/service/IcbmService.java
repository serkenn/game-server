package com.strategygames.api.service;

import com.strategygames.api.model.ActiveDebuff;
import com.strategygames.api.model.IcbmSilo;
import com.strategygames.api.model.IcbmSilo.SiloState;
import com.strategygames.api.model.ResearchProgress.ResearchStatus;
import com.strategygames.api.repository.ActiveDebuffRepository;
import com.strategygames.api.repository.IcbmSiloRepository;
import com.strategygames.api.repository.ResearchProgressRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class IcbmService {

    private static final Logger log = LoggerFactory.getLogger(IcbmService.class);

    // ICBM debuff parameters (from doc: 非破壊デバフ)
    private static final long DEBUFF_DURATION_HOURS = 72;
    private static final Map<String, Double> DEBUFF_MAGNITUDES = Map.of(
        "PRODUCTION_PENALTY", 0.30,
        "MORALE_PENALTY",     0.40,
        "COMMS_DISRUPTION",   1.0,   // binary
        "INFRA_PENALTY",      0.25
    );
    private static final long RELOAD_TIME_DAYS = 2;
    private static final long BUILD_TIME_DAYS  = 7;

    private final IcbmSiloRepository siloRepo;
    private final ResearchProgressRepository researchRepo;
    private final ActiveDebuffRepository debuffRepo;
    private final RconService rconService;

    public IcbmService(IcbmSiloRepository siloRepo,
                       ResearchProgressRepository researchRepo,
                       ActiveDebuffRepository debuffRepo,
                       RconService rconService) {
        this.siloRepo = siloRepo;
        this.researchRepo = researchRepo;
        this.debuffRepo = debuffRepo;
        this.rconService = rconService;
    }

    /**
     * Starts ICBM silo construction for a nation.
     * Requires T3_ICBM technology to be researched.
     */
    @Transactional
    public IcbmSilo startConstruction(String nationId) {
        requireTechnology(nationId, "T3_ICBM");

        IcbmSilo silo = siloRepo.findByNationId(nationId)
                .orElseGet(() -> { IcbmSilo s = new IcbmSilo(); s.setNationId(nationId); return s; });

        if (silo.getState() != SiloState.UNBUILT) {
            throw new IllegalStateException("Silo already exists for nation: " + nationId);
        }
        silo.setState(SiloState.BUILDING);
        silo.setReadyAt(Instant.now().plus(BUILD_TIME_DAYS, ChronoUnit.DAYS));
        return siloRepo.save(silo);
    }

    /**
     * Launches the ICBM at a target nation.
     */
    @Transactional
    public Map<String, Object> launch(String attackerNationId, String targetNationId, String playerUuid) {
        IcbmSilo silo = siloRepo.findByNationId(attackerNationId)
                .orElseThrow(() -> new IllegalStateException("No ICBM silo for nation: " + attackerNationId));

        if (silo.getState() != SiloState.READY) {
            throw new IllegalStateException("ICBM is not ready. Current state: " + silo.getState());
        }

        silo.setState(SiloState.LAUNCHING);
        siloRepo.save(silo);

        // Intercept roll: 30% base intercept chance
        boolean intercepted = new Random().nextDouble() < 0.30;

        if (!intercepted) {
            applyDebuffs(targetNationId, DEBUFF_MAGNITUDES);
            rconService.sendCommand("broadcast [StrategyGames] ICBM from " + attackerNationId
                    + " has struck " + targetNationId + "!");
        } else {
            // Partial debuff on intercept (production + morale only)
            applyDebuffs(targetNationId, Map.of(
                "PRODUCTION_PENALTY", DEBUFF_MAGNITUDES.get("PRODUCTION_PENALTY"),
                "MORALE_PENALTY",     DEBUFF_MAGNITUDES.get("MORALE_PENALTY")
            ));
            rconService.sendCommand("broadcast [StrategyGames] ICBM from " + attackerNationId
                    + " was partially intercepted by " + targetNationId + "!");
        }

        // Begin reload
        silo.setState(SiloState.RELOADING);
        silo.setReadyAt(Instant.now().plus(RELOAD_TIME_DAYS, ChronoUnit.DAYS));
        siloRepo.save(silo);

        log.info("ICBM launched: attacker={} target={} intercepted={}", attackerNationId, targetNationId, intercepted);
        return Map.of("intercepted", intercepted, "targetNationId", targetNationId);
    }

    /**
     * Returns the silo state for a nation (creates UNBUILT record if missing).
     */
    @Transactional(readOnly = true)
    public IcbmSilo getStatus(String nationId) {
        return siloRepo.findByNationId(nationId).orElseGet(() -> {
            IcbmSilo s = new IcbmSilo();
            s.setNationId(nationId);
            return s;
        });
    }

    /**
     * Scheduler: advances silo states when their timers expire.
     */
    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void processSiloTimers() {
        Instant now = Instant.now();
        siloRepo.findAll().stream()
            .filter(s -> s.getReadyAt() != null && s.getReadyAt().isBefore(now))
            .forEach(s -> {
                SiloState next = switch (s.getState()) {
                    case BUILDING  -> SiloState.LOADING;
                    case RELOADING -> SiloState.READY;
                    default        -> null;
                };
                if (next != null) {
                    log.info("Silo state transition: nation={} {} -> {}", s.getNationId(), s.getState(), next);
                    s.setState(next);
                    s.setReadyAt(next == SiloState.LOADING
                            ? Instant.now().plus(1, ChronoUnit.HOURS) // loading takes 1h
                            : null);
                    siloRepo.save(s);
                }
            });
    }

    private void applyDebuffs(String nationId, Map<String, Double> debuffs) {
        Instant now = Instant.now();
        Instant expires = now.plus(DEBUFF_DURATION_HOURS, ChronoUnit.HOURS);
        for (Map.Entry<String, Double> entry : debuffs.entrySet()) {
            ActiveDebuff debuff = new ActiveDebuff();
            debuff.setNationId(nationId);
            debuff.setDebuffType(entry.getKey());
            debuff.setMagnitude(entry.getValue());
            debuff.setSource("ICBM_STRIKE");
            debuff.setAppliedAt(now);
            debuff.setExpiresAt(expires);
            debuffRepo.save(debuff);
        }
    }

    private void requireTechnology(String nationId, String techId) {
        boolean hasIt = researchRepo.existsByNationIdAndTechnologyIdAndStatus(
                nationId, techId, ResearchStatus.COMPLETED);
        if (!hasIt) {
            throw new IllegalStateException("Required technology not researched: " + techId);
        }
    }
}
