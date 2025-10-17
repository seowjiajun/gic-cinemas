package com.gic.cinemas.backend.service;

import com.gic.cinemas.backend.model.SeatingConfigEntity;
import com.gic.cinemas.backend.repository.SeatingConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SeatingConfigHelper {

  private final SeatingConfigRepository seatingConfigRepository;

  @Transactional
  public SeatingConfigEntity findOrCreateSeatingConfig(
      String movieTitle, int rowCount, int seatsPerRow) {
    Long seatingConfigId =
        seatingConfigRepository
            .findIdByTitleAndLayout(movieTitle, rowCount, seatsPerRow)
            .orElse(null);

    SeatingConfigEntity seatingConfigEntity;
    if (seatingConfigId == null) {
      // create seating config since not found
      try {
        seatingConfigEntity = new SeatingConfigEntity(movieTitle, rowCount, seatsPerRow);
        seatingConfigEntity = seatingConfigRepository.saveAndFlush(seatingConfigEntity);
        // in the case of race condition, runner-up fails to create config.
        // runner-up will use the seating config created by winner.
      } catch (DataIntegrityViolationException e) {
        Long existingId =
            seatingConfigRepository
                .findIdByTitleAndLayout(movieTitle, rowCount, seatsPerRow)
                .orElseThrow(() -> e);
        seatingConfigEntity = seatingConfigRepository.getReferenceById(existingId);
      }
      // seating config is found
    } else {
      seatingConfigEntity = seatingConfigRepository.getReferenceById(seatingConfigId);
    }

    return seatingConfigEntity;
  }
}
