package com.gic.cinemas.backend.exception;

public class BookingNotPendingException extends RuntimeException {
  private final String bookingId;

  public BookingNotPendingException(String bookingId) {
    super("Booking is not pending or has already been confirmed: " + bookingId);
    this.bookingId = bookingId;
  }

  public String getBookingId() {
    return bookingId;
  }
}
