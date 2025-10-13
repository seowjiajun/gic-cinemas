package com.gic.cinemas.common.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.gic.cinemas.common.dto.response.SeatDto;
import java.util.List;

public record ConfirmBookingRequest(List<SeatDto> confirmedSeats) {
  @JsonCreator
  public ConfirmBookingRequest {}
}
