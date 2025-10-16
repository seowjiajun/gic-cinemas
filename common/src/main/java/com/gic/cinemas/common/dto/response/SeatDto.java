package com.gic.cinemas.common.dto.response;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record SeatDto(@NotBlank String rowLabel, @Min(1) @Max(50) int seatNumber) {}
