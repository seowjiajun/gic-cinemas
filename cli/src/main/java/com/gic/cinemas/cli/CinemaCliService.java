package com.gic.cinemas.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gic.cinemas.common.dto.response.CheckBookingResponse;
import com.gic.cinemas.common.dto.response.ReserveSeatsResponse;
import com.gic.cinemas.common.dto.response.SeatDto;
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

  // --- 1. Create or find a seating config (POST) ---
  public SeatingAvailabilityResponse createOrFetchConfig(
      String movieTitle, int rowCount, int seatsPerRow) throws Exception {
    HttpResponse<String> resp =
        cinemaApiClient.postSeatingConfig(movieTitle, rowCount, seatsPerRow);
    validateResponse(resp, "create or fetch seating config");
    return parse(resp, SeatingAvailabilityResponse.class);
  }

  // --- 2. Get current availability (GET) ---
  public SeatingAvailabilityResponse fetchAvailability(
      String movieTitle, int rowCount, int seatsPerRow) throws Exception {
    HttpResponse<String> resp =
        cinemaApiClient.getSeatingAvailability(movieTitle, rowCount, seatsPerRow);
    validateResponse(resp, "fetch seat availability");
    return parse(resp, SeatingAvailabilityResponse.class);
  }

  // --- 3. Reserve seats (POST) ---
  public ReserveSeatsResponse reserveSeats(
      String movieTitle, int rowCount, int seatsPerRow, int tickets) throws Exception {
    HttpResponse<String> resp =
        cinemaApiClient.postReserveBooking(movieTitle, rowCount, seatsPerRow, tickets);
    validateResponse(resp, "reserve seats");
    return parse(resp, ReserveSeatsResponse.class);
  }

  // --- 4. Change seat selection (POST) ---
  public ReserveSeatsResponse changeSeats(String bookingId, SeatDto startSeat) throws Exception {
    HttpResponse<String> resp = cinemaApiClient.postChangeBooking(bookingId, startSeat);
    validateResponse(resp, "change seat selection");
    return parse(resp, ReserveSeatsResponse.class);
  }

  // --- 5. Confirm booking (POST) ---
  public void confirmBooking(String bookingId) throws Exception {
    HttpResponse<String> resp = cinemaApiClient.postConfirmBooking(bookingId);
    validateResponse(resp, "confirm booking");
  }

  // --- 6. Check bookings (GET) ---
  public CheckBookingResponse getBookings(String bookingId) throws Exception {
    HttpResponse<String> resp = cinemaApiClient.getBookingById(bookingId);
    validateResponse(resp, "get bookings");
    return parse(resp, CheckBookingResponse.class);
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
