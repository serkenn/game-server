package com.strategygames.api.dto;

import jakarta.validation.constraints.NotBlank;

public record ResearchStartRequest(
    @NotBlank String nationId,
    @NotBlank String technologyId,
    @NotBlank String playerUuid
) {}
