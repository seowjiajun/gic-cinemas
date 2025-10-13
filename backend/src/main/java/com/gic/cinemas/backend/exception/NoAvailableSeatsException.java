package com.gic.cinemas.backend.exception;

public class NoAvailableSeatsException extends RuntimeException {
  public NoAvailableSeatsException(String message) {
    super(message);
  }
}
