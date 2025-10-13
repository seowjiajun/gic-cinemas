package com.gic.cinemas.backend.validation;

import org.springframework.stereotype.Component;

@Component
public class BookingValidator {

  private final SeatingConfigValidator seatingConfigValidator;

  public BookingValidator(SeatingConfigValidator seatingConfigValidator) {
    this.seatingConfigValidator = seatingConfigValidator;
  }

  public void validate(String movieTitle, int rowCount, int seatsPerRow, int numberOfTickets) {
    seatingConfigValidator.validate(movieTitle, rowCount, seatsPerRow);
    if (numberOfTickets <= 0) {
      throw new IllegalArgumentException("numberOfTickets must be > 0");
    }
  }
}
