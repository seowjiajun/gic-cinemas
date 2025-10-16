package com.gic.cinemas.backend.exception;

public class BookingNotFoundException extends RuntimeException {
  private final String bookingId;

  public BookingNotFoundException(String bookingId) {
    super("Booking not found for bookingId: " + bookingId);
    this.bookingId = bookingId;
  }

  public String getBookingId() {
    return bookingId;
  }
}
