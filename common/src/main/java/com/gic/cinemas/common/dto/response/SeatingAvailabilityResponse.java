package com.gic.cinemas.common.dto.response;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record SeatingAvailabilityResponse(
    @NotBlank String movieTitle,
    @Min(1) @Max(26) int rowCount,
    @Min(1) @Max(50) int seatsPerRow,
    @Min(0) @Max(1300) long availableSeatsCount) {}
