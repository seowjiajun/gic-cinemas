package com.gic.cinemas.backend.integration.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.gic.cinemas.backend.model.BookingEntity;
import com.gic.cinemas.backend.model.SeatingConfigEntity;
import com.gic.cinemas.backend.repository.BookingRepository;
import com.gic.cinemas.backend.repository.SeatingConfigRepository;
import com.gic.cinemas.common.dto.BookingStatus;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class BookingRepositoryIntegrationTest {

  @Autowired private BookingRepository bookingRepository;
  @Autowired private SeatingConfigRepository seatingConfigRepository;

  @Test
  @DisplayName("findMaxId returns 0 when repository is empty")
  void testFindMaxIdWhenEmpty() {
    Long maxId = bookingRepository.findMaxId();
    assertThat(maxId).isZero();
  }

  @Test
  @DisplayName("findMaxId returns highest ID when bookings exist")
  void testFindMaxIdWhenBookingsExist() {
    SeatingConfigEntity seatingConfig = new SeatingConfigEntity("Inception", 8, 10);
    seatingConfigRepository.save(seatingConfig);

    BookingEntity b1 = new BookingEntity("GIC0001", seatingConfig, LocalDateTime.now());
    bookingRepository.save(b1);

    BookingEntity b2 =
        new BookingEntity("GIC0002", seatingConfig, LocalDateTime.now(), BookingStatus.CONFIRMED);
    bookingRepository.save(b2);

    Long maxId = bookingRepository.findMaxId();
    assertThat(maxId).isEqualTo(b2.getId());
  }

  @Test
  @DisplayName("findByBookingId returns the correct entity when it exists")
  void testFindByBookingId_found() {
    SeatingConfigEntity cfg = new SeatingConfigEntity("Interstellar", 5, 8);
    seatingConfigRepository.save(cfg);

    BookingEntity booking =
        new BookingEntity("GIC0001", cfg, LocalDateTime.now(), BookingStatus.CONFIRMED);
    bookingRepository.saveAndFlush(booking);

    Optional<BookingEntity> found = bookingRepository.findByBookingId("GIC0001");
    assertThat(found)
        .isPresent()
        .get()
        .satisfies(
            b -> {
              assertThat(b.getBookingId()).isEqualTo("GIC0001");
              assertThat(b.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
              assertThat(b.getSeatingConfig().getMovieTitle()).isEqualTo("Interstellar");
            });
  }

  @Test
  @DisplayName("findByBookingId returns empty when bookingId does not exist")
  void testFindByBookingId_notFound() {
    SeatingConfigEntity cfg = new SeatingConfigEntity("Interstellar", 5, 8);
    seatingConfigRepository.save(cfg);

    BookingEntity booking =
        new BookingEntity("GIC0001", cfg, LocalDateTime.now(), BookingStatus.CONFIRMED);
    bookingRepository.saveAndFlush(booking);

    Optional<BookingEntity> notFound = bookingRepository.findByBookingId("GIC1001");
    assertThat(notFound).isEmpty();
  }
}
