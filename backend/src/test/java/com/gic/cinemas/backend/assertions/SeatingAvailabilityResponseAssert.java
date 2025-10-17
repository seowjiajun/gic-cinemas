package com.gic.cinemas.backend.assertions;

import static org.assertj.core.api.Assertions.assertThat;

import com.gic.cinemas.common.dto.response.SeatingAvailabilityResponse;

public class SeatingAvailabilityResponseAssert {

  private final SeatingAvailabilityResponse actual;

  private SeatingAvailabilityResponseAssert(SeatingAvailabilityResponse actual) {
    this.actual = actual;
  }

  public static SeatingAvailabilityResponseAssert assertThatSeatingAvailabilityResponse(
      SeatingAvailabilityResponse actual) {
    return new SeatingAvailabilityResponseAssert(actual);
  }

  public SeatingAvailabilityResponseAssert title(String expected) {
    assertThat(actual.movieTitle()).as("movieTitle").isEqualTo(expected);
    return this;
  }

  public SeatingAvailabilityResponseAssert layout(int expectedRows, int expectedSeatsPerRow) {
    assertThat(actual.rowCount()).as("rowCount").isEqualTo(expectedRows);
    assertThat(actual.seatsPerRow()).as("seatsPerRow").isEqualTo(expectedSeatsPerRow);
    return this;
  }

  public SeatingAvailabilityResponseAssert availableSeats(long expected) {
    assertThat(actual.availableSeatsCount()).as("availableSeatsCount").isEqualTo(expected);
    return this;
  }
}
