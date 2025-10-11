package com.gic.cinemas.backend.validation;

import org.springframework.stereotype.Component;

@Component
public class SeatingConfigValidator {

  private static final int MAX_ROWS = 26;
  private static final int MAX_COLS = 50;

  public void validate(String title, int rows, int cols) {
    if (title == null || title.isBlank()) {
      throw new IllegalArgumentException("title is required");
    }
    if (rows <= 0 || cols <= 0) {
      throw new IllegalArgumentException("rows and cols must be > 0");
    }
    if (rows > MAX_ROWS) {
      throw new IllegalArgumentException("rows must not exceed " + MAX_ROWS);
    }
    if (cols > MAX_COLS) {
      throw new IllegalArgumentException("cols must not exceed " + MAX_COLS);
    }
  }
}
