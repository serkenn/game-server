package com.strategygames.api.repository;

import com.strategygames.api.model.ResearchProgress;
import com.strategygames.api.model.ResearchProgress.ResearchStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ResearchProgressRepository extends JpaRepository<ResearchProgress, Long> {

    Optional<ResearchProgress> findByNationIdAndTechnologyId(String nationId, String technologyId);

    List<ResearchProgress> findByNationIdAndStatus(String nationId, ResearchStatus status);

    List<ResearchProgress> findByStatusAndCompletesAtBefore(ResearchStatus status, Instant now);

    boolean existsByNationIdAndTechnologyIdAndStatus(String nationId, String technologyId, ResearchStatus status);
}
