package com.gic.cinemas.backend.exception;

public class BookingExpiredException extends RuntimeException {
  private final String bookingId;

  public BookingExpiredException(String bookingId) {
    super("Booking has expired: " + bookingId);
    this.bookingId = bookingId;
  }

  public String getBookingId() {
    return bookingId;
  }
}
