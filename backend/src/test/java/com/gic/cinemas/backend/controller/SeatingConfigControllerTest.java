package com.gic.cinemas.backend.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gic.cinemas.backend.service.SeatingConfigService;
import com.gic.cinemas.common.dto.response.SeatingAvailabilityResponse;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

/** Tests the controller layer only (no DB), mocking SeatingConfigService. */
@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = SeatingConfigController.class)
class SeatingConfigControllerTest {

  private static final String REQUEST_TEMPLATE =
      """
      {
        "title": "%s",
        "rows": %d,
        "cols": %d
      }
      """;

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockBean private SeatingConfigService seatingConfigService;

  @ParameterizedTest(name = "[{index}] POST /api/config -> {0}, {1}x{2}")
  @MethodSource("cases")
  @DisplayName("POST /api/seating-config returns 200 and body from service (parameterized)")
  void postFindOrCreateSuccess(String title, int rows, int cols) throws Exception {
    // Arrange
    SeatingAvailabilityResponse serviceResponse =
        new SeatingAvailabilityResponse(42L, title, rows, cols, rows * cols);

    Mockito.when(seatingConfigService.findOrCreate(eq(title), eq(rows), eq(cols)))
        .thenReturn(serviceResponse);

    String requestJson = REQUEST_TEMPLATE.formatted(title, rows, cols);

    // Act & Assert
    mockMvc
        .perform(
            post("/api/seating-config")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.seatingConfigId").value(42))
        .andExpect(jsonPath("$.title").value(title))
        .andExpect(jsonPath("$.rows").value(rows))
        .andExpect(jsonPath("$.cols").value(cols))
        .andExpect(jsonPath("$.availableSeats").value(rows * cols));

    Mockito.verify(seatingConfigService, Mockito.times(1))
        .findOrCreate(eq(title), eq(rows), eq(cols));
  }

  private static Stream<Arguments> cases() {
    return Stream.of(
        Arguments.of("Dune", 8, 12),
        Arguments.of("Deadpool and Wolverine", 10, 15),
        Arguments.of("Spider-Man: Across the Spider-Verse", 12, 20), // unicode/long title
        Arguments.of("Oppenheimer (70mm)", 5, 10));
  }
}
