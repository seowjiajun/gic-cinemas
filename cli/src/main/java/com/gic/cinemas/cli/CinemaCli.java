package com.gic.cinemas.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Scanner;

public class CinemaCli {

  private static final String DEFAULT_BASE = "http://localhost:8080/api";

  public static void main(String[] args) {
    String base =
        // program arg: --api.base=...
        findArg(args, "--api.base=")
            .orElse(
                // env var: CINEMA_API_BASE
                System.getenv()
                    .getOrDefault(
                        "CINEMA_API_BASE",
                        // JVM prop: -Dapi.base=...
                        System.getProperty("api.base", DEFAULT_BASE)));

    try (Scanner scanner = new Scanner(System.in)) {
      ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
      CinemaApiClient client = new CinemaApiClient(base, mapper);
      CinemaCliService service = new CinemaCliService(client, mapper);
      CinemaCliRunner runner = new CinemaCliRunner(scanner, service);
      runner.run();
    } catch (Exception e) {
      System.err.println("CLI failed: " + e.getMessage());
      e.printStackTrace(System.err);
      System.exit(1);
    }
  }

  private static java.util.Optional<String> findArg(String[] args, String prefix) {
    for (String a : args) {
      if (a != null && a.startsWith(prefix)) {
        return java.util.Optional.of(a.substring(prefix.length()));
      }
    }
    return java.util.Optional.empty();
  }
}
