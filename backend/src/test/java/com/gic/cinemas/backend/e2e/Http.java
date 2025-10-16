package com.gic.cinemas.backend.e2e;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

public final class Http {
  private Http() {}

  public static <T> ResponseEntity<T> postJson(
      TestRestTemplate rest, String path, Object body, Class<T> responseType) {
    HttpHeaders h = new HttpHeaders();
    h.setContentType(MediaType.APPLICATION_JSON);
    h.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
    HttpEntity<Object> req = new HttpEntity<>(body, h);
    return rest.postForEntity(path, req, responseType);
  }

  public static <T> ResponseEntity<T> get(TestRestTemplate rest, String path, Class<T> type) {
    return rest.getForEntity(path, type);
  }

  public static ResponseEntity<String> postNoBody(TestRestTemplate rest, String path) {
    HttpHeaders h = new HttpHeaders();
    h.setContentType(MediaType.APPLICATION_JSON);
    return rest.postForEntity(path, new HttpEntity<>("", h), String.class);
  }
}
