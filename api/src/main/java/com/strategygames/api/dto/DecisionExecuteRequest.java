package com.strategygames.api.dto;

import jakarta.validation.constraints.NotBlank;

public record DecisionExecuteRequest(
    @NotBlank String nationId,
    @NotBlank String decisionId,
    @NotBlank String playerUuid,
    String targetNationId  // optional, for decisions targeting another nation
) {}
