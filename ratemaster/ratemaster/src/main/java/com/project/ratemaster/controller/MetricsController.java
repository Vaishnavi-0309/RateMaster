package com.project.ratemaster.controller;

import com.project.ratemaster.dto.MetricsResponse;
import com.project.ratemaster.service.MetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/metrics")
@RequiredArgsConstructor
public class MetricsController {

    private final MetricsService metricsService;

    /* Total requests, block rate today */
    @GetMapping("/summary")
    public ResponseEntity<MetricsResponse> getSummary(){
        return ResponseEntity.ok(metricsService.getSummary());
    }

    /* Top 10 clients by request count */
    @GetMapping("/top-consumers")
    public ResponseEntity<List<Map<String,Object>>> getTopConsumers(){
        return ResponseEntity.ok(metricsService.getTopConsumers());
    }

    /* All blocked requests today */
    @GetMapping("/blocked")
    public ResponseEntity<List<Map<String,Object>>> getBlockedRequests(){
        return ResponseEntity.ok(metricsService.getBlockedRequests());
    }
}
