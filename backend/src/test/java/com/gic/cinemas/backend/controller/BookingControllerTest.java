package com.gic.cinemas.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gic.cinemas.backend.service.BookingService;
import com.gic.cinemas.common.dto.SeatDto;
import com.gic.cinemas.common.dto.request.BookingRequest;
import com.gic.cinemas.common.dto.response.BookingResponse;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  // 👇 Mock the service dependency (we’re unit testing the controller only)
  @MockBean private BookingService bookingService;

  @Test
  void createBooking_returnsBookingResponse() throws Exception {
    // Arrange
    var request = new BookingRequest("Inception", 3);
    var response =
        new BookingResponse(
            "GIC0001",
            "Inception",
            List.of(new SeatDto("A01"), new SeatDto("A02"), new SeatDto("A03")));

    when(bookingService.createBooking(any(BookingRequest.class))).thenReturn(response);

    // Act + Assert
    mockMvc
        .perform(
            post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("GIC0001"))
        .andExpect(jsonPath("$.movie").value("Inception"))
        .andExpect(jsonPath("$.seats[0].code").value("A01"))
        .andExpect(jsonPath("$.seats.length()").value(3));
  }

  @Test
  void getBooking_returnsBookingResponse() throws Exception {
    // Arrange
    var response =
        new BookingResponse("GIC0002", "Matrix", List.of(new SeatDto("A01"), new SeatDto("A02")));

    when(bookingService.getBooking("GIC0002")).thenReturn(response);

    // Act + Assert
    mockMvc
        .perform(get("/api/bookings/GIC0002"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("GIC0002"))
        .andExpect(jsonPath("$.movie").value("Matrix"))
        .andExpect(jsonPath("$.seats.length()").value(2));
  }

  @Test
  void getBooking_whenNotFound_returns404() throws Exception {
    // Arrange
    when(bookingService.getBooking(eq("BAD_ID")))
        .thenThrow(new NoSuchElementException("Booking not found"));

    // Act + Assert
    mockMvc.perform(get("/api/bookings/BAD_ID")).andExpect(status().isNotFound());
  }
}
