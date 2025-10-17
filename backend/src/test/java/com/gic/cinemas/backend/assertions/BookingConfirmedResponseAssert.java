package com.gic.cinemas.backend.assertions;

import static org.assertj.core.api.Assertions.assertThat;

import com.gic.cinemas.common.dto.BookingStatus;
import com.gic.cinemas.common.dto.response.BookingConfirmedResponse;

public class BookingConfirmedResponseAssert {

  private final BookingConfirmedResponse actual;

  private BookingConfirmedResponseAssert(BookingConfirmedResponse actual) {
    this.actual = actual;
  }

  /** Entry point for assertions. */
  public static BookingConfirmedResponseAssert assertThatBookingConfirmedResponse(
      BookingConfirmedResponse actual) {
    return new BookingConfirmedResponseAssert(actual);
  }

  public BookingConfirmedResponseAssert hasBookingId(String expected) {
    assertThat(actual.bookingId()).as("bookingId").isEqualTo(expected);
    return this;
  }

  public BookingConfirmedResponseAssert hasMovieTitle(String expected) {
    assertThat(actual.movieTitle()).as("movieTitle").isEqualTo(expected);
    return this;
  }

  public BookingConfirmedResponseAssert hasStatus(BookingStatus expected) {
    assertThat(actual.status()).as("status").isEqualTo(expected);
    return this;
  }
}
