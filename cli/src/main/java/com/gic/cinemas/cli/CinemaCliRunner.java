package com.gic.cinemas.cli;

import com.gic.cinemas.cli.exception.BookingNotFoundCliException;
import com.gic.cinemas.common.dto.SeatDto;
import com.gic.cinemas.common.dto.response.CheckBookingResponse;
import com.gic.cinemas.common.dto.response.ReservedSeatsResponse;
import java.util.Scanner;
import java.util.regex.Pattern;

public class CinemaCliRunner {

  // ==== Dependencies & State ====
  private final Scanner scanner;
  private final CinemaCliService cliService;

  public CinemaCliRunner(Scanner scanner, CinemaCliService cinemaCliService) {
    this.scanner = scanner;
    this.cliService = cinemaCliService;
  }

  // ==== Public Entrypoint ====
  public void run() throws Exception {
    Layout layout = promptLayout();

    // Ensure config exists (POST find-or-create)
    cliService.createOrFetchConfig(layout.movieTitle(), layout.rowCount(), layout.seatsPerRow());

    mainMenuLoop(layout);
  }

  // ==== Menu ====
  private void mainMenuLoop(Layout layout) throws Exception {
    long availableSeatsCount = 0; // last-known value

    while (true) {
      // Refresh availability each loop so the number stays current
      try {
        var latest =
            cliService.fetchAvailability(
                layout.movieTitle(), layout.rowCount(), layout.seatsPerRow());
        availableSeatsCount = latest.availableSeatsCount();
      } catch (Exception e) {
        System.out.println("(couldn't refresh availability: " + e.getMessage() + ")");
      }

      System.out.println();
      System.out.println("Welcome to GIC Cinemas");
      System.out.printf(
          """
                    [1] Book tickets for %s (%d seats available)
                    [2] Check bookings
                    [3] Exit
                    Please enter your selection:
                    """,
          layout.movieTitle(), availableSeatsCount);
      System.out.print("> ");
      String selection = scanner.nextLine().trim();

      switch (selection) {
        case "1" -> handleCreateBooking(layout); // menu will refresh availability on next loop
        case "2" -> handleCheckBooking(layout);
        case "3" -> {
          System.out.println();
          System.out.println("Thank you for using GIC Cinemas system. Bye!");
          return;
        }
        default -> System.out.println("Invalid option. Try again:");
      }
    }
  }

  // ==== Flow: Create/Change/Confirm Booking ====
  private void handleCreateBooking(Layout layout) throws Exception {
    Integer tickets = promptTickets(layout);
    if (tickets == null) return; // back to main

    // Re-check availability just before reserving
    long available =
        cliService
            .fetchAvailability(layout.movieTitle(), layout.rowCount(), layout.seatsPerRow())
            .availableSeatsCount();
    if (available < tickets) {
      System.out.printf("Sorry, there are only %d seats available.%n", available);
      return;
    }

    // Reserve via service
    ReservedSeatsResponse reserve =
        cliService.reserveSeats(
            layout.movieTitle(), layout.rowCount(), layout.seatsPerRow(), tickets);

    renderBookingSnapshot(layout, tickets, reserve);

    // Allow user to adjust selection before confirming
    previewChangeLoop(layout, reserve);
  }

  private void previewChangeLoop(Layout layout, ReservedSeatsResponse snapshot) {
    final String bookingId = snapshot.bookingId();

    while (true) {
      System.out.println();
      System.out.print("Enter blank to accept seat selection, or enter new seating position:\n> ");
      String line = scanner.nextLine().strip();

      if (line.isEmpty()) {
        tryConfirm(bookingId);
        return;
      }

      if (!isValidSeatCode(line, layout.rowCount(), layout.seatsPerRow())) {
        System.out.println("Invalid seat label. Use like B03 (row letter + digits within bounds).");
        continue;
      }

      SeatDto anchor = parseSeatCode(line);
      try {
        snapshot = cliService.changeSeats(bookingId, anchor);
        System.out.printf("%nBooking id: %s%nSelected seats:%n%n", bookingId);
        SeatMapPrinter.print(
            layout.rowCount(),
            layout.seatsPerRow(),
            snapshot.takenSeats(),
            snapshot.reservedSeats());
      } catch (Exception e) {
        System.out.println("Could not update seats. Try another position.");
      }
    }
  }

