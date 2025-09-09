/*
 * Copyright 2025 Bruno Dusausoy <bruno.dusausoy@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.jmsguisender;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

public class ConfigurationManager {
    
    private static final String CONFIG_DIR = System.getProperty("user.home") + File.separator + ".jms-gui-sender";
    private static final String CONFIG_FILE = "config.json";
    private static final Path CONFIG_PATH = Paths.get(CONFIG_DIR, CONFIG_FILE);
    
    private final ObjectMapper objectMapper;
    
    public ConfigurationManager() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        ensureConfigDirectoryExists();
    }
    
    private void ensureConfigDirectoryExists() {
        try {
            Path configDir = Paths.get(CONFIG_DIR);
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }
        } catch (IOException e) {
            System.err.println("Failed to create config directory: " + e.getMessage());
        }
    }
    
    public ServerConfiguration loadConfiguration() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                String jsonContent = Files.readString(CONFIG_PATH);
                ServerConfiguration config = objectMapper.readValue(jsonContent, ServerConfiguration.class);
                
                // Decode password if it exists
                if (config.getPassword() != null && !config.getPassword().isEmpty()) {
                    try {
                        String decodedPassword = new String(Base64.getDecoder().decode(config.getPassword()));
                        config.setPassword(decodedPassword);
                    } catch (IllegalArgumentException e) {
                        // Password might not be encoded, leave as is
                    }
                }
                
                return config;
            }
        } catch (IOException e) {
            System.err.println("Failed to load configuration: " + e.getMessage());
        }
        
        // Return default configuration if loading fails
        return new ServerConfiguration();
    }
    
    public void saveConfiguration(ServerConfiguration config) {
        try {
            // Create a copy for saving with encoded password
            ServerConfiguration configToSave = new ServerConfiguration(
                config.getServerUrl(),
                config.getUsername(),
                config.getPassword(),
                config.getLastDestination(),
                config.isTopicSelected()
            );
            
            // Encode password for storage (simple Base64 - not for high security)
            if (configToSave.getPassword() != null && !configToSave.getPassword().isEmpty()) {
                String encodedPassword = Base64.getEncoder().encodeToString(
                    configToSave.getPassword().getBytes()
                );
                configToSave.setPassword(encodedPassword);
            }
            
            String jsonContent = objectMapper.writeValueAsString(configToSave);
            Files.writeString(CONFIG_PATH, jsonContent);
            
        } catch (IOException e) {
            System.err.println("Failed to save configuration: " + e.getMessage());
        }
    }
    
    public void clearConfiguration() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                Files.delete(CONFIG_PATH);
            }
        } catch (IOException e) {
            System.err.println("Failed to clear configuration: " + e.getMessage());
        }
    }
    
    public boolean configurationExists() {
        return Files.exists(CONFIG_PATH);
    }
    
    public String getConfigurationPath() {
        return CONFIG_PATH.toString();
    }
}