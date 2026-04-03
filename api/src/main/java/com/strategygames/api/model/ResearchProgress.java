package com.strategygames.api.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "research_progress",
       uniqueConstraints = @UniqueConstraint(columnNames = {"nation_id", "technology_id"}))
public class ResearchProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nation_id", nullable = false, length = 64)
    private String nationId;

    @Column(name = "technology_id", nullable = false, length = 64)
    private String technologyId;

    @Column(nullable = false, length = 16)
    @Enumerated(EnumType.STRING)
    private ResearchStatus status = ResearchStatus.IN_PROGRESS;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "completes_at", nullable = false)
    private Instant completesAt;

    @Column(name = "player_uuid", length = 36)
    private String playerUuid;

    public enum ResearchStatus {
        IN_PROGRESS, COMPLETED, CANCELLED
    }

    public Long getId() { return id; }

    public String getNationId() { return nationId; }
    public void setNationId(String nationId) { this.nationId = nationId; }

    public String getTechnologyId() { return technologyId; }
    public void setTechnologyId(String technologyId) { this.technologyId = technologyId; }

    public ResearchStatus getStatus() { return status; }
    public void setStatus(ResearchStatus status) { this.status = status; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public Instant getCompletesAt() { return completesAt; }
    public void setCompletesAt(Instant completesAt) { this.completesAt = completesAt; }

    public String getPlayerUuid() { return playerUuid; }
    public void setPlayerUuid(String playerUuid) { this.playerUuid = playerUuid; }
}
