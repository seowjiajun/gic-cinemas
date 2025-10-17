package com.gic.cinemas.backend.assertions;

import static org.assertj.core.api.Assertions.assertThat;

import com.gic.cinemas.common.dto.response.ErrorResponse;

/**
 * Fluent assertion helper for ErrorResponse.
 *
 * <p>Example: assertThatErrorResponse(response) .hasStatus(400) .hasError("No Available Seats")
 * .hasMessageContaining("fully booked");
 */
public class ErrorResponseAssert {

  private final ErrorResponse actual;

  private ErrorResponseAssert(ErrorResponse actual) {
    this.actual = actual;
  }

  /** Entry point for fluent assertions. */
  public static ErrorResponseAssert assertThatErrorResponse(ErrorResponse actual) {
    return new ErrorResponseAssert(actual);
  }

  public ErrorResponseAssert hasStatus(int expected) {
    assertThat(actual.status()).as("status").isEqualTo(expected);
    return this;
  }

  public ErrorResponseAssert hasError(String expected) {
    assertThat(actual.error()).as("error").isEqualTo(expected);
    return this;
  }

  public ErrorResponseAssert hasMessage(String expected) {
    assertThat(actual.message()).as("message").isEqualTo(expected);
    return this;
  }

  public ErrorResponseAssert hasMessageContaining(String substring) {
    assertThat(actual.message()).as("message").contains(substring);
    return this;
  }
}
