package com.gic.cinemas.backend.validation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SeatingConfigValidatorTest {

  private final SeatingConfigValidator validator = new SeatingConfigValidator();

  @Test
  void rejectsBlankTitle() {
    assertThrows(IllegalArgumentException.class, () -> validator.validate("  ", 10, 10));
  }

  @Test
  void rejectsZeroOrNegativeDimensions() {
    assertThrows(IllegalArgumentException.class, () -> validator.validate("Dune", 0, 10));
    assertThrows(IllegalArgumentException.class, () -> validator.validate("Dune", 10, -1));
  }

  @Test
  void rejectsRowsOrColsAboveLimits() {
    assertThrows(IllegalArgumentException.class, () -> validator.validate("Dune", 27, 10));
    assertThrows(IllegalArgumentException.class, () -> validator.validate("Dune", 10, 51));
  }

  @Test
  void acceptsValidInput() {
    assertDoesNotThrow(() -> validator.validate("Dune", 8, 12));
  }
}
