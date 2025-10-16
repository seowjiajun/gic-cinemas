package com.gic.cinemas.backend.controller;

import com.gic.cinemas.backend.service.SeatingConfigService;
import com.gic.cinemas.common.dto.request.SeatingConfigRequest;
import com.gic.cinemas.common.dto.response.SeatingAvailabilityResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seating-config")
public class SeatingConfigController {

  private final SeatingConfigService seatingConfigService;

  public SeatingConfigController(SeatingConfigService seatingConfigService) {
    this.seatingConfigService = seatingConfigService;
  }

  @PostMapping
  public ResponseEntity<SeatingAvailabilityResponse> findOrCreate(
      @Valid @RequestBody SeatingConfigRequest request) {
    SeatingAvailabilityResponse response =
        seatingConfigService.findOrCreate(
            request.movieTitle(), request.rowCount(), request.seatsPerRow());
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  @GetMapping
  public ResponseEntity<SeatingAvailabilityResponse> getAvailability(
      @RequestParam String movieTitle, @RequestParam int rowCount, @RequestParam int seatsPerRow) {
    SeatingAvailabilityResponse response =
        seatingConfigService.getAvailability(movieTitle, rowCount, seatsPerRow);
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }
}
