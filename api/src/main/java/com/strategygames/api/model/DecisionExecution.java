package com.strategygames.api.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "decision_executions",
       uniqueConstraints = @UniqueConstraint(columnNames = {"nation_id", "decision_id"}))
public class DecisionExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nation_id", nullable = false, length = 64)
    private String nationId;

    @Column(name = "decision_id", nullable = false, length = 64)
    private String decisionId;

    @Column(name = "executed_at", nullable = false)
    private Instant executedAt;

    @Column(name = "cooldown_ends_at", nullable = false)
    private Instant cooldownEndsAt;

    @Column(name = "effect_ends_at")
    private Instant effectEndsAt;

    @Column(name = "player_uuid", length = 36)
    private String playerUuid;

    public Long getId() { return id; }

    public String getNationId() { return nationId; }
    public void setNationId(String nationId) { this.nationId = nationId; }

    public String getDecisionId() { return decisionId; }
    public void setDecisionId(String decisionId) { this.decisionId = decisionId; }

    public Instant getExecutedAt() { return executedAt; }
    public void setExecutedAt(Instant executedAt) { this.executedAt = executedAt; }

    public Instant getCooldownEndsAt() { return cooldownEndsAt; }
    public void setCooldownEndsAt(Instant cooldownEndsAt) { this.cooldownEndsAt = cooldownEndsAt; }

    public Instant getEffectEndsAt() { return effectEndsAt; }
    public void setEffectEndsAt(Instant effectEndsAt) { this.effectEndsAt = effectEndsAt; }

    public String getPlayerUuid() { return playerUuid; }
    public void setPlayerUuid(String playerUuid) { this.playerUuid = playerUuid; }
}
