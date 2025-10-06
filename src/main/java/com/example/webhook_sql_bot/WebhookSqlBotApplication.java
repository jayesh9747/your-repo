package com.example.webhook_sql_bot.config;


import com.example.webhook_sql_bot.config.AppProps;
import com.example.webhook_sql_bot.config.RemoteProps;
import com.example.webhook_sql_bot.dto.GenerateWebhookRequest;
import com.example.webhook_sql_bot.dto.GenerateWebhookResponse;
import com.example.webhook_sql_bot.dto.FinalQueryPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootApplication
@RequiredArgsConstructor
public class WebhookSqlBotApplication {

  private final RestTemplate restTemplate;
  private final AppProps appProps;
  private final RemoteProps remoteProps;

  public static void main(String[] args) {
    SpringApplication.run(WebhookSqlBotApplication.class, args);
  }

  @Bean
  ApplicationRunner runner() {
    return args -> {
      var reqBody = new GenerateWebhookRequest(appProps.getName(), appProps.getRegNo(), appProps.getEmail());
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<GenerateWebhookRequest> request = new HttpEntity<>(reqBody, headers);

      ResponseEntity<GenerateWebhookResponse> response = restTemplate.exchange(
          remoteProps.getGenerateWebhookUrl(), HttpMethod.POST, request, GenerateWebhookResponse.class);

      if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
        throw new RuntimeException("Failed to generate webhook!");
      }

      var data = response.getBody();
      String accessToken = data.getAccessToken();
      String webhookUrl = data.getWebhook() != null ? data.getWebhook() :
          (data.getWebhookUrl() != null ? data.getWebhookUrl() : remoteProps.getTestWebhookFallbackUrl());

      int lastTwo = extractLastTwoDigits(appProps.getRegNo());
      boolean isOdd = (lastTwo % 2) == 1;

      String finalQuery;
      if (isOdd) {
        finalQuery = """
        SELECT
          p.amount AS salary,
          CONCAT(e.first_name, ' ', e.last_name) AS name,
          TIMESTAMPDIFF(YEAR, e.dob, DATE(p.payment_time)) AS age,
          d.department_name
        FROM payments p
        JOIN employee e ON e.emp_id = p.emp_id
        JOIN department d ON d.department_id = e.department
        WHERE DAY(p.payment_time) <> 1
        ORDER BY p.amount DESC
        LIMIT 1;
        """;
      } else {
        finalQuery = "SELECT 1; -- TODO: Add your even question solution";
      }

      Files.writeString(Path.of("finalQuery.sql"), finalQuery);

      HttpHeaders headers2 = new HttpHeaders();
      headers2.setContentType(MediaType.APPLICATION_JSON);
      headers2.set("Authorization", accessToken);
      HttpEntity<FinalQueryPayload> finalRequest = new HttpEntity<>(
          new FinalQueryPayload(finalQuery), headers2);

      ResponseEntity<String> submit = restTemplate.exchange(webhookUrl, HttpMethod.POST, finalRequest, String.class);
      System.out.println("Submitted: " + submit.getStatusCode());
      System.out.println("Server says: " + submit.getBody());
    };
  }

  private static int extractLastTwoDigits(String regNo) {
    String digits = regNo.replaceAll("\\D+", "");
    if (digits.length() < 2) return Integer.parseInt(digits);
    return Integer.parseInt(digits.substring(digits.length() - 2));
  }
}
