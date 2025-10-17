package com.gic.cinemas.common.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ReserveSeatsRequest(
    @NotBlank String movieTitle,
    @Min(1) @Max(26) int rowCount,
    @Min(1) @Max(50) int seatsPerRow,
    @Min(1) int numberOfTickets) {}
