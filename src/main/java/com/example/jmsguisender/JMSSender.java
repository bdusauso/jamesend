package com.example.jmsguisender;

import jakarta.jms.*;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class JMSSender {
    
    private final Map<String, Connection> connections = new ConcurrentHashMap<>();
    
    public void sendMessage(String brokerURL, String username, String password, String destinationName, String messageText, boolean isTopic) throws JMSException {
        Connection connection = null;
        Session session = null;
        MessageProducer producer = null;
        
        try {
            // Get or create connection
            connection = getConnection(brokerURL, username, password);
            
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
            
            // Add some useful headers
            message.setStringProperty("contentType", "application/json");
            message.setLongProperty("timestamp", System.currentTimeMillis());
            message.setStringProperty("sender", "JMS-GUI-Sender");
            
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
    
    private Connection getConnection(String brokerURL, String username, String password) throws JMSException {
        String connectionKey = brokerURL + "|" + (username != null ? username : "");
        
        return connections.computeIfAbsent(connectionKey, key -> {
            try {
                jakarta.jms.ConnectionFactory factory = new ActiveMQConnectionFactory(brokerURL);
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