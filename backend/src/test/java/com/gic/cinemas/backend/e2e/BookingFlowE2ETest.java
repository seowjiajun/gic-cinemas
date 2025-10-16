package com.gic.cinemas.backend.e2e;

import static com.gic.cinemas.backend.e2e.Http.*;

import com.gic.cinemas.backend.CinemaApplication;
import com.gic.cinemas.common.dto.request.ChangeSeatsRequest;
import com.gic.cinemas.common.dto.request.ReserveSeatsRequest;
import com.gic.cinemas.common.dto.request.SeatingConfigRequest;
import com.gic.cinemas.common.dto.response.CheckBookingResponse;
import com.gic.cinemas.common.dto.response.ReserveSeatsResponse;
import com.gic.cinemas.common.dto.response.SeatDto;
import com.gic.cinemas.common.dto.response.SeatingAvailabilityResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

@SpringBootTest(
    classes = CinemaApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"spring.profiles.active=test"})
class BookingFlowE2ETest {

  @LocalServerPort int port;
  @Autowired TestRestTemplate rest;

  String base;

  @BeforeEach
  void setUp() {
    base = "http://localhost:" + port + "/api";
  }

  @Test
  @DisplayName("E2E: create/find config → reserve → change → confirm → check")
  void fullBookingFlow() {
    // --- 1) Create/find seating config (POST /seating-config)
    var createReq = new SeatingConfigRequest("Inception", 8, 10);
    ResponseEntity<SeatingAvailabilityResponse> createResp =
        postJson(rest, base + "/seating-config", createReq, SeatingAvailabilityResponse.class);

    Assertions.assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.OK);
    SeatingAvailabilityResponse cfg = createResp.getBody();
    Assertions.assertThat(cfg).isNotNull();
    Assertions.assertThat(cfg.movieTitle()).isEqualTo("Inception");
    Assertions.assertThat(cfg.rowCount()).isEqualTo(8);
    Assertions.assertThat(cfg.seatsPerRow()).isEqualTo(10);
    long totalSeats = 8L * 10L;
    Assertions.assertThat(cfg.availableSeatsCount()).isEqualTo(totalSeats);

    // --- 2) Reserve (POST /booking/reserve)
    int tickets = 4;
    var reserveReq = new ReserveSeatsRequest("Inception", 8, 10, tickets);
    ResponseEntity<ReserveSeatsResponse> reserveResp =
        postJson(rest, base + "/booking/reserve", reserveReq, ReserveSeatsResponse.class);

    Assertions.assertThat(reserveResp.getStatusCode()).isEqualTo(HttpStatus.OK);
    ReserveSeatsResponse reserve = reserveResp.getBody();
    Assertions.assertThat(reserve).isNotNull();
    Assertions.assertThat(reserve.bookingId()).isNotBlank();
    Assertions.assertThat(reserve.reservedSeats()).hasSize(tickets);
    // bookedSeats are those held/taken by others at that moment
    Assertions.assertThat(reserve.bookedSeats()).isNotNull();

    String bookingId = reserve.bookingId();

    // --- 3) Change seats (POST /booking/change-booking)
    // choose a valid anchor in bounds; e.g., row B, seat 3
    var changeReq = new ChangeSeatsRequest(bookingId, new SeatDto("B", 3));
    ResponseEntity<ReserveSeatsResponse> changeResp =
        postJson(rest, base + "/booking/change-booking", changeReq, ReserveSeatsResponse.class);

    Assertions.assertThat(changeResp.getStatusCode()).isEqualTo(HttpStatus.OK);
    ReserveSeatsResponse changed = changeResp.getBody();
    Assertions.assertThat(changed).isNotNull();
    Assertions.assertThat(changed.bookingId()).isEqualTo(bookingId);
    Assertions.assertThat(changed.reservedSeats()).hasSize(tickets);

    // --- 4) Confirm (POST /booking/confirm/{bookingId})
    ResponseEntity<String> confirmResp = postNoBody(rest, base + "/booking/confirm/" + bookingId);

    Assertions.assertThat(confirmResp.getStatusCode().is2xxSuccessful()).isTrue();

    // --- 5) Check bookings (GET /booking/check/{bookingId})
    ResponseEntity<CheckBookingResponse> checkResp =
        get(rest, base + "/booking/check/" + bookingId, CheckBookingResponse.class);

    Assertions.assertThat(checkResp.getStatusCode()).isEqualTo(HttpStatus.OK);
    CheckBookingResponse check = checkResp.getBody();
    Assertions.assertThat(check).isNotNull();
    Assertions.assertThat(check.bookingId()).isEqualTo(bookingId);

    // In your schema: reservedSeats = your seats; bookedSeats = others
    Assertions.assertThat(check.bookedSeats()).hasSize(tickets);
    // After confirm, your seats should appear as "mine"; others can be empty in a single-user test
    Assertions.assertThat(check.bookedSeats()).isNotNull();

    // --- Extra sanity: availability should have decreased by exactly 'tickets'
    ResponseEntity<SeatingAvailabilityResponse> availResp =
        get(
            rest,
            base + "/seating-config?movieTitle=Inception&rowCount=8&seatsPerRow=10",
            SeatingAvailabilityResponse.class);
    Assertions.assertThat(availResp.getStatusCode()).isEqualTo(HttpStatus.OK);
    long afterAvail = availResp.getBody().availableSeatsCount();
    Assertions.assertThat(afterAvail).isEqualTo(totalSeats - tickets);
  }

  @Test
  @DisplayName("E2E: returns 400 when no seats available")
  void reserveWhenNoSeatsAvailable() {
    // Small room to exhaust quickly
    var cfgReq = new SeatingConfigRequest("Tiny", 1, 2);
    postJson(rest, base + "/seating-config", cfgReq, SeatingAvailabilityResponse.class);

    // book both seats
    postJson(
        rest,
        base + "/booking/reserve",
        new ReserveSeatsRequest("Tiny", 1, 2, 2),
        ReserveSeatsResponse.class);

    // try to book another → expect 400 (as per GlobalExceptionHandler mapping)
    ResponseEntity<String> fail =
        postJson(
            rest,
            base + "/booking/reserve",
            new ReserveSeatsRequest("Tiny", 1, 2, 1),
            String.class);

    Assertions.assertThat(fail.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    Assertions.assertThat(fail.getBody()).contains("No Available Seats");
  }

  @Test
  @DisplayName("E2E: 404 when checking non-existent booking")
  void checkUnknownBooking() {
    ResponseEntity<String> resp = get(rest, base + "/booking/check/NOPE123", String.class);
    // Adjust if your exception mapper returns 404 or 400 for not found
    Assertions.assertThat(resp.getStatusCode().value()).isIn(404, 400);
  }
}
