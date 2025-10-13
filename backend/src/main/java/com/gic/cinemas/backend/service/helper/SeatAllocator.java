package com.gic.cinemas.backend.service.helper;

import com.gic.cinemas.common.dto.response.SeatDto;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SeatAllocator {

  public List<SeatDto> allocateWithOverflow(
      BitSet[] seatMap, int rowCount, int seatsPerRow, int tickets) {
    List<SeatDto> out = new ArrayList<>();
    int remaining = tickets;

    for (int r = rowCount - 1; r >= 0 && remaining > 0; r--) { // A(back) → Z(front)
      List<Integer> pick = findBestBlockUpTo(seatMap[r], seatsPerRow, remaining);
      if (pick.isEmpty()) continue;

      String rowLabel = toRowLabel(r, rowCount);
      for (int seatNo : pick) {
        out.add(new SeatDto(rowLabel, seatNo));
        seatMap[r].set(seatNo - 1); // mark taken in transient map
      }
      remaining -= pick.size();
    }

    //        if (remaining > 0) throw new NoAvailableSeatsException("Not enough seats available");
    return out;
  }

  private String toRowLabel(int rowIndex, int rowCount) {
    char label = (char) ('A' + (rowCount - 1 - rowIndex));
    return String.valueOf(label);
  }

  private List<Integer> findBestBlockUpTo(BitSet row, int seatsPerRow, int numberOfTickets) {
    if (numberOfTickets <= 0) return List.of();

    int bestStart = -1;
    int bestBlockLength = 0;
    double bestDistance = Double.MAX_VALUE;
    double rowCenter = (seatsPerRow - 1) / 2.0;

    // Iterate through all contiguous free runs in the row
    for (int i = row.nextClearBit(0); i >= 0 && i < seatsPerRow; ) {
      int nextTaken = row.nextSetBit(i);
      int end = (nextTaken == -1 || nextTaken > seatsPerRow) ? seatsPerRow : nextTaken;
      int freeRunLength = end - i;

      if (freeRunLength > 0) {
        // allocate as many seats as possible from this run (up to numberOfTickets)
        int blockLength = Math.min(freeRunLength, numberOfTickets);

        // Try every possible sub-block of this size within the free run
        int bestStartInRun = i;
        double bestDistInRun = Double.MAX_VALUE;

        for (int start = i; start <= end - blockLength; start++) {
          double mid = start + (blockLength - 1) / 2.0;
          double dist = Math.abs(mid - rowCenter);
          if (dist < bestDistInRun) {
            bestDistInRun = dist;
            bestStartInRun = start;
          }
        }

        // Prefer larger block; then closer to center
        if (blockLength > bestBlockLength
            || (blockLength == bestBlockLength && bestDistInRun < bestDistance)) {
          bestStart = bestStartInRun;
          bestBlockLength = blockLength;
          bestDistance = bestDistInRun;
        }
      }

      if (nextTaken == -1) break;
      i = row.nextClearBit(nextTaken + 1);
    }

    if (bestBlockLength == 0) return List.of(); // no available block

    // Convert to 1-based seat numbers
    List<Integer> seats = new ArrayList<>(bestBlockLength);
    for (int j = 0; j < bestBlockLength; j++) {
      seats.add(bestStart + j + 1);
    }
    return seats;
  }

  private static int toRowIndex(char rowLabel, int rowCount) {
    return (rowCount - 1) - (rowLabel - 'A');
  }
}
