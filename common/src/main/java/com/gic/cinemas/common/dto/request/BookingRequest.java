package com.gic.cinemas.common.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/** Request payload from CLI → Backend when creating a booking. */
public record BookingRequest(
    @NotBlank @JsonProperty("movie") String movie, @Min(1) @JsonProperty("count") int count) {
  @JsonCreator
  public BookingRequest {}
}
