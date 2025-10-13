package com.gic.cinemas.common.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/** Request payload from CLI → Backend when creating a booking. */
public record StartBookingRequest(
    @NotBlank String movieTitle,
    @Min(1) @Max(26) int rowCount,
    @Min(1) @Max(50) int seatsPerRow,
    @Min(1) int numberOfTickets) {
  @JsonCreator
  public StartBookingRequest {}
}
