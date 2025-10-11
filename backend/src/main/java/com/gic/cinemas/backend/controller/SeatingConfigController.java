package com.gic.cinemas.backend.controller;

import com.gic.cinemas.backend.service.BookingService;
import com.gic.cinemas.common.dto.request.SeatingConfigRequest;
import com.gic.cinemas.common.dto.response.SeatingConfigResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/config")
public class SeatingConfigController {

  private final BookingService service;

  public SeatingConfigController(BookingService service) {
    this.service = service;
  }

  @PostMapping
  public ResponseEntity<SeatingConfigResponse> configure(@RequestBody SeatingConfigRequest req) {
    return ResponseEntity.ok(service.setConfig(req));
  }

  @GetMapping
  public ResponseEntity<SeatingConfigResponse> current() {
    return service.getConfig().map(ResponseEntity::ok).orElse(ResponseEntity.noContent().build());
  }
}
