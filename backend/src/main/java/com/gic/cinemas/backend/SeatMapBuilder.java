package com.gic.cinemas.backend;

import com.gic.cinemas.backend.exception.InvalidStartSeatException;
import com.gic.cinemas.common.dto.SeatDto;
import java.util.BitSet;
import java.util.List;

public class SeatMapBuilder {

  public static BitSet[] buildSeatMap(int rowCount, int seatsPerRow, List<SeatDto> bookedSeats) {
    BitSet[] seatMap = new BitSet[rowCount];
    for (int r = 0; r < rowCount; r++) {
      seatMap[r] = new BitSet(seatsPerRow);
    }
    for (SeatDto seat : bookedSeats) {
      int rowIndex = toRowIndex(seat.rowLabel(), rowCount);
      int colIndex = seat.seatNumber();
      seatMap[rowIndex].set(colIndex - 1);
    }

    return seatMap;
  }

  /** A == 0, B == 1, … */
  public static int toRowIndex(String rowLabel, int rowCount) {
    if (rowLabel == null || rowLabel.isBlank()) {
      throw new InvalidStartSeatException("Row label is blank");
    }
    char c = Character.toUpperCase(rowLabel.charAt(0));
    int idx = c - 'A';
    if (idx < 0 || idx >= rowCount) {
      throw new InvalidStartSeatException("Row label out of bounds: " + rowLabel);
    }
    return idx;
  }

  /** 0 -> "A", 1 -> "B", … */
  public static String toRowLabel(int rowIndex, int rowCount) {
    if (rowIndex < 0 || rowIndex >= rowCount) {
      throw new IllegalArgumentException("Row index out of bounds: " + rowIndex);
    }
    return String.valueOf((char) ('A' + rowIndex));
  }
}
