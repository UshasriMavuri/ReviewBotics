package com.aicodereview.service;

public interface LLMService {
    String generateCodeReview(String diff, String context);
    String analyzeCodeQuality(String code);
    String generateRefactoringSuggestions(String code);
    String generateTestSuggestions(String code);
    String generateDocumentationSuggestions(String code);
    boolean isAvailable();
} 