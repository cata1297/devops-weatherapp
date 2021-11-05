package com.example.demo.service;

import com.example.demo.pojo.CurrentWeather;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplate;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Objects;

@Service
public class WeatherService {

    private static final Logger logger = LogManager.getLogger(WeatherService.class);

    // country param accepts ISO 3166-1 alpha-3 codes only (e.g. "hun", "lva", "deu")
    private static final String WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather?q={city},{country}&APPID={key}&units=metric";

    @Value("${api.openweathermap.key}")
    private String apiKey;
    @Value("${api.openweathermap.city}")
    private String city;
    @Value("${app.is.everything.ok}")
    private boolean isEverythingOk;


    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public WeatherService(RestTemplateBuilder restTemplateBuilder, ObjectMapper objectMapper) {

        this.restTemplate = restTemplateBuilder.build();
        this.objectMapper = objectMapper;
    }

    public CurrentWeather getCurrentWeather() {

        if (!isEverythingOk) {
            logger.error("There is still something FISHY. Maybe try logs in DEBUG level");
            logger.debug("app.is.everything.ok should be true in the application.properties");  
        }

        URI url = new UriTemplate(WEATHER_URL).expand(city, "hu", apiKey);
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        return convert(response);
    }

    private CurrentWeather convert(ResponseEntity<String> response) {
        try {

            JsonNode root = objectMapper.readTree(Objects.requireNonNull(response.getBody()));

            return new CurrentWeather(root.path("name").asText(),
                    root.path("weather").get(0).path("main").asText(),
                    BigDecimal.valueOf(root.path("main").path("temp").asDouble()),
                    BigDecimal.valueOf(root.path("main").path("feels_like").asDouble()),
                    BigDecimal.valueOf(root.path("wind").path("speed").asDouble()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing JSON", e);
        }
    }
}
