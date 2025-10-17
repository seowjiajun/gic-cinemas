package com.gic.cinemas.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gic.cinemas.backend.exception.BookingNotFoundException;
import com.gic.cinemas.backend.exception.InvalidStartSeatException;
import com.gic.cinemas.backend.exception.NoAvailableSeatsException;
import com.gic.cinemas.backend.exception.SeatJustTakenException;
import com.gic.cinemas.common.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/** Handles all domain exceptions and guarantees a JSON body. */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  private final ObjectMapper mapper = new ObjectMapper();

  // ---------------------------------------
  // Exception handlers
  // ---------------------------------------

  @ExceptionHandler(NoAvailableSeatsException.class)
  public ResponseEntity<String> handleNoAvailableSeats(
      NoAvailableSeatsException e, WebRequest request) {
    return buildJsonResponse(HttpStatus.BAD_REQUEST, "No Available Seats", e.getMessage());
  }

  @ExceptionHandler(SeatJustTakenException.class)
  public ResponseEntity<String> handleSeatJustTaken(SeatJustTakenException e, WebRequest request) {
    return buildJsonResponse(HttpStatus.CONFLICT, "Seat Just Taken", e.getMessage());
  }

  @ExceptionHandler(BookingNotFoundException.class)
  public ResponseEntity<String> handleBookingNotFound(
      BookingNotFoundException e, WebRequest request) {
    return buildJsonResponse(HttpStatus.NOT_FOUND, "Booking Not Found", e.getMessage());
  }

  @ExceptionHandler(InvalidStartSeatException.class)
  public ResponseEntity<String> handleInvalidStartSeat(
      InvalidStartSeatException e, WebRequest request) {
    return buildJsonResponse(HttpStatus.BAD_REQUEST, "Invalid Start Seat", e.getMessage());
  }

  // ---------------------------------------
  // Utility
  // ---------------------------------------

  /**
   * Forces JSON response serialization by returning a raw JSON string body. This bypasses Jackson’s
   * ambiguity between Object vs record serialization under certain slices.
   */
  private ResponseEntity<String> buildJsonResponse(
      HttpStatus status, String error, String message) {
    try {
      ErrorResponse body = new ErrorResponse(status.value(), error, message);
      String json = mapper.writeValueAsString(body);

      return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(json);
    } catch (Exception ex) {
      String fallback =
          String.format(
              "{\"status\":%d,\"error\":\"%s\",\"message\":\"%s\"}",
              status.value(), error, message);
      return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(fallback);
    }
  }
}
