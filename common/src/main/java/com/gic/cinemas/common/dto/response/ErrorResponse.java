package com.gic.cinemas.common.dto.response;

import jakarta.validation.constraints.NotBlank;

public record ErrorResponse(int status, @NotBlank String error, @NotBlank String message) {}
