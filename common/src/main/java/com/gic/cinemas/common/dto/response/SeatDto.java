package com.gic.cinemas.common.dto.response;

import com.fasterxml.jackson.annotation.JsonCreator;

public record SeatDto(String rowLabel, int seatNumber) {
  @JsonCreator
  public SeatDto {}
}
