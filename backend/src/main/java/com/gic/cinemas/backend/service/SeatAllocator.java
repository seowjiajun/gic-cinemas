package com.gic.cinemas.backend.service;

import com.gic.cinemas.backend.SeatMapBuilder;
import com.gic.cinemas.backend.exception.InvalidStartSeatException;
import com.gic.cinemas.backend.exception.NoAvailableSeatsException;
import com.gic.cinemas.common.dto.SeatDto;
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
    List<SeatDto> allocatedSeats = new ArrayList<>(tickets);
    int remaining = tickets;

    // First pass: seats allocation (no mutation)
    for (int r = 0; r < rowCount && remaining > 0; r++) {
      BitSet row = seatMap[r];
      String rowLabel = SeatMapBuilder.toRowLabel(r, rowCount);

      List<Integer> picks = allocateFromCenter(row, seatsPerRow, remaining);
      for (int seatNumber : picks) {
        allocatedSeats.add(new SeatDto(rowLabel, seatNumber));
        if (allocatedSeats.size() == tickets) break;
      }
      remaining = tickets - allocatedSeats.size();
    }

    if (allocatedSeats.size() < tickets) {
      long available = allocatedSeats.size();
      throw new NoAvailableSeatsException(
          String.format("Only %d seat(s) available, requested %d.", available, tickets), available);
    }

    // Second pass: commit (mutate BitSets)
    for (SeatDto seat : allocatedSeats) {
      int rowIndex = SeatMapBuilder.toRowIndex(seat.rowLabel(), rowCount);
      seatMap[rowIndex].set(seat.seatNumber() - 1); // BitSet is 0-based
    }

    return allocatedSeats;
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

    int startRow = SeatMapBuilder.toRowIndex(startSeat.rowLabel(), rowCount);
    int startCol = startSeat.seatNumber() - 1; // SeatDto is 1-based

    if (startRow < 0 || startRow >= rowCount || startCol < 0 || startCol >= seatsPerRow) {
      throw new InvalidStartSeatException("startSeat out of bounds: " + startSeat);
    }

    // -----------------------------
    // Plan allocation (no mutation yet)
    // -----------------------------
    List<SeatDto> allocatedSeats = new ArrayList<>(tickets);
    int remaining = tickets;

    // Start row → allocate to the right greedily
    remaining -=
        allocateRightGreedy(
            seatMap, rowCount, seatsPerRow, startRow, startCol, remaining, allocatedSeats);

    // Overflow rows (toward screen, i.e. increasing row index)
    for (int r = startRow + 1; r < rowCount && remaining > 0; r++) {
      BitSet row = seatMap[r];
      List<Integer> picks = allocateFromCenter(row, seatsPerRow, remaining);
      if (!picks.isEmpty()) {
        String rowLabel = SeatMapBuilder.toRowLabel(r, rowCount);
        for (int seatNum : picks) {
          allocatedSeats.add(new SeatDto(rowLabel, seatNum));
          if (allocatedSeats.size() == tickets) break;
        }
        remaining = tickets - allocatedSeats.size();
      }
    }

    // -----------------------------
    // Validation — all seats available?
    // -----------------------------
    if (allocatedSeats.size() < tickets) {
      long available = allocatedSeats.size();
      throw new NoAvailableSeatsException(
          String.format("Only %d seat(s) available, requested %d.", available, tickets), available);
    }

    // -----------------------------
    // Commit (mutate BitSets)
    // -----------------------------
    for (SeatDto seat : allocatedSeats) {
      int rowIndex = SeatMapBuilder.toRowIndex(seat.rowLabel(), rowCount);
      seatMap[rowIndex].set(seat.seatNumber() - 1); // BitSet is 0-based
    }

    return allocatedSeats;
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
