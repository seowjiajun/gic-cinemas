package com.gic.cinemas.backend.assertions;

import static org.assertj.core.api.Assertions.assertThat;

import com.gic.cinemas.common.dto.SeatDto;
import com.gic.cinemas.common.dto.response.CheckBookingResponse;
import java.util.List;

/**
 * Fluent assertion helper for CheckBookingResponse.
 *
 * <p>Example usage: CheckBookingResponseAssert.assertThatResponse(response)
 * .hasBookingId("GIC0001") .hasBookedSeats(List.of(new SeatDto("A", 1), new SeatDto("A", 2)))
 * .hasTakenSeats(List.of(new SeatDto("A", 3)));
 */
public class CheckBookingResponseAssert {

  private final CheckBookingResponse actual;

  private CheckBookingResponseAssert(CheckBookingResponse actual) {
    this.actual = actual;
  }

  /** Entry point for fluent assertions. */
  public static CheckBookingResponseAssert assertThatCheckBookingResponse(
      CheckBookingResponse actual) {
    return new CheckBookingResponseAssert(actual);
  }

  public CheckBookingResponseAssert hasBookingId(String expected) {
    assertThat(actual.bookingId()).as("bookingId").isEqualTo(expected);
    return this;
  }

  public CheckBookingResponseAssert hasBookedSeats(List<SeatDto> expected) {
    assertThat(actual.bookedSeats())
        .as("bookedSeats")
        .containsExactlyInAnyOrderElementsOf(expected);
    return this;
  }

  public CheckBookingResponseAssert hasTakenSeats(List<SeatDto> expected) {
    assertThat(actual.takenSeats()).as("takenSeats").containsExactlyInAnyOrderElementsOf(expected);
    return this;
  }

  public CheckBookingResponseAssert hasBookedSeatCount(int expectedCount) {
    assertThat(actual.bookedSeats()).as("bookedSeats count").hasSize(expectedCount);
    return this;
  }

  public CheckBookingResponseAssert hasTakenSeatCount(int expectedCount) {
    assertThat(actual.takenSeats()).as("takenSeats count").hasSize(expectedCount);
    return this;
  }

  public CheckBookingResponseAssert hasNoTakenSeats() {
    assertThat(actual.takenSeats()).as("takenSeats should be empty").isEmpty();
    return this;
  }
}
