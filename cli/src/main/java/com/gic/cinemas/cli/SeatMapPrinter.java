package com.gic.cinemas.cli;

import com.gic.cinemas.common.dto.response.SeatDto;
import java.util.*;
import java.util.List;

public final class SeatMapPrinter {

  private static final String SEAT_GAP = "  "; // 2 spaces between seats
  private static final String SCREEN = "S C R E E N";
  private static final int ROW_LABEL_WIDTH = 2; // "<row><space>" e.g. "A "

  private SeatMapPrinter() {}

  public static void print(
      int rowCount, int seatsPerRow, List<SeatDto> bookedSeats, List<SeatDto> reservedSeats) {

    Set<String> booked = toLabels(bookedSeats);
    Set<String> reserved = toLabels(reservedSeats);

    printHeader(seatsPerRow);
    printGrid(rowCount, seatsPerRow, booked, reserved);
    printFooter(seatsPerRow);
  }

  // -------------------- layout math --------------------

  private static int seatAreaWidth(int seatsPerRow) {
    // visual width of seats only (no row label area)
    // each seat is 1 char + GAP after it (except the last one)
    return seatsPerRow + (seatsPerRow - 1) * SEAT_GAP.length();
  }

  // indent for the "SCREEN" banner so it’s centered over the seat area
  private static int screenIndent(int seatsPerRow) {
    int w = seatAreaWidth(seatsPerRow);
    int diff = w - SCREEN.length(); // positive => seats wider than screen
    return ROW_LABEL_WIDTH + Math.max(0, diff / 2);
  }

  // extra indent to push the grid under a wider "SCREEN" banner
  private static int gridExtraIndent(int seatsPerRow) {
    int w = seatAreaWidth(seatsPerRow);
    int diff = SCREEN.length() - w; // positive => screen wider than seats
    return Math.max(0, diff / 2);
  }

  // -------------------- sections --------------------

  private static void printHeader(int seatsPerRow) {
    System.out.println();
    System.out.print(" ".repeat(screenIndent(seatsPerRow)));
    System.out.println(SCREEN);

    // underline the seat area, centered under banner or grid
    int underlineIndent = ROW_LABEL_WIDTH + gridExtraIndent(seatsPerRow);
    System.out.print(" ".repeat(underlineIndent - 2)); // shift left slightly for symmetry
    System.out.println("--" + "-".repeat(seatAreaWidth(seatsPerRow)) + "--");
  }

  private static void printGrid(
      int rowCount, int seatsPerRow, Set<String> booked, Set<String> reserved) {

    int extra = gridExtraIndent(seatsPerRow);

    for (int r = rowCount - 1; r >= 0; r--) {
      char rowChar = (char) ('A' + r);

      StringBuilder sb = new StringBuilder();
      sb.append(rowChar).append(' '); // row label + 1 space
      sb.append(" ".repeat(extra)); // center grid when screen is wider

      for (int c = 1; c <= seatsPerRow; c++) {
        String key = rowChar + String.format("%02d", c); // internal normalized key

        char cell = '.';
        if (booked.contains(key)) cell = '#';
        if (reserved.contains(key)) cell = 'o';

        sb.append(cell);
        if (c < seatsPerRow) sb.append(SEAT_GAP);
      }

      System.out.println(sb);
    }
  }

  private static void printFooter(int seatsPerRow) {
    int extra = gridExtraIndent(seatsPerRow);
    StringBuilder axis = new StringBuilder();
    axis.append(" ".repeat(ROW_LABEL_WIDTH + extra));

    final int cellWidth = 1 + SEAT_GAP.length(); // visual width of one seat column

    for (int c = 1; c <= seatsPerRow; c++) {
      String num = Integer.toString(c); // no leading zero
      axis.append(num);

      // pad so each number column occupies the same visual width as a seat
      int pad = (c < seatsPerRow) ? (cellWidth - num.length()) : 0;
      if (pad > 0) axis.append(" ".repeat(pad));
    }

    System.out.println(axis);
  }

  // -------------------- utils --------------------

  private static Set<String> toLabels(List<SeatDto> seats) {
    Set<String> set = new HashSet<>();
    if (seats != null) {
      for (SeatDto s : seats) {
        set.add(s.rowLabel().toUpperCase() + String.format("%02d", s.seatNumber()));
      }
    }
    return set;
  }
}
