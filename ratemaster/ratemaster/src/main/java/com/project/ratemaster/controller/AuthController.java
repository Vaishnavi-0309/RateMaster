package com.project.ratemaster.controller;

import com.project.ratemaster.dto.RateLimitResult;
import com.project.ratemaster.dto.RegisterResponse;
import com.project.ratemaster.dto.RegisterRequest;
import com.project.ratemaster.model.Tier;
import com.project.ratemaster.service.ApiClientService;
import com.project.ratemaster.service.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final ApiClientService apiClientService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request){
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(apiClientService.register(request));
    }

    @PostMapping("/regenrate-key")
    public ResponseEntity<RegisterResponse> regenerateKey(@RequestParam String email){
        return ResponseEntity.ok(apiClientService.regenerateKey(email));
    }

}
