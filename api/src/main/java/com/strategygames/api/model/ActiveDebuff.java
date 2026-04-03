package com.strategygames.api.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "active_debuffs")
public class ActiveDebuff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nation_id", nullable = false, length = 64)
    private String nationId;

    @Column(name = "debuff_type", nullable = false, length = 64)
    private String debuffType; // PRODUCTION_PENALTY, MORALE_PENALTY, COMMS_DISRUPTION, INFRA_PENALTY

    @Column(nullable = false)
    private double magnitude;

    @Column(name = "source", length = 64)
    private String source; // e.g., "ICBM_STRIKE"

    @Column(name = "applied_at", nullable = false)
    private Instant appliedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    public Long getId() { return id; }

    public String getNationId() { return nationId; }
    public void setNationId(String nationId) { this.nationId = nationId; }

    public String getDebuffType() { return debuffType; }
    public void setDebuffType(String debuffType) { this.debuffType = debuffType; }

    public double getMagnitude() { return magnitude; }
    public void setMagnitude(double magnitude) { this.magnitude = magnitude; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public Instant getAppliedAt() { return appliedAt; }
    public void setAppliedAt(Instant appliedAt) { this.appliedAt = appliedAt; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
}
