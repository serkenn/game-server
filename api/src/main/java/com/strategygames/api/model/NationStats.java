package com.strategygames.api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "nation_stats")
public class NationStats {

    @Id
    @Column(name = "nation_id", length = 64)
    private String nationId;

    @Column(name = "nation_name", nullable = false, length = 128)
    private String nationName;

    @Column(nullable = false, length = 32)
    private String trait = "NEUTRAL"; // MILITARY, SCIENCE, INDUSTRY, TRADE

    @Column(name = "research_points", nullable = false)
    private int researchPoints = 0;

    @Column(name = "production_modifier", nullable = false)
    private double productionModifier = 1.0;

    @Column(name = "morale", nullable = false)
    private double morale = 100.0;

    @Column(name = "prestige", nullable = false)
    private int prestige = 0;

    @Column(name = "infra_rate", nullable = false)
    private double infraRate = 1.0;

    public String getNationId() { return nationId; }
    public void setNationId(String nationId) { this.nationId = nationId; }

    public String getNationName() { return nationName; }
    public void setNationName(String nationName) { this.nationName = nationName; }

    public String getTrait() { return trait; }
    public void setTrait(String trait) { this.trait = trait; }

    public int getResearchPoints() { return researchPoints; }
    public void setResearchPoints(int researchPoints) { this.researchPoints = researchPoints; }

    public double getProductionModifier() { return productionModifier; }
    public void setProductionModifier(double productionModifier) { this.productionModifier = productionModifier; }

    public double getMorale() { return morale; }
    public void setMorale(double morale) { this.morale = morale; }

    public int getPrestige() { return prestige; }
    public void setPrestige(int prestige) { this.prestige = prestige; }

    public double getInfraRate() { return infraRate; }
    public void setInfraRate(double infraRate) { this.infraRate = infraRate; }
}
