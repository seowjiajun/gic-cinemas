package com.gic.cinemas.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.gic.cinemas.backend.model.SeatingConfigEntity;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class SeatingConfigRepositoryTest {

  @Autowired private SeatingConfigRepository seatingConfigRepository;

  @Test
  @DisplayName("findIdByTitleAndLayout returns correct ID when config exists")
  void testFindIdByTitleAndLayoutFound() {
    SeatingConfigEntity saved =
        seatingConfigRepository.save(
            SeatingConfigEntity.builder()
                .movieTitle("Inception")
                .rowCount(8)
                .seatsPerRow(10)
                .build());

    Optional<Long> result = seatingConfigRepository.findIdByTitleAndLayout("Inception", 8, 10);

    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(saved.getId());
  }

  @Test
  @DisplayName("findIdByTitleAndLayout returns empty when not found")
  void testFindIdByTitleAndLayoutNotFound() {
    Optional<Long> result = seatingConfigRepository.findIdByTitleAndLayout("Nonexistent", 5, 5);

    assertThat(result).isEmpty();
  }
}
