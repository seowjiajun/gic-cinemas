package com.gic.cinemas.backend.exception;

public class SeatAllocationException extends RuntimeException {
  private final int requestedSeats;
  private final int allocatedSeats;

  public SeatAllocationException(int requestedSeats, int allocatedSeats) {
    super(
        "Unable to allocate requested number of seats "
            + "(requested=%d, allocated=%d)".formatted(requestedSeats, allocatedSeats));
    this.requestedSeats = requestedSeats;
    this.allocatedSeats = allocatedSeats;
  }

  public int getRequestedSeats() {
    return requestedSeats;
  }

  public int getAllocatedSeats() {
    return allocatedSeats;
  }
}
