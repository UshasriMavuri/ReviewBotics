package com.aicodereview.controller;

import com.aicodereview.service.LLMService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final LLMService llmService;

    @PostMapping("/review")
    public ResponseEntity<String> testCodeReview(@RequestBody String code, @RequestParam(required = false) String context) {
        return ResponseEntity.ok(llmService.generateCodeReview(code, context));
    }

    @PostMapping("/tests")
    public ResponseEntity<List<String>> testTestSuggestions(@RequestBody String code) {
        return ResponseEntity.ok(llmService.generateTestSuggestions(code));
    }

    @PostMapping("/docs")
    public ResponseEntity<List<String>> testDocumentationSuggestions(@RequestBody String code) {
        return ResponseEntity.ok(llmService.generateDocumentationSuggestions(code));
    }

    @PostMapping("/quality")
    public ResponseEntity<Map<String, String>> testCodeQuality(@RequestBody String code) {
        return ResponseEntity.ok(llmService.analyzeCodeQuality(code));
    }

    @PostMapping("/refactor")
    public ResponseEntity<String> testRefactoringSuggestions(@RequestBody String code) {
        return ResponseEntity.ok(llmService.generateRefactoringSuggestions(code));
    }

    @GetMapping("/health")
    public ResponseEntity<Boolean> testHealth() {
        return ResponseEntity.ok(llmService.isAvailable());
    }
}