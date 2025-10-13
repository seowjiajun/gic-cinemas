package com.gic.cinemas.common.dto.response;

import com.fasterxml.jackson.annotation.JsonCreator;

public record SeatingAvailabilityResponse(
    Long seatingConfigId,
    String movieTitle,
    int rowCount,
    int seatsPerRow,
    long availableSeatsCount) {
  @JsonCreator
  public SeatingAvailabilityResponse {}
}
