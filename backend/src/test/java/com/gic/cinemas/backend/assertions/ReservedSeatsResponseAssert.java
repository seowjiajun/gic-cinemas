package com.gic.cinemas.backend.assertions;

import static org.assertj.core.api.Assertions.assertThat;

import com.gic.cinemas.common.dto.SeatDto;
import com.gic.cinemas.common.dto.response.ReservedSeatsResponse;
import java.util.List;

public class ReservedSeatsResponseAssert {

  private final ReservedSeatsResponse actual;

  private ReservedSeatsResponseAssert(ReservedSeatsResponse actual) {
    this.actual = actual;
  }

  public static ReservedSeatsResponseAssert assertThatReservedSeatsResponse(
      ReservedSeatsResponse actual) {
    return new ReservedSeatsResponseAssert(actual);
  }

  public ReservedSeatsResponseAssert hasBookingId(String expected) {
    assertThat(actual.bookingId()).as("bookingId").isEqualTo(expected);
    return this;
  }

  public ReservedSeatsResponseAssert hasTakenSeats(List<SeatDto> expected) {
    assertThat(actual.takenSeats()).as("takenSeats").containsExactlyInAnyOrderElementsOf(expected);
    return this;
  }

  public ReservedSeatsResponseAssert hasReservedSeats(List<SeatDto> expected) {
    assertThat(actual.reservedSeats())
        .as("reservedSeats")
        .containsExactlyInAnyOrderElementsOf(expected);
    return this;
  }

  public ReservedSeatsResponseAssert hasReservedSeatCount(int expectedCount) {
    assertThat(actual.reservedSeats()).as("reservedSeats count").hasSize(expectedCount);
    return this;
  }

  public ReservedSeatsResponseAssert hasTakenSeatCount(int expectedCount) {
    assertThat(actual.takenSeats()).as("takenSeats count").hasSize(expectedCount);
    return this;
  }
}
