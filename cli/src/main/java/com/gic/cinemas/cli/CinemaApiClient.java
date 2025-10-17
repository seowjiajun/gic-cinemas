package com.gic.cinemas.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gic.cinemas.common.dto.SeatDto;
import com.gic.cinemas.common.dto.request.ChangeSeatsRequest;
import com.gic.cinemas.common.dto.request.ReserveSeatsRequest;
import com.gic.cinemas.common.dto.request.SeatingConfigRequest;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.*;
import java.nio.charset.StandardCharsets;

public class CinemaApiClient {

  private final HttpClient client = HttpClient.newHttpClient();
  private final ObjectMapper objectMapper;
  private final String baseUrl;

  public CinemaApiClient(String baseUrl, ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
  }

  // ==== SEATING CONFIGURATION ====

  /** POST /seating-config — Find or create a seating configuration */
  public HttpResponse<String> postSeatingConfig(String movieTitle, int rowCount, int seatsPerRow)
      throws IOException, InterruptedException {
    SeatingConfigRequest dto = new SeatingConfigRequest(movieTitle, rowCount, seatsPerRow);
    return post("/seating-config", dto);
  }

  /** GET /seating-config — Check availability (query params) */
  public HttpResponse<String> getSeatingAvailability(
      String movieTitle, int rowCount, int seatsPerRow) throws IOException, InterruptedException {
    String query =
        String.format(
            "?movieTitle=%s&rowCount=%d&seatsPerRow=%d",
            URLEncoder.encode(movieTitle, StandardCharsets.UTF_8), rowCount, seatsPerRow);
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/seating-config" + query))
            .header("Accept", "application/json")
            .GET()
            .build();
    return client.send(request, HttpResponse.BodyHandlers.ofString());
  }

  // ==== BOOKINGS ====

  /** POST /booking/reserve — Reserve seats */
  public HttpResponse<String> postReserveBooking(
      String movieTitle, int rowCount, int seatsPerRow, int numberOfTickets)
      throws IOException, InterruptedException {
    ReserveSeatsRequest dto =
        new ReserveSeatsRequest(movieTitle, rowCount, seatsPerRow, numberOfTickets);
    return post("/booking/reserve", dto);
  }

  /** POST /booking/confirm/{bookingId} — Confirm a pending booking */
  public HttpResponse<String> postConfirmBooking(String bookingId)
      throws IOException, InterruptedException {
    return post("/booking/confirm/" + bookingId, null);
  }

  /** POST /booking/change-booking — Change reserved seats */
  public HttpResponse<String> postChangeBooking(String bookingId, SeatDto startSeat)
      throws IOException, InterruptedException {
    ChangeSeatsRequest dto = new ChangeSeatsRequest(bookingId, startSeat);
    return post("/booking/change-booking", dto);
  }

  /** GET /booking/check/{bookingId} — Check current booking and seat map */
  public HttpResponse<String> getBookingById(String bookingId)
      throws IOException, InterruptedException {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/booking/check/" + bookingId))
            .GET()
            .header("Accept", "application/json")
            .build();
    return client.send(request, HttpResponse.BodyHandlers.ofString());
  }

  // ==== INTERNAL HELPERS ====

  private HttpResponse<String> post(String endpoint, Object dto)
      throws IOException, InterruptedException {
    String jsonBody = (dto != null) ? objectMapper.writeValueAsString(dto) : "";
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + endpoint))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
            .build();
    return client.send(request, HttpResponse.BodyHandlers.ofString());
  }
}
