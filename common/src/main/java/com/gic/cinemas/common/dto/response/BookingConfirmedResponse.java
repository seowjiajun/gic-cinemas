package com.gic.cinemas.common.dto.response;

import com.gic.cinemas.common.dto.BookingStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BookingConfirmedResponse(
    @NotBlank String bookingId, @NotBlank String movieTitle, @NotNull BookingStatus status) {}
