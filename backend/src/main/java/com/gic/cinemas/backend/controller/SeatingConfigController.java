package com.gic.cinemas.backend.controller;

import com.gic.cinemas.backend.service.SeatingConfigService;
import com.gic.cinemas.common.dto.request.SeatingConfigRequest;
import com.gic.cinemas.common.dto.response.SeatingAvailabilityResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seating-config")
public class SeatingConfigController {

  private final SeatingConfigService seatingConfigService;

  public SeatingConfigController(SeatingConfigService seatingConfigService) {
    this.seatingConfigService = seatingConfigService;
  }

  /** Find or create a config with the exact values provided by the CLI. */
  @PostMapping
  public ResponseEntity<SeatingAvailabilityResponse> findOrCreate(
      @RequestBody SeatingConfigRequest req) {
    SeatingAvailabilityResponse res =
        seatingConfigService.findOrCreate(req.title(), req.rows(), req.cols());
    return ResponseEntity.ok(res);
  }
}
