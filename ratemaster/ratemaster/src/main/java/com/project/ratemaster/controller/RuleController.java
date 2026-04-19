package com.project.ratemaster.controller;

import com.project.ratemaster.dto.RuleRequest;
import com.project.ratemaster.dto.RuleResponse;
import com.project.ratemaster.service.RuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rules")
@RequiredArgsConstructor
public class RuleController {
    private final RuleService ruleService;

    @PostMapping
    public ResponseEntity<RuleResponse> createRule(@Valid @RequestBody RuleRequest request){
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ruleService.createRule(request));
    }

    @GetMapping
    public ResponseEntity<List<RuleResponse>> getAllRules(){
        return ResponseEntity.ok(ruleService.getAllRules());
    }

    @PutMapping("/{id}")
    public ResponseEntity<RuleResponse> updateRule(@PathVariable String id,
                                                   @Valid @RequestBody RuleRequest request){
        return ResponseEntity.ok(ruleService.updateRule(id,request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable String id){
        ruleService.deleteRule(id);
        return ResponseEntity.noContent().build();
    }
}
