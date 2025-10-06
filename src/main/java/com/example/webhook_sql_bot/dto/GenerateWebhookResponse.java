package com.example.webhook_sql_bot.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GenerateWebhookResponse {
  @JsonProperty("webhook")
  private String webhook;
  @JsonProperty("webhookUrl")
  private String webhookUrl;
  private String accessToken;
}
