package com.project.ratemaster.service;

import com.project.ratemaster.dto.RegisterResponse;
import com.project.ratemaster.dto.RegisterRequest;
import com.project.ratemaster.model.ApiClient;
import com.project.ratemaster.repository.ApiClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiClientService {

    private final ApiClientRepository apiClientRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterResponse register(RegisterRequest request){

        if(apiClientRepository.findByEmail(request.getEmail()).isPresent()){
            throw new RuntimeException("Email already registered");
        }

        String rawApiKey=generateApiKey();
        String prefix=rawApiKey.substring(0,8);
        String hashedKey=passwordEncoder.encode(rawApiKey);

        ApiClient apiClient=ApiClient.builder()
                .email(request.getEmail())
                .hashedPassword(passwordEncoder.encode(request.getPassword()))
                .apiKeyHash(hashedKey)
                .apiKeyPrefix(prefix)
                .tier(request.getTier())
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        apiClientRepository.save(apiClient);
        log.info("Client registered : {}",request.getEmail());

        return RegisterResponse.builder()
                .apiKey(rawApiKey)
                .tier(request.getTier().name())
                .message("Save your API key - it wont be shown again!")
                .build();
    }

    public RegisterResponse regenerateKey(String email){
        ApiClient apiClient=apiClientRepository.findByEmail(email)
                .orElseThrow(()->new RuntimeException("Client not found"));
        String rawApiKey=generateApiKey();
        apiClient.setApiKeyHash(passwordEncoder.encode(rawApiKey));
        apiClient.setApiKeyPrefix(rawApiKey.substring(0,8));
        apiClientRepository.save(apiClient);

        return RegisterResponse.builder()
                .apiKey(rawApiKey)
                .tier(apiClient.getTier().name())
                .message("New key generated - save it !")
                .build();

    }

    private String generateApiKey() {
        return "rm_live_"+ UUID.randomUUID().toString().replace("-","");
    }
}
