package com.strategygames.api.model;

import jakarta.persistence.*;
import java.util.Set;

@Entity
@Table(name = "technologies")
public class Technology {

    @Id
    @Column(length = 64)
    private String id;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(nullable = false, length = 16)
    private String category; // MILITARY, SCIENCE, INDUSTRY, ECONOMY, DIPLOMACY

    @Column(nullable = false)
    private int tier;

    @Column(name = "research_time_seconds", nullable = false)
    private long researchTimeSeconds;

    @Column(name = "research_cost", nullable = false)
    private int researchCost;

    @Column(name = "weight_modifier", nullable = false)
    private double weightModifier = 1.0;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "technology_prerequisites", joinColumns = @JoinColumn(name = "technology_id"))
    @Column(name = "prerequisite_id")
    private Set<String> prerequisiteIds;

    @Column(name = "description", length = 512)
    private String description;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getTier() { return tier; }
    public void setTier(int tier) { this.tier = tier; }

    public long getResearchTimeSeconds() { return researchTimeSeconds; }
    public void setResearchTimeSeconds(long researchTimeSeconds) { this.researchTimeSeconds = researchTimeSeconds; }

    public int getResearchCost() { return researchCost; }
    public void setResearchCost(int researchCost) { this.researchCost = researchCost; }

    public double getWeightModifier() { return weightModifier; }
    public void setWeightModifier(double weightModifier) { this.weightModifier = weightModifier; }

    public Set<String> getPrerequisiteIds() { return prerequisiteIds; }
    public void setPrerequisiteIds(Set<String> prerequisiteIds) { this.prerequisiteIds = prerequisiteIds; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
