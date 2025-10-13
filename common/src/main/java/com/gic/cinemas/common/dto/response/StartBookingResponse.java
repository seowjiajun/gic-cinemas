package com.gic.cinemas.common.dto.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.List;

/** Response returned by the backend after creating or fetching a booking. */
public record StartBookingResponse(
    String bookingId, List<SeatDto> bookedSeats, List<SeatDto> reservedSeats) {
  @JsonCreator
  public StartBookingResponse {}
}
