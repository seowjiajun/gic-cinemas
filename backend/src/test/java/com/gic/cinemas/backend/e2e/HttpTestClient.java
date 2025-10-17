package com.gic.cinemas.backend.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gic.cinemas.common.dto.request.*;
import com.gic.cinemas.common.dto.request.ChangeSeatsRequest;
import com.gic.cinemas.common.dto.request.ReserveSeatsRequest;
import com.gic.cinemas.common.dto.request.SeatingConfigRequest;
import com.gic.cinemas.common.dto.response.*;
import com.gic.cinemas.common.dto.response.BookingConfirmedResponse;
import com.gic.cinemas.common.dto.response.CheckBookingResponse;
import com.gic.cinemas.common.dto.response.ErrorResponse;
import com.gic.cinemas.common.dto.response.ReservedSeatsResponse;
import com.gic.cinemas.common.dto.response.SeatingAvailabilityResponse;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

public class HttpTestClient {

  private final TestRestTemplate rest;
  private final String base;
  private final ObjectMapper mapper = new ObjectMapper();

  public HttpTestClient(TestRestTemplate rest, String base) {
    this.rest = rest;
    this.base = base;
  }

  // -----------------------------
  // Core helpers
  // -----------------------------

  public <T> ResponseEntity<T> get(String path, Class<T> type) {
    return rest.getForEntity(base + path, type);
  }

  public <T> ResponseEntity<T> postJson(String path, Object body, Class<T> type) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<Object> entity = new HttpEntity<>(body, headers);
    return rest.exchange(base + path, HttpMethod.POST, entity, type);
  }

  public <T> ResponseEntity<T> postNoBody(String path, Class<T> type) {
    return rest.exchange(base + path, HttpMethod.POST, null, type);
  }

  // -----------------------------
  // Domain-specific endpoints
  // -----------------------------

  public ResponseEntity<SeatingAvailabilityResponse> postSeatingConfigRequest(
      SeatingConfigRequest body) {
    return postJson("/seating-config", body, SeatingAvailabilityResponse.class);
  }

  public ResponseEntity<ReservedSeatsResponse> postReserveSeatsRequest(ReserveSeatsRequest body) {
    return postJson("/booking/reserve", body, ReservedSeatsResponse.class);
  }

  /**
   * Calls /booking/reserve but safely captures and deserializes backend error responses (e.g. 400,
   * 404, 409) into ErrorResponse objects.
   */
  public ResponseEntity<ErrorResponse> postReserveSeatsRequestSafe(ReserveSeatsRequest body) {
    ResponseEntity<String> raw = postJson("/booking/reserve", body, String.class);

    if (raw.getStatusCode().is2xxSuccessful()) {
      // You expected an error but got success — return empty body
      return ResponseEntity.ok().build();
    }

    try {
      ErrorResponse error = mapper.readValue(raw.getBody(), ErrorResponse.class);
      return ResponseEntity.status(raw.getStatusCode()).body(error);
    } catch (Exception ex) {
      return ResponseEntity.status(raw.getStatusCode())
          .body(
              new ErrorResponse(
                  raw.getStatusCode().value(), "Deserialization Error", ex.getMessage()));
    }
  }

  public ResponseEntity<BookingConfirmedResponse> postConfirmBookingRequest(String bookingId) {
    return rest.exchange(
        base + "/booking/confirm/" + bookingId,
        HttpMethod.POST,
        HttpEntity.EMPTY,
        BookingConfirmedResponse.class);
  }

  public ResponseEntity<ReservedSeatsResponse> postChangeBookingRequest(ChangeSeatsRequest body) {
    return postJson("/booking/change-booking", body, ReservedSeatsResponse.class);
  }

  public ResponseEntity<CheckBookingResponse> getCheckBookingRequest(String bookingId) {
    return rest.exchange(
        base + "/booking/check/" + bookingId,
        HttpMethod.GET,
        HttpEntity.EMPTY,
        CheckBookingResponse.class);
  }

  public ResponseEntity<ErrorResponse> getCheckBookingRequestSafe(String bookingId) {
    // Request the raw response as String, so we can read error JSON manually
    ResponseEntity<String> raw =
        rest.exchange(
            base + "/booking/check/" + bookingId, HttpMethod.GET, HttpEntity.EMPTY, String.class);

    // Success (2xx): return empty body (no error)
    if (raw.getStatusCode().is2xxSuccessful()) {
      return ResponseEntity.ok().build();
    }

    // Error (4xx or 5xx): try to parse JSON ErrorResponse
    try {
      ErrorResponse error = mapper.readValue(raw.getBody(), ErrorResponse.class);
      return ResponseEntity.status(raw.getStatusCode()).body(error);
    } catch (Exception ex) {
      return ResponseEntity.status(raw.getStatusCode())
          .body(
              new ErrorResponse(
                  raw.getStatusCode().value(), "Deserialization Error", ex.getMessage()));
    }
  }
}
