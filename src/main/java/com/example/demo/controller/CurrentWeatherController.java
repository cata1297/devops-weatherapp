package com.example.demo.controller;

import com.example.demo.service.WeatherService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.HttpClientErrorException;

@Controller
public class CurrentWeatherController {

    private static final Logger logger = LogManager.getLogger(CurrentWeatherController.class);

    private final WeatherService weatherService;

    public CurrentWeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/")
    public String getCurrentWeather(Model model) {
        try {
            model.addAttribute("currentWeather", weatherService.getCurrentWeather());
            logger.info("Weather data loaded properly.");
            return "current-weather";
        } catch (HttpClientErrorException e) {
            throw new HttpClientErrorException(e.getStatusCode(), e.getStatusText());
        }
    }
}
