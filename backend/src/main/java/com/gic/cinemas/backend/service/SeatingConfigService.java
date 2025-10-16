package com.gic.cinemas.backend.service;

import com.gic.cinemas.backend.exception.SeatingConfigNotFoundException;
import com.gic.cinemas.backend.model.SeatingConfigEntity;
import com.gic.cinemas.backend.repository.BookedSeatRepository;
import com.gic.cinemas.backend.repository.SeatingConfigRepository;
import com.gic.cinemas.common.dto.response.SeatingAvailabilityResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SeatingConfigService {

  private final BookedSeatRepository bookedSeatRepository;
  private final SeatingConfigRepository seatingConfigRepository;
  private final SeatingConfigHelper seatingConfigHelper;

  /**
   * Find a seating configuration by (movieTitle, rowCount, seatsPerRow). If not found, create one.
   * Then return a simple response.
   */
  @Transactional
  public SeatingAvailabilityResponse findOrCreate(
      String movieTitle, int rowCount, int seatsPerRow) {
    SeatingConfigEntity seatingConfigEntity =
        seatingConfigHelper.findOrCreateConfig(movieTitle, rowCount, seatsPerRow);

    Long availableSeatsCount =
        bookedSeatRepository.countAvailableSeatsByConfigId(seatingConfigEntity.getId());
    if (availableSeatsCount == null) {
      availableSeatsCount = (long) rowCount * seatsPerRow;
    }

    return new SeatingAvailabilityResponse(
        seatingConfigEntity.getMovieTitle(),
        seatingConfigEntity.getRowCount(),
        seatingConfigEntity.getSeatsPerRow(),
        availableSeatsCount);
  }

  @Transactional(readOnly = true)
  public SeatingAvailabilityResponse getAvailability(
      String movieTitle, int rowCount, int seatsPerRow) {
    // Try to find an existing config (do NOT create a new one)
    SeatingConfigEntity config =
        seatingConfigRepository
            .findByTitleAndLayout(movieTitle.trim(), rowCount, seatsPerRow)
            .orElseThrow(
                () -> new SeatingConfigNotFoundException(movieTitle, rowCount, seatsPerRow));

    Long availableSeatsCount = bookedSeatRepository.countAvailableSeatsByConfigId(config.getId());
    if (availableSeatsCount == null) {
      availableSeatsCount = (long) rowCount * seatsPerRow;
    }

    return new SeatingAvailabilityResponse(
        config.getMovieTitle(), config.getRowCount(), config.getSeatsPerRow(), availableSeatsCount);
  }
}
