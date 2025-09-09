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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ServerConfiguration {
    
    @JsonProperty("serverUrl")
    private String serverUrl;
    
    @JsonProperty("username")
    private String username;
    
    @JsonProperty("password")
    private String password;
    
    @JsonProperty("lastDestination")
    private String lastDestination;
    
    @JsonProperty("isTopicSelected")
    private boolean isTopicSelected;
    
    public ServerConfiguration() {
        // Default values
        this.serverUrl = "tcp://localhost:61616";
        this.username = "";
        this.password = "";
        this.lastDestination = "";
        this.isTopicSelected = false;
    }
    
    public ServerConfiguration(String serverUrl, String username, String password, 
                             String lastDestination, boolean isTopicSelected) {
        this.serverUrl = serverUrl;
        this.username = username;
        this.password = password;
        this.lastDestination = lastDestination;
        this.isTopicSelected = isTopicSelected;
    }
    
    // Getters and Setters
    public String getServerUrl() {
        return serverUrl;
    }
    
    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getLastDestination() {
        return lastDestination;
    }
    
    public void setLastDestination(String lastDestination) {
        this.lastDestination = lastDestination;
    }
    
    public boolean isTopicSelected() {
        return isTopicSelected;
    }
    
    public void setTopicSelected(boolean topicSelected) {
        this.isTopicSelected = topicSelected;
    }
    
    public boolean hasCredentials() {
        return username != null && !username.trim().isEmpty() && 
               password != null && !password.trim().isEmpty();
    }
    
    @Override
    public String toString() {
        return "ServerConfiguration{" +
                "serverUrl='" + serverUrl + '\'' +
                ", username='" + username + '\'' +
                ", password='***'" +
                ", lastDestination='" + lastDestination + '\'' +
                ", isTopicSelected=" + isTopicSelected +
                '}';
    }
}