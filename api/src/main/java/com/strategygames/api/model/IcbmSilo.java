package com.strategygames.api.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "icbm_silos")
public class IcbmSilo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nation_id", nullable = false, length = 64, unique = true)
    private String nationId;

    @Column(nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private SiloState state = SiloState.UNBUILT;

    @Column(name = "state_updated_at")
    private Instant stateUpdatedAt;

    @Column(name = "ready_at")
    private Instant readyAt;

    public enum SiloState {
        UNBUILT,
        BUILDING,
        LOADING,
        READY,
        LAUNCHING,
        RELOADING
    }

    public Long getId() { return id; }

    public String getNationId() { return nationId; }
    public void setNationId(String nationId) { this.nationId = nationId; }

    public SiloState getState() { return state; }
    public void setState(SiloState state) {
        this.state = state;
        this.stateUpdatedAt = Instant.now();
    }

    public Instant getStateUpdatedAt() { return stateUpdatedAt; }

    public Instant getReadyAt() { return readyAt; }
    public void setReadyAt(Instant readyAt) { this.readyAt = readyAt; }
}
