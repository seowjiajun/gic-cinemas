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
  public SeatingAvailabilityResponse findOrCreateSeatingConfig(
      String movieTitle, int rowCount, int seatsPerRow) {
    final String normalizedTitle = movieTitle.trim();

    SeatingConfigEntity seatingConfigEntity =
        seatingConfigHelper.findOrCreateSeatingConfig(normalizedTitle, rowCount, seatsPerRow);

    Long availableSeatsCount =
        bookedSeatRepository
            .countAvailableSeatsByConfigId(seatingConfigEntity.getId())
            .orElseThrow(
                () -> new SeatingConfigNotFoundException(normalizedTitle, rowCount, seatsPerRow));

    return new SeatingAvailabilityResponse(
        seatingConfigEntity.getMovieTitle(),
        seatingConfigEntity.getRowCount(),
        seatingConfigEntity.getSeatsPerRow(),
        availableSeatsCount);
  }

  @Transactional(readOnly = true)
  public SeatingAvailabilityResponse getSeatingAvailability(
      String movieTitle, int rowCount, int seatsPerRow) {
    final String normalizedTitle = movieTitle.trim();

    SeatingConfigEntity seatingConfig =
        seatingConfigRepository
            .findByTitleAndLayout(movieTitle.trim(), rowCount, seatsPerRow)
            .orElseThrow(
                () -> new SeatingConfigNotFoundException(normalizedTitle, rowCount, seatsPerRow));

    Long availableSeatsCount =
        bookedSeatRepository
            .countAvailableSeatsByConfigId(seatingConfig.getId())
            .orElseThrow(
                () -> new SeatingConfigNotFoundException(normalizedTitle, rowCount, seatsPerRow));

    return new SeatingAvailabilityResponse(
        seatingConfig.getMovieTitle(),
        seatingConfig.getRowCount(),
        seatingConfig.getSeatsPerRow(),
        availableSeatsCount);
  }
}
