package com.gic.cinemas.backend.exception;

public class NoAvailableSeatsException extends RuntimeException {
  private final long availableSeatCount;

  public NoAvailableSeatsException(String message, long availableSeatCount) {
    super(message);
    this.availableSeatCount = availableSeatCount;
  }

  public long getAvailableSeatCount() {
    return availableSeatCount;
  }
}
