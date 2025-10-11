package com.gic.cinemas.common.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** Standard error shape so CLI can print friendly messages. */
public record ApiError(
    @JsonProperty("error") String error,
    @JsonProperty("message") String message,
    @JsonProperty("details") List<String> details) {
  @JsonCreator
  public ApiError {}
}
