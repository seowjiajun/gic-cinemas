package com.gic.cinemas.backend.service;

import com.gic.cinemas.backend.model.SeatingConfigEntity;
import com.gic.cinemas.backend.repository.SeatingConfigRepository;
import com.gic.cinemas.backend.validation.SeatingConfigValidator;
import com.gic.cinemas.common.dto.response.SeatingAvailabilityResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SeatingConfigService {

  private final SeatingConfigRepository seatingConfigRepository;
  private final SeatingConfigValidator validator;

  public SeatingConfigService(
      SeatingConfigRepository seatingConfigRepository, SeatingConfigValidator validator) {
    this.seatingConfigRepository = seatingConfigRepository;
    this.validator = validator;
  }

  /**
   * Find a seating configuration by (movieTitle, rows, cols). If not found, create one. Then return
   * a simple response.
   */
  @Transactional
  public SeatingAvailabilityResponse findOrCreate(String title, int rows, int cols) {
    validator.validate(title, rows, cols);

    String normalizedTitle = title.trim();

    // 1. Try to find existing
    SeatingConfigEntity config =
        seatingConfigRepository
            .findByMovieTitleAndRowsAndSeatsPerRow(normalizedTitle, rows, cols)
            .orElse(null);

    // 2. Create if missing (race-safe)
    if (config == null) {
      try {
        config = new SeatingConfigEntity(normalizedTitle, rows, cols);
        config = seatingConfigRepository.save(config);
      } catch (DataIntegrityViolationException dup) {
        // handle concurrent creation
        config =
            seatingConfigRepository
                .findByMovieTitleAndRowsAndSeatsPerRow(normalizedTitle, rows, cols)
                .orElseThrow(() -> dup);
      }
    }

    int availableSeats = config.getRows() * config.getSeatsPerRow();
    return new SeatingAvailabilityResponse(
        config.getId(),
        config.getMovieTitle(),
        config.getRows(),
        config.getSeatsPerRow(),
        availableSeats);
  }
}
