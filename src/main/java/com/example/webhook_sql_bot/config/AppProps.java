package com.example.webhook_sql_bot.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter @Setter
@Component
@ConfigurationProperties(prefix = "app.identity")
public class AppProps {
  private String name;
  private String regNo;
  private String email;
}
