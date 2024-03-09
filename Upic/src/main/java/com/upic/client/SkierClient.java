package com.upic.client;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;

public class SkierClient {

  private HttpClient client;
  private String baseUrl;

  public SkierClient(String baseUrl) {
    this.client = HttpClient.newHttpClient();
    this.baseUrl = baseUrl;
  }

  public int sendPostRequest(String endpoint, String requestBody)
      throws IOException, InterruptedException {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + endpoint))
        .header("Content-Type", "application/json")
        .POST(BodyPublishers.ofString(requestBody))
        .build();

    HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
    return response.statusCode();
  }
}