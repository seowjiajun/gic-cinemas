package com.gic.cinemas.backend.repository;

import com.gic.cinemas.backend.model.SeatingConfigEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatingConfigRepository extends JpaRepository<SeatingConfigEntity, Long> {
  Optional<SeatingConfigEntity> findByMovieTitleAndRowsAndSeatsPerRow(
      String movieTitle, int rows, int seatsPerRow);
}
