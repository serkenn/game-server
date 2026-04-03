package com.strategygames.api.repository;

import com.strategygames.api.model.DecisionExecution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface DecisionExecutionRepository extends JpaRepository<DecisionExecution, Long> {

    Optional<DecisionExecution> findByNationIdAndDecisionId(String nationId, String decisionId);

    boolean existsByNationIdAndDecisionIdAndCooldownEndsAtAfter(
            String nationId, String decisionId, Instant now);
}
