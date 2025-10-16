package com.gic.cinemas.backend.service;

import com.gic.cinemas.backend.SeatMapBuilder;
import com.gic.cinemas.common.dto.response.SeatDto;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SeatAllocator {

  public List<SeatDto> allocateDefault(
      int rowCount, int seatsPerRow, int tickets, List<SeatDto> bookedSeats) {
    BitSet[] seatMap = SeatMapBuilder.buildSeatMap(rowCount, seatsPerRow, bookedSeats);
    return allocateDefault(seatMap, rowCount, seatsPerRow, tickets);
  }

  public List<SeatDto> allocateDefault(
      BitSet[] seatMap, int rowCount, int seatsPerRow, int tickets) {
    List<SeatDto> reservedSeats = new ArrayList<>(tickets);

    int remaining = tickets;

    // A (index 0) → Z (index rowCount - 1)
    for (int r = 0; r < rowCount && remaining > 0; r++) {
      BitSet row = seatMap[r];
      String rowLabel = SeatMapBuilder.toRowLabel(r, rowCount);

      // Allocate seats expanding outward from the middle
      List<Integer> picks = allocateFromCenter(row, seatsPerRow, remaining);
      if (picks.isEmpty()) continue;

      for (int seatNumber : picks) {
        reservedSeats.add(new SeatDto(rowLabel, seatNumber));
        row.set(seatNumber - 1); // mark taken (BitSet is 0-based)
      }
      remaining -= picks.size();
    }

    return reservedSeats;
  }

  private List<Integer> allocateFromCenter(BitSet row, int seatsPerRow, int need) {
    List<Integer> picks = new ArrayList<>(need);

    int left = (seatsPerRow - 1) / 2;
    int right = seatsPerRow / 2;

    while (picks.size() < need && (left >= 0 || right < seatsPerRow)) {
      // For odd rows, center seat is same as left == right
      if (left == right && !row.get(left)) {
        picks.add(left + 1);
        left--;
        right++;
        continue;
      }

      // For even rows, prefer left side first (center-left), then right
      if (left >= 0 && !row.get(left) && picks.size() < need) {
        picks.add(left + 1);
      }
      if (right < seatsPerRow && !row.get(right) && picks.size() < need) {
        picks.add(right + 1);
      }

      left--;
      right++;
    }

    return picks;
  }

  public List<SeatDto> allocateFromStartSeat(
      int rowCount, int seatsPerRow, int tickets, SeatDto startSeat, List<SeatDto> bookedSeats) {
    BitSet[] seatMap = SeatMapBuilder.buildSeatMap(rowCount, seatsPerRow, bookedSeats);
    return allocateFromStartSeat(seatMap, rowCount, seatsPerRow, tickets, startSeat);
  }

  public List<SeatDto> allocateFromStartSeat(
      BitSet[] seatMap, int rowCount, int seatsPerRow, int tickets, SeatDto startSeat) {

    if (tickets <= 0) return List.of();
    if (startSeat == null || startSeat.rowLabel() == null || startSeat.rowLabel().isBlank()) {
      throw new IllegalArgumentException("startSeat must be provided");
    }

    int startRow =
        SeatMapBuilder.toRowIndex(String.valueOf(startSeat.rowLabel().charAt(0)), rowCount);
    int startCol = startSeat.seatNumber() - 1; // DTO is 1-based

    if (startRow < 0 || startRow >= rowCount || startCol < 0 || startCol >= seatsPerRow) {
      throw new IllegalArgumentException("startSeat out of bounds: " + startSeat);
    }

    List<SeatDto> out = new ArrayList<>(tickets);
    int remaining = tickets;

    // 1) Start row: greedy to the RIGHT (skip taken; no contiguity requirement)
    remaining -=
        allocateRightGreedy(seatMap, rowCount, seatsPerRow, startRow, startCol, remaining, out);

    // 2) ONLY overflow TOWARD the SCREEN (increasing row letter): use center-out on each row
    for (int r = startRow + 1; r < rowCount && remaining > 0; r++) {
      List<Integer> picks = allocateFromCenter(seatMap[r], seatsPerRow, remaining);
      if (!picks.isEmpty()) {
        String rowLabel = SeatMapBuilder.toRowLabel(r, rowCount);
        for (int seatNum : picks) {
          out.add(new SeatDto(rowLabel, seatNum));
          seatMap[r].set(seatNum - 1); // mark taken in transient map
        }
        remaining -= picks.size();
      }
    }

    // Caller can decide whether partial allocation is OK:
    // if (out.size() < tickets) throw new UnableToAllocateSeatsException(...);
    return out;
  }

  private int allocateRightGreedy(
      BitSet[] seatMap,
      int rowCount,
      int seatsPerRow,
      int row,
      int startCol,
      int need,
      List<SeatDto> out) {

    if (need <= 0) return 0;
    String rowLabel = SeatMapBuilder.toRowLabel(row, rowCount);
    int taken = 0;

    for (int c = startCol; c < seatsPerRow && taken < need; c++) {
      if (seatMap[row].get(c)) continue; // skip taken; no contiguity requirement
      out.add(new SeatDto(rowLabel, c + 1)); // DTO is 1-based
      seatMap[row].set(c); // mark taken in transient map
      taken++;
    }
    return taken;
  }
}
