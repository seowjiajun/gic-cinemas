package com.gic.cinemas.backend.exception;

public class NoHeldSeatsException extends RuntimeException {
  private static final String DEFAULT_MESSAGE = "No seats currently held by this booking to change";
  private final int tickets;

  public NoHeldSeatsException(int tickets) {
    super(DEFAULT_MESSAGE);
    this.tickets = tickets;
  }

  public int getTickets() {
    return tickets;
  }
}
