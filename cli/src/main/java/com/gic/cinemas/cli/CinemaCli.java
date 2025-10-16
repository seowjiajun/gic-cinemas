package com.gic.cinemas.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Scanner;

public class CinemaCli {

  private static final String BASE_URL =
      System.getProperty("api.base", "http://localhost:8080/api");

  public static void main(String[] args) throws Exception {
    Scanner scanner = new Scanner(System.in);
    ObjectMapper mapper = new ObjectMapper();
    CinemaApiClient client = new CinemaApiClient(BASE_URL, mapper);
    CinemaCliService service = new CinemaCliService(client, mapper);
    CinemaCliRunner runner = new CinemaCliRunner(scanner, service);
    runner.run();
  }
}
