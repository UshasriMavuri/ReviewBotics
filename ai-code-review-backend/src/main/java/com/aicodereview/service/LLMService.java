package com.aicodereview.service;

import java.util.List;
import java.util.Map;

public interface LLMService {
    String generateCodeReview(String code, String context);
    List<String> generateTestSuggestions(String code);
    List<String> generateDocumentationSuggestions(String code);
    Map<String, String> analyzeCodeQuality(String code);
    String generateRefactoringSuggestions(String code);
    boolean isAvailable();
} 