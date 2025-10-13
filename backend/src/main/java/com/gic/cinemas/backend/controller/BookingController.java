package com.gic.cinemas.backend.controller;

import com.gic.cinemas.backend.service.BookingService;
import com.gic.cinemas.common.dto.request.ConfirmBookingRequest;
import com.gic.cinemas.common.dto.request.StartBookingRequest;
import com.gic.cinemas.common.dto.response.ConfirmBookingResponse;
import com.gic.cinemas.common.dto.response.StartBookingResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

  private final BookingService service;

  public BookingController(BookingService service) {
    this.service = service;
  }

  @PostMapping
  public ResponseEntity<StartBookingResponse> startBooking(
      @RequestBody StartBookingRequest request) {
    StartBookingResponse response =
        service.startBooking(
            request.movieTitle(), request.rowCount(), request.seatsPerRow(), request.seatsPerRow());
    return ResponseEntity.ok(response);
  }

  public ResponseEntity<ConfirmBookingResponse> confirmBooking(
      @RequestBody ConfirmBookingRequest request) {
    ConfirmBookingResponse response = service.confirmBooking(request.confirmedSeats());
    return ResponseEntity.ok(response);
  }
}
