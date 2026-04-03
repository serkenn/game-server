package com.strategygames.api.repository;

import com.strategygames.api.model.IcbmSilo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IcbmSiloRepository extends JpaRepository<IcbmSilo, Long> {

    Optional<IcbmSilo> findByNationId(String nationId);
}
