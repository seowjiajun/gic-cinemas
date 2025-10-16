package com.gic.cinemas.backend.controller;

import com.gic.cinemas.backend.exception.NoAvailableSeatsException;
import com.gic.cinemas.backend.exception.SeatJustTakenException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(NoAvailableSeatsException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Map<String, Object> handleNoAvailableSeats(NoAvailableSeatsException e) {
    return buildErrorResponse(HttpStatus.BAD_REQUEST, "No Available Seats", e.getMessage());
  }

  @ExceptionHandler(SeatJustTakenException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public Map<String, Object> handleSeatJustTaken(SeatJustTakenException e) {
    return buildErrorResponse(HttpStatus.CONFLICT, "Seat Just Taken", e.getMessage());
  }

  private Map<String, Object> buildErrorResponse(HttpStatus status, String error, String message) {
    return Map.of("status", status.value(), "error", error, "message", message);
  }
}
