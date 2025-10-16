package com.gic.cinemas.backend.exception;

public class SeatJustTakenException extends RuntimeException {

  public SeatJustTakenException() {
    super("One or more selected seats were just taken by someone else. Please pick again.");
  }
}
