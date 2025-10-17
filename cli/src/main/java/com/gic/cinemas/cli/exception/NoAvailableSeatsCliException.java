package com.gic.cinemas.cli.exception;

public class NoAvailableSeatsCliException extends RuntimeException {
  public NoAvailableSeatsCliException(String message) {
    super(message);
  }
}
