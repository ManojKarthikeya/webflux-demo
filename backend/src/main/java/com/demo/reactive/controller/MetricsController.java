package com.demo.reactive.controller;

import com.demo.reactive.model.MetricsSnapshot;
import com.demo.reactive.service.MetricsService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
@RequestMapping("/api/metrics")
@CrossOrigin(origins = "http://localhost:5173")
public class MetricsController {

    private final MetricsService metricsService;

    public MetricsController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @GetMapping
    public Mono<MetricsSnapshot> getCurrentMetrics() {
        return metricsService.getCurrentMetrics();
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<MetricsSnapshot> streamMetrics() {
        return Flux.interval(Duration.ofSeconds(2))
                .flatMap(tick -> metricsService.getCurrentMetrics());
    }
}
