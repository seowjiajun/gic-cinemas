package com.gic.cinemas.common.dto.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gic.cinemas.common.dto.SeatDto;
import java.util.List;

/** Response returned by the backend after creating or fetching a booking. */
public record BookingResponse(
    @JsonProperty("id") String id,
    @JsonProperty("movie") String movie,
    @JsonProperty("seats") List<SeatDto> seats) {
  @JsonCreator
  public BookingResponse {}
}
