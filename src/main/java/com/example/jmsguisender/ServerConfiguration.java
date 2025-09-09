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