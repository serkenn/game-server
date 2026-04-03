package com.strategygames.api.dto;

import jakarta.validation.constraints.NotBlank;

public record IcbmLaunchRequest(
    @NotBlank String attackerNationId,
    @NotBlank String targetNationId,
    @NotBlank String playerUuid
) {}
