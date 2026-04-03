package com.strategygames.api.repository;

import com.strategygames.api.model.Technology;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TechnologyRepository extends JpaRepository<Technology, String> {

    List<Technology> findByCategory(String category);

    List<Technology> findByTier(int tier);

    @Query("SELECT t FROM Technology t WHERE t.id NOT IN " +
           "(SELECT rp.technologyId FROM ResearchProgress rp WHERE rp.nationId = :nationId AND rp.status = 'COMPLETED')")
    List<Technology> findUnresearchedByNation(@Param("nationId") String nationId);
}
