package com.gic.cinemas.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.gic.cinemas.backend.model.SeatingConfigEntity;
import com.gic.cinemas.backend.repository.SeatingConfigRepository;
import com.gic.cinemas.common.dto.response.SeatingAvailabilityResponse;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class SeatingConfigServiceTest {

  @Mock SeatingConfigRepository configs;

  @InjectMocks SeatingConfigService service;

  private final String title = "Dune";
  private final int rows = 8;
  private final int cols = 12;

  @Nested
  @DisplayName("findOrCreate")
  class FindOrCreate {

    @Test
    @DisplayName("returns existing config when found")
    void returnsExisting() {
      // existing entity is a mock with getters stubbed
      SeatingConfigEntity existing = mock(SeatingConfigEntity.class);
      when(existing.getId()).thenReturn(42L);
      when(existing.getMovieTitle()).thenReturn("Dune");
      when(existing.getRows()).thenReturn(rows);
      when(existing.getSeatsPerRow()).thenReturn(cols);

      when(configs.findByMovieTitleAndRowsAndSeatsPerRow("Dune", rows, cols))
          .thenReturn(Optional.of(existing));

      SeatingAvailabilityResponse response = service.findOrCreate("Dune", rows, cols);

      assertEquals(42L, response.seatingConfigId());
      assertEquals("Dune", response.title());
      assertEquals(rows, response.rows());
      assertEquals(cols, response.cols());
      assertEquals(rows * cols, response.availableSeats());

      verify(configs, never()).save(any());
    }

    @Test
    @DisplayName("creates new config when not found")
    void createsWhenMissing() {
      when(configs.findByMovieTitleAndRowsAndSeatsPerRow(title, rows, cols))
          .thenReturn(Optional.empty());

      // capture the entity passed to save(...)
      ArgumentCaptor<SeatingConfigEntity> captor =
          ArgumentCaptor.forClass(SeatingConfigEntity.class);

      // mock the saved/returned entity (with an id), independent from the captured input
      SeatingConfigEntity savedFromDb = mock(SeatingConfigEntity.class);
      when(savedFromDb.getId()).thenReturn(7L);
      when(savedFromDb.getMovieTitle()).thenReturn(title);
      when(savedFromDb.getRows()).thenReturn(rows);
      when(savedFromDb.getSeatsPerRow()).thenReturn(cols);

      when(configs.save(captor.capture())).thenReturn(savedFromDb);

      SeatingAvailabilityResponse res = service.findOrCreate(title, rows, cols);

      // verify the entity created by the service before persistence
      SeatingConfigEntity created = captor.getValue();
      assertEquals(title, created.getMovieTitle());
      assertEquals(rows, created.getRows());
      assertEquals(cols, created.getSeatsPerRow());

      // verify response comes from the saved entity (with id)
      assertEquals(7L, res.seatingConfigId());
      assertEquals(title, res.title());
      assertEquals(rows, res.rows());
      assertEquals(cols, res.cols());
      assertEquals(rows * cols, res.availableSeats());

      verify(configs, times(1)).save(any(SeatingConfigEntity.class));
    }

    @Test
    @DisplayName("handles race: save throws DataIntegrityViolationException then re-reads")
    void handlesConcurrentCreation() {
      when(configs.findByMovieTitleAndRowsAndSeatsPerRow(title, rows, cols))
          .thenReturn(Optional.empty()) // first lookup: not found
          .thenReturn(Optional.of(stubEntity(99L, title, rows, cols))); // second lookup after dup

      when(configs.save(any(SeatingConfigEntity.class)))
          .thenThrow(new DataIntegrityViolationException("duplicate"));

      SeatingAvailabilityResponse res = service.findOrCreate(title, rows, cols);

      assertEquals(99L, res.seatingConfigId());
      assertEquals(title, res.title());
      assertEquals(rows, res.rows());
      assertEquals(cols, res.cols());
      assertEquals(rows * cols, res.availableSeats());
    }

    @Test
    @DisplayName("validates inputs (blank title)")
    void validatesBlankTitle() {
      IllegalArgumentException ex =
          assertThrows(
              IllegalArgumentException.class, () -> service.findOrCreate("   ", rows, cols));
      assertTrue(ex.getMessage().toLowerCase().contains("title"));
      verifyNoInteractions(configs);
    }

    @Test
    @DisplayName("validates inputs (rows/cols must be > 0)")
    void validatesRowsCols() {
      assertThrows(IllegalArgumentException.class, () -> service.findOrCreate(title, 0, cols));
      assertThrows(IllegalArgumentException.class, () -> service.findOrCreate(title, rows, 0));
      verifyNoInteractions(configs);
    }
  }

  // ---- local stub factory (no reflection) ---------------------------------

  private static SeatingConfigEntity stubEntity(Long id, String title, int rows, int cols) {
    SeatingConfigEntity e = mock(SeatingConfigEntity.class);
    when(e.getId()).thenReturn(id);
    when(e.getMovieTitle()).thenReturn(title);
    when(e.getRows()).thenReturn(rows);
    when(e.getSeatsPerRow()).thenReturn(cols);
    return e;
  }
}
