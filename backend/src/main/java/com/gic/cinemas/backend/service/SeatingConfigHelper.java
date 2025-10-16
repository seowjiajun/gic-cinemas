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
  public SeatingConfigEntity findOrCreateConfig(String movieTitle, int rowCount, int seatsPerRow) {
    final String normalized = movieTitle.trim();

    Long seatingConfigId =
        seatingConfigRepository
            .findIdByTitleAndLayout(normalized, rowCount, seatsPerRow)
            .orElse(null);

    SeatingConfigEntity seatingConfigEntity;

    if (seatingConfigId == null) {
      try {
        seatingConfigEntity =
            SeatingConfigEntity.builder()
                .movieTitle(normalized)
                .rowCount(rowCount)
                .seatsPerRow(seatsPerRow)
                .build();
        seatingConfigEntity = seatingConfigRepository.saveAndFlush(seatingConfigEntity);
      } catch (DataIntegrityViolationException e) {
        // Handle race: re-read after concurrent insert
        Long existingId =
            seatingConfigRepository
                .findIdByTitleAndLayout(normalized, rowCount, seatsPerRow)
                .orElseThrow(() -> e);
        seatingConfigEntity = seatingConfigRepository.getReferenceById(existingId);
      }
    } else {
      seatingConfigEntity = seatingConfigRepository.getReferenceById(seatingConfigId);
    }

    return seatingConfigEntity;
  }
}
