package com.aicodereview.util;

import org.springframework.stereotype.Component;
import java.util.regex.Pattern;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

@Component
public class SecurityUtil {
    
    // Patterns for input validation
    private static final Pattern GITHUB_REPO_PATTERN = Pattern.compile("^[a-zA-Z0-9][a-zA-Z0-9-_.]*/[a-zA-Z0-9][a-zA-Z0-9-_.]*$");
    private static final Pattern PR_NUMBER_PATTERN = Pattern.compile("^[0-9]+$");
    
    // List of potentially dangerous commands/patterns
    private static final List<String> DANGEROUS_PATTERNS = Arrays.asList(
        "rm -rf", "sudo", "chmod", "chown", "wget", "curl", "bash", "sh", "exec",
        "system(", "eval(", "Runtime.exec", "ProcessBuilder", "ScriptEngine",
        "<script>", "javascript:", "onerror=", "onload=", "onclick="
    );

    // List of sensitive data patterns
    private static final List<Pattern> SENSITIVE_PATTERNS = Arrays.asList(
        Pattern.compile("(?i)(password|secret|key|token)\\s*[=:]\\s*['\"][^'\"]+['\"]"),
        Pattern.compile("(?i)(api[_-]?key|access[_-]?token)\\s*[=:]\\s*['\"][^'\"]+['\"]"),
        Pattern.compile("(?i)(aws[_-]?key|aws[_-]?secret)\\s*[=:]\\s*['\"][^'\"]+['\"]")
    );

    /**
     * Sanitize repository name input
     */
    public String sanitizeRepositoryName(String repoName) {
        if (repoName == null || repoName.trim().isEmpty()) {
            throw new IllegalArgumentException("Repository name cannot be empty");
        }
        
        String sanitized = repoName.trim();
        if (!GITHUB_REPO_PATTERN.matcher(sanitized).matches()) {
            throw new IllegalArgumentException("Invalid repository name format");
        }
        
        return sanitized;
    }

    /**
     * Sanitize PR number input
     */
    public String sanitizePRNumber(String prNumber) {
        if (prNumber == null || prNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("PR number cannot be empty");
        }
        
        String sanitized = prNumber.trim();
        if (!PR_NUMBER_PATTERN.matcher(sanitized).matches()) {
            throw new IllegalArgumentException("Invalid PR number format");
        }
        
        return sanitized;
    }

    /**
     * Sanitize code content before sending to LLM
     */
    public String sanitizeCodeContent(String code) {
        if (code == null) {
            return "";
        }

        // Remove any null bytes
        String sanitized = code.replace("\0", "");
        
        // Check for dangerous patterns
        for (String pattern : DANGEROUS_PATTERNS) {
            if (sanitized.toLowerCase().contains(pattern.toLowerCase())) {
                throw new SecurityException("Code contains potentially dangerous patterns");
            }
        }

        // Remove sensitive data
        for (Pattern pattern : SENSITIVE_PATTERNS) {
            sanitized = pattern.matcher(sanitized).replaceAll("$1=***REDACTED***");
        }

        return sanitized;
    }

    /**
     * Filter LLM response for security
     */
    public String filterLLMResponse(String response) {
        if (response == null) {
            return "";
        }

        String filtered = response;

        // Remove any null bytes
        filtered = filtered.replace("\0", "");

        // Remove any HTML/script tags
        filtered = filtered.replaceAll("<[^>]*>", "");

        // Remove any dangerous patterns
        for (String pattern : DANGEROUS_PATTERNS) {
            filtered = filtered.replaceAll("(?i)" + Pattern.quote(pattern), "***REDACTED***");
        }

        // Remove any sensitive data patterns
        for (Pattern pattern : SENSITIVE_PATTERNS) {
            filtered = pattern.matcher(filtered).replaceAll("$1=***REDACTED***");
        }

        // Remove any command injection attempts
        filtered = filtered.replaceAll("`.*?`", "***REDACTED***");
        filtered = filtered.replaceAll("\\$\\{.*?\\}", "***REDACTED***");

        return filtered;
    }

    /**
     * Validate and sanitize file paths
     */
    public String sanitizeFilePath(String path) {
        if (path == null) {
            return "";
        }

        // Remove any null bytes
        String sanitized = path.replace("\0", "");

        // Remove any directory traversal attempts
        sanitized = sanitized.replaceAll("\\.\\./", "");
        sanitized = sanitized.replaceAll("\\.\\\\", "");

        // Remove any absolute paths
        if (sanitized.startsWith("/") || sanitized.matches("^[A-Za-z]:\\\\")) {
            throw new SecurityException("Absolute paths are not allowed");
        }

        return sanitized;
    }
} 