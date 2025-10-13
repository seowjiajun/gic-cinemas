package com.gic.cinemas.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gic.cinemas.common.dto.request.StartBookingRequest;
import com.gic.cinemas.common.dto.response.SeatDto;
import com.gic.cinemas.common.dto.response.StartBookingResponse;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

public class CinemaCli {

  private static final HttpClient HTTP = HttpClient.newHttpClient();
  private static final ObjectMapper JSON = new ObjectMapper();
  private static final String BASE_URL =
      System.getProperty("api.base", "http://localhost:8080/api");

  public static void main(String[] args) throws Exception {
    Scanner sc = new Scanner(System.in);
    System.out.println("🎬 Welcome to GIC Cinemas CLI");
    System.out.println("==============================");

    while (true) {
      System.out.println(
          """
                    [1] Create booking
                    [2] View booking by ID
                    [3] Exit
                    """);
      System.out.print("> ");
      String choice = sc.nextLine().trim();

      switch (choice) {
        case "1" -> createBooking(sc);
        case "2" -> getBooking(sc);
        case "3" -> {
          System.out.println("Goodbye!");
          return;
        }
        default -> System.out.println("Invalid option. Try again.");
      }
    }
  }

  private static void createBooking(Scanner sc) {
    try {
      System.out.print("Enter movie title: ");
      String movie = sc.nextLine().trim();

      System.out.print("Number of tickets: ");
      int count = Integer.parseInt(sc.nextLine().trim());

      // For simplicity, no seat selection yet
      StartBookingRequest req = new StartBookingRequest(movie, 1, 1, count);

      String json = JSON.writeValueAsString(req);

      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(URI.create(BASE_URL + "/bookings"))
              .header("Content-Type", "application/json")
              .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
              .build();

      HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 200) {
        StartBookingResponse booking = JSON.readValue(response.body(), StartBookingResponse.class);
        System.out.printf(
            "✅ Booking created! ID: %s | Movie: %s | Seats: %s%n",
            booking.id(), booking.movie(), formatSeats(booking.seats()));
      } else {
        System.out.printf("❌ Failed: %d %s%n", response.statusCode(), response.body());
      }

    } catch (Exception e) {
      System.out.println("⚠️  Error creating booking: " + e.getMessage());
    }
  }

  private static void getBooking(Scanner sc) {
    try {
      System.out.print("Enter booking ID: ");
      String id = sc.nextLine().trim();

      HttpRequest request =
          HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/bookings/" + id)).GET().build();

      HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 200) {
        StartBookingResponse booking = JSON.readValue(response.body(), StartBookingResponse.class);
        System.out.printf(
            "🎟  Booking ID: %s%nMovie: %s%nSeats: %s%n",
            booking.id(), booking.movie(), formatSeats(booking.seats()));
      } else {
        System.out.printf("❌ Booking not found: %d %s%n", response.statusCode(), response.body());
      }

    } catch (Exception e) {
      System.out.println("⚠️  Error retrieving booking: " + e.getMessage());
    }
  }

  private static String formatSeats(List<SeatDto> seats) {
    if (seats == null || seats.isEmpty()) return "(none)";
    //    return String.join(", ", seats.stream().map(SeatDto::code).toList());
    return "";
  }
}
