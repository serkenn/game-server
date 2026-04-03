-- =============================================================
-- Strategy Games DB schema – V1 initial
-- DB: strategy_games_debug (debug) / strategy_games (prod)
-- Engine: MariaDB 11
-- =============================================================

-- ── Nation stats ──────────────────────────────────────────────
CREATE TABLE nation_stats (
    nation_id          VARCHAR(64)    NOT NULL,
    nation_name        VARCHAR(128)   NOT NULL,
    trait              VARCHAR(32)    NOT NULL DEFAULT 'NEUTRAL',
                       -- NEUTRAL | MILITARY | SCIENCE | INDUSTRY | TRADE
    research_points    INT            NOT NULL DEFAULT 0,
    production_modifier DOUBLE        NOT NULL DEFAULT 1.0,
    morale             DOUBLE         NOT NULL DEFAULT 100.0,
    prestige           INT            NOT NULL DEFAULT 0,
    infra_rate         DOUBLE         NOT NULL DEFAULT 1.0,
    PRIMARY KEY (nation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── Technology master data ────────────────────────────────────
CREATE TABLE technologies (
    id                      VARCHAR(64)     NOT NULL,
    name                    VARCHAR(128)    NOT NULL,
    category                VARCHAR(16)     NOT NULL,
                            -- MILITARY | SCIENCE | INDUSTRY | ECONOMY | DIPLOMACY
    tier                    INT             NOT NULL,
    research_time_seconds   BIGINT          NOT NULL,
    research_cost           INT             NOT NULL,
    weight_modifier         DOUBLE          NOT NULL DEFAULT 1.0,
    description             VARCHAR(512),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Technology prerequisites (many-to-many, self-ref)
CREATE TABLE technology_prerequisites (
    technology_id    VARCHAR(64)  NOT NULL,
    prerequisite_id  VARCHAR(64)  NOT NULL,
    PRIMARY KEY (technology_id, prerequisite_id),
    CONSTRAINT fk_tp_tech FOREIGN KEY (technology_id)
        REFERENCES technologies(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── Research progress ─────────────────────────────────────────
CREATE TABLE research_progress (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    nation_id       VARCHAR(64)     NOT NULL,
    technology_id   VARCHAR(64)     NOT NULL,
    status          VARCHAR(16)     NOT NULL DEFAULT 'IN_PROGRESS',
                    -- IN_PROGRESS | COMPLETED | CANCELLED
    started_at      DATETIME(3)     NOT NULL,
    completes_at    DATETIME(3)     NOT NULL,
    player_uuid     VARCHAR(36),
    PRIMARY KEY (id),
    UNIQUE KEY uq_rp_nation_tech (nation_id, technology_id),
    KEY idx_rp_status_completes (status, completes_at),
    CONSTRAINT fk_rp_nation FOREIGN KEY (nation_id)
        REFERENCES nation_stats(nation_id) ON DELETE CASCADE,
    CONSTRAINT fk_rp_tech   FOREIGN KEY (technology_id)
        REFERENCES technologies(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── ICBM silos ────────────────────────────────────────────────
CREATE TABLE icbm_silos (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    nation_id           VARCHAR(64)     NOT NULL,
    state               VARCHAR(32)     NOT NULL DEFAULT 'UNBUILT',
                        -- UNBUILT | BUILDING | LOADING | READY | LAUNCHING | RELOADING
    state_updated_at    DATETIME(3),
    ready_at            DATETIME(3),
    PRIMARY KEY (id),
    UNIQUE KEY uq_icbm_nation (nation_id),
    CONSTRAINT fk_icbm_nation FOREIGN KEY (nation_id)
        REFERENCES nation_stats(nation_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── Active debuffs ────────────────────────────────────────────
CREATE TABLE active_debuffs (
    id           BIGINT          NOT NULL AUTO_INCREMENT,
    nation_id    VARCHAR(64)     NOT NULL,
    debuff_type  VARCHAR(64)     NOT NULL,
                 -- PRODUCTION_PENALTY | MORALE_PENALTY | COMMS_DISRUPTION | INFRA_PENALTY
    magnitude    DOUBLE          NOT NULL,
    source       VARCHAR(64),
    applied_at   DATETIME(3)     NOT NULL,
    expires_at   DATETIME(3)     NOT NULL,
    PRIMARY KEY (id),
    KEY idx_debuff_nation_expires (nation_id, expires_at),
    KEY idx_debuff_expires (expires_at),
    CONSTRAINT fk_debuff_nation FOREIGN KEY (nation_id)
        REFERENCES nation_stats(nation_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── Decision executions (cooldown tracking) ───────────────────
CREATE TABLE decision_executions (
    id               BIGINT          NOT NULL AUTO_INCREMENT,
    nation_id        VARCHAR(64)     NOT NULL,
    decision_id      VARCHAR(64)     NOT NULL,
    executed_at      DATETIME(3)     NOT NULL,
    cooldown_ends_at DATETIME(3)     NOT NULL,
    effect_ends_at   DATETIME(3),
    player_uuid      VARCHAR(36),
    PRIMARY KEY (id),
    UNIQUE KEY uq_de_nation_decision (nation_id, decision_id),
    KEY idx_de_cooldown (nation_id, decision_id, cooldown_ends_at),
    CONSTRAINT fk_de_nation FOREIGN KEY (nation_id)
        REFERENCES nation_stats(nation_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
