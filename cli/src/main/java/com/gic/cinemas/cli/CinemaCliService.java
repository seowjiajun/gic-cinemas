package com.gic.cinemas.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gic.cinemas.cli.exception.BookingNotFoundCliException;
import com.gic.cinemas.cli.exception.InvalidStartSeatCliException;
import com.gic.cinemas.cli.exception.NoAvailableSeatsCliException;
import com.gic.cinemas.cli.exception.SeatJustTakenCliException;
import com.gic.cinemas.common.dto.SeatDto;
import com.gic.cinemas.common.dto.response.CheckBookingResponse;
import com.gic.cinemas.common.dto.response.ErrorResponse;
import com.gic.cinemas.common.dto.response.ReservedSeatsResponse;
import com.gic.cinemas.common.dto.response.SeatingAvailabilityResponse;
import java.io.IOException;
import java.net.http.HttpResponse;

public class CinemaCliService {

  private final CinemaApiClient cinemaApiClient;
  private final ObjectMapper mapper;

  public CinemaCliService(CinemaApiClient cinemaApiClient, ObjectMapper mapper) {
    this.cinemaApiClient = cinemaApiClient;
    this.mapper = mapper;
  }

  // --- Create or find a seating config (POST) ---
  public SeatingAvailabilityResponse createOrFetchConfig(
      String movieTitle, int rowCount, int seatsPerRow) throws Exception {
    HttpResponse<String> resp =
        cinemaApiClient.postSeatingConfig(movieTitle, rowCount, seatsPerRow);
    validateResponse(resp, "create or fetch seating config");
    return parse(resp, SeatingAvailabilityResponse.class);
  }

  // --- Get current availability (GET) ---
  public SeatingAvailabilityResponse fetchAvailability(
      String movieTitle, int rowCount, int seatsPerRow) throws Exception {
    HttpResponse<String> resp =
        cinemaApiClient.getSeatingAvailability(movieTitle, rowCount, seatsPerRow);
    validateResponse(resp, "fetch seat availability");
    return parse(resp, SeatingAvailabilityResponse.class);
  }

  // --- Reserve seats (POST) ---
  public ReservedSeatsResponse reserveSeats(
      String movieTitle, int rowCount, int seatsPerRow, int tickets) throws Exception {
    HttpResponse<String> resp =
        cinemaApiClient.postReserveBooking(movieTitle, rowCount, seatsPerRow, tickets);
    validateResponse(resp, "reserve seats");
    return parse(resp, ReservedSeatsResponse.class);
  }

  // --- Change seat selection (POST) ---
  public ReservedSeatsResponse changeSeats(String bookingId, SeatDto startSeat) throws Exception {
    HttpResponse<String> resp = cinemaApiClient.postChangeBooking(bookingId, startSeat);
    int status = resp.statusCode();

    if (status == 200) {
      return parse(resp, ReservedSeatsResponse.class);
    }

    ErrorResponse error = new ObjectMapper().readValue(resp.body(), ErrorResponse.class);

    if (status == 400 && "No Available Seats".equals(error.error())) {
      throw new NoAvailableSeatsCliException(error.message());
    }
    if (status == 400 && "Invalid Start Seat".equals(error.error())) {
      throw new InvalidStartSeatCliException(error.message());
    }
    if (status == 409 && "Seat Just Taken".equals(error.error())) {
      throw new SeatJustTakenCliException(error.message());
    }
    if (status == 404 && "Booking Not Found".equals(error.error())) {
      throw new BookingNotFoundCliException(error.message());
    }

    // Fallback: generic runtime error
    throw new RuntimeException(
        "Request failed (" + status + "): " + error.error() + " - " + error.message());
  }

  // --- Confirm booking (POST) ---
  public void confirmBooking(String bookingId) throws Exception {
    HttpResponse<String> resp = cinemaApiClient.postConfirmBooking(bookingId);
    validateResponse(resp, "confirm booking");
  }

  public CheckBookingResponse getBookings(String bookingId) throws Exception {
    HttpResponse<String> resp = cinemaApiClient.getBookingById(bookingId);
    int status = resp.statusCode();

    if (status == 200) {
      return mapper.readValue(resp.body(), CheckBookingResponse.class);
    }

    // Parse backend error JSON body
    ErrorResponse error = mapper.readValue(resp.body(), ErrorResponse.class);

    if (status == 404 && "Booking Not Found".equals(error.error())) {
      throw new BookingNotFoundCliException(error.message());
    }

    throw new RuntimeException(
        "Request failed (" + status + "): " + error.error() + " - " + error.message());
  }

  // --- Internal helpers ---
  private void validateResponse(HttpResponse<String> resp, String action) {
    int code = resp.statusCode();
    if (code / 100 != 2) {
      throw new IllegalStateException(
          "Failed to " + action + " (HTTP " + code + "): " + resp.body());
    }
  }

  private <T> T parse(HttpResponse<String> resp, Class<T> type) throws IOException {
    return mapper.readValue(resp.body(), type);
  }
}