  private void tryConfirm(String bookingId) {
    try {
      cliService.confirmBooking(bookingId);
      System.out.printf("%nBooking id: %s confirmed.%n%n", bookingId);
    } catch (Exception e) {
      System.out.println("Confirmation failed: " + e.getMessage());
    }
  }

  // ==== Flow: Check Booking ====
  private void handleCheckBooking(Layout layout) {
    while (true) {
      System.out.print("\nEnter booking id, or enter blank to go back to main menu:\n> ");
      String bookingId = scanner.nextLine().trim();
      if (bookingId.isEmpty()) return;

      try {
        CheckBookingResponse dto = cliService.getBookings(bookingId);

        System.out.println("\nBooking id: " + dto.bookingId());
        System.out.println("Selected seats:");
        // takenSeats = others, reservedSeats = mine
        SeatMapPrinter.print(
            layout.rowCount(), layout.seatsPerRow(), dto.takenSeats(), dto.bookedSeats());
      } catch (BookingNotFoundCliException e) {
        System.out.println();
        System.out.println("No booking found for " + bookingId + ".");
      } catch (Exception e) {
        // Service already throws with HTTP status + body; keep UX-friendly here
        System.out.println("Unable to load booking: " + e.getMessage());
      }
    }
  }

  // ==== Prompts & Parsing ====
  private Layout promptLayout() {
    while (true) {
      System.out.println();
      System.out.print(
          "Please define movie title and seating map in [Title] [Row] [SeatsPerRow] format:\n> ");
      String line = scanner.nextLine().trim();
      String[] tokens = line.split("\\s+");

      if (tokens.length < 3) {
        System.out.println("Invalid input. Please enter: [Title] [Rows] [SeatsPerRow].");
        continue;
      }

      String movieTitle = tokens[0];
      try {
        int rowCount = Integer.parseInt(tokens[1]);
        int seatsPerRow = Integer.parseInt(tokens[2]);

        if (rowCount < 1 || rowCount > 26 || seatsPerRow < 1 || seatsPerRow > 50) {
          System.out.println("Row must be 1–26 and Seats/Row 1–50.\n");
          continue;
        }
        return new Layout(movieTitle, rowCount, seatsPerRow);
      } catch (NumberFormatException e) {
        System.out.println("Rows and Seats must be numbers.\n");
      }
    }
  }

  private Integer promptTickets(Layout layout) {
    while (true) {
      System.out.println();
      System.out.println(
          "Enter number of tickets to book, or enter blank to go back to main menu:");
      System.out.print("> ");
      String line = scanner.nextLine().trim();
      if (line.isEmpty()) return null;

      try {
        int n = Integer.parseInt(line);
        if (n <= 0) {
          System.out.println("Please enter a positive integer.\n");
          continue;
        }
        return n;
      } catch (NumberFormatException nfe) {
        System.out.println("Please enter a valid integer.\n");
      }
    }
  }

  private boolean isValidSeatCode(String code, int rowCount, int seatsPerRow) {
    String s = code.trim().toUpperCase();
    if (!s.matches("^[A-Z]\\d{1,2}$")) return false;
    char rowChar = s.charAt(0);
    int col = Integer.parseInt(s.substring(1));
    char maxRowChar = (char) ('A' + (rowCount - 1));
    return rowChar >= 'A' && rowChar <= maxRowChar && col >= 1 && col <= seatsPerRow;
  }

  private SeatDto parseSeatCode(String seatCode) {
    var m = Pattern.compile("^([A-Z])0*(\\d+)$").matcher(seatCode.trim().toUpperCase());
    if (!m.matches()) throw new IllegalArgumentException("Invalid seat code: " + seatCode);
    return new SeatDto(m.group(1), Integer.parseInt(m.group(2)));
  }

  private void renderBookingSnapshot(Layout layout, int tickets, ReservedSeatsResponse reserve) {
    System.out.println();
    System.out.printf(
        """
                Successfully reserved %d %s tickets.
                Booking id: %s
                Selected seats:

                """,
        tickets, layout.movieTitle(), reserve.bookingId());

    SeatMapPrinter.print(
        layout.rowCount(),
        layout.seatsPerRow(),
        reserve.takenSeats(), // seats taken by others
        reserve.reservedSeats()); // your temporary hold
  }

  // ==== Small types ====
  private record Layout(String movieTitle, int rowCount, int seatsPerRow) {}
}
