package com.gic.cinemas.common.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record SeatingConfigRequest(
    @NotBlank @JsonProperty("title") String title,
    @Min(1) @Max(26) @JsonProperty("rows") int rows,
    @Min(1) @Max(50) @JsonProperty("cols") int cols) {
  @JsonCreator
  public SeatingConfigRequest {}
}
