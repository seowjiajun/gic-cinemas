package com.gic.cinemas.backend.validation;

import org.springframework.stereotype.Component;

@Component
public class SeatingConfigValidator {

  private static final int MAX_ROW_COUNT = 26;
  private static final int MAX_SEATS_PER_ROW = 50;

  public void validate(String movieTitle, int rowCount, int seatsPerRow) {
    if (movieTitle == null || movieTitle.isBlank()) {
      throw new IllegalArgumentException("movie title is required");
    }
    if (rowCount <= 0 || seatsPerRow <= 0) {
      throw new IllegalArgumentException("rowCount and seatsPerRow must be > 0");
    }
    if (rowCount > MAX_ROW_COUNT) {
      throw new IllegalArgumentException("rowCount must not exceed " + MAX_ROW_COUNT);
    }
    if (seatsPerRow > MAX_SEATS_PER_ROW) {
      throw new IllegalArgumentException("seatsPerRow must not exceed " + MAX_SEATS_PER_ROW);
    }
  }
}
