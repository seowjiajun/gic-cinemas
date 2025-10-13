package com.gic.cinemas.common.dto.response;

import com.fasterxml.jackson.annotation.JsonCreator;

public record ConfirmBookingResponse(String bookingId) {
  @JsonCreator
  public ConfirmBookingResponse {}
}
