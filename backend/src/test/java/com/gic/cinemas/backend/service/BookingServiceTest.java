package com.gic.cinemas.backend.service;

import static org.junit.jupiter.api.Assertions.*;

class BookingServiceTest {

  //  private final BookingService service = new BookingService();

  //  @ParameterizedTest
  //  @ValueSource(ints = {1, 2, 5})
  //  void createBooking_allocatesRequestedSeatCount(int count) {
  //    BookingRequest req = new BookingRequest("Inception", count);
  //    BookingResponse resp = service.createBooking(req);
  //
  //    assertNotNull(resp.id());
  //    assertEquals("Inception", resp.movie());
  //    assertEquals(count, resp.seats().size());
  //    assertTrue(resp.seats().get(0).code().startsWith("A"));
  //  }
  //
  //  @Test
  //  void getBooking_returnsCreatedBooking() {
  //    var created = service.createBooking(new BookingRequest("Matrix", 2));
  //    var fetched = service.getBooking(created.id());
  //
  //    assertEquals(created.id(), fetched.id());
  //    assertEquals(created.movie(), fetched.movie());
  //    assertEquals(created.seats(), fetched.seats());
  //  }
  //
  //  @Test
  //  void getBooking_throwsWhenNotFound() {
  //    assertThrows(NoSuchElementException.class, () -> service.getBooking("DOES_NOT_EXIST"));
  //  }
  //
  //  @Test
  //  void bookingIdsIncrement() {
  //    var a = service.createBooking(new BookingRequest("A", 1)).id();
  //    var b = service.createBooking(new BookingRequest("B", 1)).id();
  //    assertNotEquals(a, b);
  //    // Optional: assert prefix and zero-padding shape
  //    assertTrue(a.matches("GIC\\d{4}"));
  //    assertTrue(b.matches("GIC\\d{4}"));
  //  }
}
