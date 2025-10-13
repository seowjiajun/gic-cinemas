package com.gic.cinemas.backend;

import com.gic.cinemas.backend.repository.SeatCoordinate;
import java.util.BitSet;
import java.util.List;

public class SeatMapBuilder {

  public static BitSet[] buildSeatMap(
      int rowCount, int seatsPerRow, List<SeatCoordinate> bookedSeats) {
    BitSet[] seatMap = new BitSet[rowCount];
    for (int r = 0; r < rowCount; r++) {
      seatMap[r] = new BitSet(seatsPerRow);
    }
    for (SeatCoordinate seat : bookedSeats) {
      int rowIndex = toRowIndex(seat.getRowLabel(), rowCount);
      int colIndex = seat.getSeatNumber();
      seatMap[rowIndex].set(colIndex - 1);
    }

    return seatMap;
  }

  public static int toRowIndex(char rowLabel, int rowCount) {
    return (rowCount - 1) - (rowLabel - 'A');
  }
}
