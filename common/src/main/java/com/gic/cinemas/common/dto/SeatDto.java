package com.gic.cinemas.common.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record SeatDto(@JsonProperty("code") String code) {
  @JsonCreator
  public SeatDto {}

  @Override
  public String toString() {
    return code;
  }
}
