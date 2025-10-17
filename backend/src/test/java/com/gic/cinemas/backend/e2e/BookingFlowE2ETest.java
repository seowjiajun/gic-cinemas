package com.gic.cinemas.backend.e2e;

import static com.gic.cinemas.backend.assertions.BookingConfirmedResponseAssert.assertThatBookingConfirmedResponse;
import static com.gic.cinemas.backend.assertions.CheckBookingResponseAssert.assertThatCheckBookingResponse;
import static com.gic.cinemas.backend.assertions.ErrorResponseAssert.assertThatErrorResponse;
import static com.gic.cinemas.backend.assertions.ReservedSeatsResponseAssert.assertThatReservedSeatsResponse;
import static com.gic.cinemas.backend.assertions.SeatingAvailabilityResponseAssert.assertThatSeatingAvailabilityResponse;
import static com.gic.cinemas.backend.e2e.Http.*;

import com.gic.cinemas.backend.CinemaApplication;
import com.gic.cinemas.common.dto.BookingStatus;
import com.gic.cinemas.common.dto.SeatDto;
import com.gic.cinemas.common.dto.request.ChangeSeatsRequest;
import com.gic.cinemas.common.dto.request.ReserveSeatsRequest;
import com.gic.cinemas.common.dto.request.SeatingConfigRequest;
import com.gic.cinemas.common.dto.response.*;
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
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BookingFlowE2ETest {

  @LocalServerPort int port;
  @Autowired TestRestTemplate rest;
  String base;
  HttpTestClient client;

  @BeforeAll
  void setUp() { // non-static because of @TestInstance(PER_CLASS)
    base = "http://localhost:" + port + "/api";
    client = new HttpTestClient(rest, base);
  }

  @Test
  @DisplayName("E2E: create/find config → reserve → change → confirm → check")
  void fullBookingFlow() {
    String movieTitle = "Inception";
    int rowCount = 8;
    int seatsPerRow = 10;
    int tickets = 4;

    // create seating config
    SeatingConfigRequest seatingConfigRequest =
        new SeatingConfigRequest(movieTitle, rowCount, seatsPerRow);
    ResponseEntity<SeatingAvailabilityResponse> createResp =
        client.postSeatingConfigRequest(seatingConfigRequest);
    Assertions.assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThatSeatingAvailabilityResponse(createResp.getBody())
        .title(movieTitle)
        .layout(rowCount, seatsPerRow)
        .availableSeats(rowCount * seatsPerRow);

    // reserve
    ReserveSeatsRequest reserveSeatsRequest = new ReserveSeatsRequest(movieTitle, 8, 10, tickets);
    ResponseEntity<?> reservedSeatsResponse = client.postReserveSeatsRequest(reserveSeatsRequest);
    Assertions.assertThat(reservedSeatsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    ReservedSeatsResponse reserveSeats = (ReservedSeatsResponse) reservedSeatsResponse.getBody();
    assertThatReservedSeatsResponse(reserveSeats)
        .hasBookingId("GIC0001")
        .hasReservedSeatCount(tickets);

    // change booking
    SeatDto startSeat = new SeatDto("B", 3);
    ChangeSeatsRequest changeReq = new ChangeSeatsRequest(reserveSeats.bookingId(), startSeat);
    ResponseEntity<ReservedSeatsResponse> changeResponse =
        client.postChangeBookingRequest(changeReq);
    Assertions.assertThat(changeResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    ReservedSeatsResponse changedSeats = changeResponse.getBody();
    assertThatReservedSeatsResponse(changedSeats)
        .hasBookingId(reserveSeats.bookingId())
        .hasReservedSeatCount(tickets);

    // confirm
    ResponseEntity<BookingConfirmedResponse> confirmResp =
        client.postConfirmBookingRequest(reserveSeats.bookingId());
    Assertions.assertThat(confirmResp.getStatusCode()).isEqualTo(HttpStatus.OK);
    BookingConfirmedResponse bookingConfirmed = confirmResp.getBody();
    assertThatBookingConfirmedResponse(bookingConfirmed)
        .hasBookingId("GIC0001")
        .hasMovieTitle(movieTitle)
        .hasStatus(BookingStatus.CONFIRMED);

    // check
    ResponseEntity<CheckBookingResponse> checkResp =
        client.getCheckBookingRequest(bookingConfirmed.bookingId());
    Assertions.assertThat(checkResp.getStatusCode()).isEqualTo(HttpStatus.OK);
    CheckBookingResponse checkBooking = checkResp.getBody();
    assertThatCheckBookingResponse(checkBooking)
        .hasBookingId("GIC0001")
        .hasBookedSeatCount(tickets)
        .hasNoTakenSeats();
  }

  @Test
  @DisplayName("E2E: returns 400 when no seats available")
  void reserveWhenNoSeatsAvailable() {
    // create seating config
    SeatingConfigRequest seatingConfigRequest = new SeatingConfigRequest("Inception", 1, 2);
    client.postSeatingConfigRequest(seatingConfigRequest);

    // book all seats
    ReserveSeatsRequest reserveSeatsRequest = new ReserveSeatsRequest("Inception", 1, 2, 2);
    ReservedSeatsResponse reservedSeats =
        client.postReserveSeatsRequest(reserveSeatsRequest).getBody();
    client.postConfirmBookingRequest(reservedSeats.bookingId());

    // book again
    ResponseEntity<ErrorResponse> errorResponse =
        client.postReserveSeatsRequestSafe(reserveSeatsRequest);
    ErrorResponse error = errorResponse.getBody();
    assertThatErrorResponse(error).hasStatus(400).hasError("No Available Seats");
  }

  @Test
  @DisplayName("E2E: check non-existent booking → 404 with error body")
  void checkNonExistentBookingReturns404() {
    // create seating config
    SeatingConfigRequest seatingConfigRequest = new SeatingConfigRequest("Inception", 1, 2);
    client.postSeatingConfigRequest(seatingConfigRequest);

    // send non-existent booking
    ErrorResponse error = client.getCheckBookingRequestSafe("GIC0001").getBody();
    assertThatErrorResponse(error).hasStatus(404).hasError("Booking Not Found");
  }
}
