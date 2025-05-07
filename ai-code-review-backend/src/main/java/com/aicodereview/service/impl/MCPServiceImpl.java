package com.aicodereview.service.impl;

import com.aicodereview.service.MCPService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MCPServiceImpl implements MCPService {

    @Value("${mcp.rules.path}")
    private String rulesPath;

    @Value("${mcp.cache.enabled}")
    private boolean cacheEnabled;

    @Value("${mcp.cache.ttl}")
    private int cacheTtl;

    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    @Override
    @Cacheable(value = "projectContext", key = "#repositoryName", unless = "#result == null")
    public String getProjectContext(String repositoryName) {
        try {
            Path contextPath = Paths.get(rulesPath, repositoryName, "context.yaml");
            if (!Files.exists(contextPath)) {
                return null;
            }

            Map<String, Object> context = yamlMapper.readValue(contextPath.toFile(), Map.class);
            return context.get("description").toString();
        } catch (IOException e) {
            log.error("Error reading project context", e);
            return null;
        }
    }

    @Override
    @Cacheable(value = "projectRules", key = "#repositoryName", unless = "#result == null")
    public Map<String, List<String>> getProjectRules(String repositoryName) {
        try {
            Path rulesPath = Paths.get(this.rulesPath, repositoryName, "rules.yaml");
            if (!Files.exists(rulesPath)) {
                return Collections.emptyMap();
            }

            Map<String, Object> rules = yamlMapper.readValue(rulesPath.toFile(), Map.class);
            Map<String, List<String>> result = new HashMap<>();

            for (Map.Entry<String, Object> entry : rules.entrySet()) {
                if (entry.getValue() instanceof List) {
                    result.put(entry.getKey(), (List<String>) entry.getValue());
                }
            }

            return result;
        } catch (IOException e) {
            log.error("Error reading project rules", e);
            return Collections.emptyMap();
        }
    }

    @Override
    @Cacheable(value = "projectDependencies", key = "#repositoryName", unless = "#result == null")
    public List<String> getProjectDependencies(String repositoryName) {
        try {
            Path depsPath = Paths.get(rulesPath, repositoryName, "dependencies.yaml");
            if (!Files.exists(depsPath)) {
                return Collections.emptyList();
            }

            Map<String, Object> deps = yamlMapper.readValue(depsPath.toFile(), Map.class);
            return (List<String>) deps.get("dependencies");
        } catch (IOException e) {
            log.error("Error reading project dependencies", e);
            return Collections.emptyList();
        }
    }

    @Override
    @Cacheable(value = "projectConfigurations", key = "#repositoryName", unless = "#result == null")
    public Map<String, String> getProjectConfigurations(String repositoryName) {
        try {
            Path configPath = Paths.get(rulesPath, repositoryName, "config.yaml");
            if (!Files.exists(configPath)) {
                return Collections.emptyMap();
            }

            Map<String, Object> config = yamlMapper.readValue(configPath.toFile(), Map.class);
            Map<String, String> result = new HashMap<>();

            for (Map.Entry<String, Object> entry : config.entrySet()) {
                result.put(entry.getKey(), entry.getValue().toString());
            }

            return result;
        } catch (IOException e) {
            log.error("Error reading project configurations", e);
            return Collections.emptyMap();
        }
    }

    @Override
    public void updateProjectContext(String repositoryName, String context) {
        try {
            Path contextPath = Paths.get(rulesPath, repositoryName, "context.yaml");
            Files.createDirectories(contextPath.getParent());

            Map<String, String> contextMap = new HashMap<>();
            contextMap.put("description", context);
            contextMap.put("lastUpdated", new Date().toString());

            yamlMapper.writeValue(contextPath.toFile(), contextMap);
        } catch (IOException e) {
            log.error("Error updating project context", e);
            throw new RuntimeException("Failed to update project context", e);
        }
    }

    @Override
    public void updateProjectRules(String repositoryName, Map<String, List<String>> rules) {
        try {
            Path rulesPath = Paths.get(this.rulesPath, repositoryName, "rules.yaml");
            Files.createDirectories(rulesPath.getParent());

            Map<String, Object> rulesMap = new HashMap<>(rules);
            rulesMap.put("lastUpdated", new Date().toString());

            yamlMapper.writeValue(rulesPath.toFile(), rulesMap);
        } catch (IOException e) {
            log.error("Error updating project rules", e);
            throw new RuntimeException("Failed to update project rules", e);
        }
    }
} 