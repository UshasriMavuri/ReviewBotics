package com.aicodereview.service;

import java.util.Map;
import java.util.List;

public interface MCPService {
    String getProjectContext(String repositoryName);
    Map<String, List<String>> getProjectRules(String repositoryName);
    List<String> getProjectDependencies(String repositoryName);
    Map<String, String> getProjectConfigurations(String repositoryName);
    void updateProjectContext(String repositoryName, String context);
    void updateProjectRules(String repositoryName, Map<String, List<String>> rules);
} 