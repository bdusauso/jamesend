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

import jakarta.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class JMSSender {
    
    private final Map<String, Connection> connections = new ConcurrentHashMap<>();
    
    public void sendMessage(String brokerURL, String username, String password, String destinationName, String messageText, boolean isTopic, ServerConfiguration sslConfig, Map<String, Object> customHeaders) throws JMSException {
        Connection connection = null;
        Session session = null;
        MessageProducer producer = null;
        
        try {
            // Get or create connection
            connection = getConnection(brokerURL, username, password, sslConfig);
            
            // Create session
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            
            // Create destination
            Destination destination;
            if (isTopic) {
                destination = session.createTopic(destinationName);
            } else {
                destination = session.createQueue(destinationName);
            }
            
            // Create producer
            producer = session.createProducer(destination);
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            
            // Create and send message
            TextMessage message = session.createTextMessage(messageText);
            
            // Add default headers
            message.setStringProperty("contentType", "application/json");
            message.setLongProperty("timestamp", System.currentTimeMillis());
            message.setStringProperty("sender", "JMS-GUI-Sender");
            
            // Add custom headers if provided
            if (customHeaders != null && !customHeaders.isEmpty()) {
                for (Map.Entry<String, Object> header : customHeaders.entrySet()) {
                    String key = header.getKey();
                    Object value = header.getValue();
                    
                    if (value instanceof String) {
                        message.setStringProperty(key, (String) value);
                    } else if (value instanceof Integer) {
                        message.setIntProperty(key, (Integer) value);
                    } else if (value instanceof Long) {
                        message.setLongProperty(key, (Long) value);
                    } else if (value instanceof Boolean) {
                        message.setBooleanProperty(key, (Boolean) value);
                    } else if (value instanceof Double) {
                        message.setDoubleProperty(key, (Double) value);
                    } else if (value instanceof Float) {
                        message.setFloatProperty(key, (Float) value);
                    } else if (value != null) {
                        // Convert to string as fallback
                        message.setStringProperty(key, value.toString());
                    }
                }
            }
            
            producer.send(message);
            
        } finally {
            // Clean up resources (but keep connection open for reuse)
            if (producer != null) {
                try {
                    producer.close();
                } catch (JMSException e) {
                    // Log but don't fail
                }
            }
            if (session != null) {
                try {
                    session.close();
                } catch (JMSException e) {
                    // Log but don't fail
                }
            }
        }
    }
    
    private Connection getConnection(String brokerURL, String username, String password, ServerConfiguration sslConfig) throws JMSException {
        String connectionKey = brokerURL + "|" + (username != null ? username : "") + "|ssl:" + (sslConfig != null && sslConfig.isUseSsl());
        
        return connections.computeIfAbsent(connectionKey, key -> {
            try {
                ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerURL);
                
                // Configure SSL if enabled
                if (sslConfig != null && sslConfig.isUseSsl()) {
                    try {
                        javax.net.ssl.SSLContext sslContext = SSLContextHelper.createSSLContext(sslConfig);
                        if (sslContext != null) {
                            // For ActiveMQ Classic, we need to set trust store system properties
                            // as it doesn't have direct SSL context configuration in the same way as Artemis
                            configureSystemSSLProperties(sslConfig);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to configure SSL: " + e.getMessage(), e);
                    }
                }
                
                Connection conn;
                
                if (username != null && !username.trim().isEmpty() && 
                    password != null && !password.trim().isEmpty()) {
                    conn = factory.createConnection(username.trim(), password);
                } else {
                    conn = factory.createConnection();
                }
                
                conn.start();
                return conn;
            } catch (JMSException e) {
                throw new RuntimeException("Failed to create connection to " + brokerURL + 
                    (username != null && !username.trim().isEmpty() ? " with user " + username : ""), e);
            }
        });
    }
    
    private void configureSystemSSLProperties(ServerConfiguration sslConfig) {
        if (sslConfig.getTrustStorePath() != null && !sslConfig.getTrustStorePath().trim().isEmpty()) {
            System.setProperty("javax.net.ssl.trustStore", sslConfig.getTrustStorePath());
            if (sslConfig.getTrustStorePassword() != null && !sslConfig.getTrustStorePassword().trim().isEmpty()) {
                System.setProperty("javax.net.ssl.trustStorePassword", sslConfig.getTrustStorePassword());
            }
            
            // Detect truststore type
            String trustStoreType = detectKeyStoreType(sslConfig.getTrustStorePath());
            System.setProperty("javax.net.ssl.trustStoreType", trustStoreType);
        }
        
        if (sslConfig.getKeyStorePath() != null && !sslConfig.getKeyStorePath().trim().isEmpty()) {
            System.setProperty("javax.net.ssl.keyStore", sslConfig.getKeyStorePath());
            if (sslConfig.getKeyStorePassword() != null && !sslConfig.getKeyStorePassword().trim().isEmpty()) {
                System.setProperty("javax.net.ssl.keyStorePassword", sslConfig.getKeyStorePassword());
            }
            
            // Detect keystore type
            String keyStoreType = detectKeyStoreType(sslConfig.getKeyStorePath());
            System.setProperty("javax.net.ssl.keyStoreType", keyStoreType);
        }
        
        if (sslConfig.isSkipCertificateValidation()) {
            System.setProperty("com.sun.net.ssl.checkRevocation", "false");
            System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");
        }
    }
    
    private String detectKeyStoreType(String path) {
        String lowerPath = path.toLowerCase();
        if (lowerPath.endsWith(".p12") || lowerPath.endsWith(".pfx")) {
            return "PKCS12";
        } else if (lowerPath.endsWith(".jks")) {
            return "JKS";
        } else {
            return "JKS"; // Default
        }
    }
    
    public void close() {
        for (Connection connection : connections.values()) {
            try {
                connection.close();
            } catch (JMSException e) {
                // Log but continue closing others
            }
        }
        connections.clear();
    }
}