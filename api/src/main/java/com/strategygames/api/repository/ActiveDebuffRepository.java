package com.strategygames.api.repository;

import com.strategygames.api.model.ActiveDebuff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface ActiveDebuffRepository extends JpaRepository<ActiveDebuff, Long> {

    List<ActiveDebuff> findByNationIdAndExpiresAtAfter(String nationId, Instant now);

    List<ActiveDebuff> findByExpiresAtBefore(Instant now);

    void deleteByNationIdAndDebuffType(String nationId, String debuffType);
}
