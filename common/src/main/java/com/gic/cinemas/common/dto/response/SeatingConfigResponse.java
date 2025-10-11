package com.gic.cinemas.common.dto.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record SeatingConfigResponse(
    @JsonProperty("title") String title,
    @JsonProperty("rows") int rows,
    @JsonProperty("cols") int cols) {
  @JsonCreator
  public SeatingConfigResponse {}
}
