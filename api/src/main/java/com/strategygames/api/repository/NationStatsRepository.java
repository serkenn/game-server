package com.strategygames.api.repository;

import com.strategygames.api.model.NationStats;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NationStatsRepository extends JpaRepository<NationStats, String> {
}
