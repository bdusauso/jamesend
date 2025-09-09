package com.example.jmsguisender;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class JMSGuiController {

    private TextField serverAddressField;
    private TextField usernameField;
    private PasswordField passwordField;
    private TextField destinationNameField;
    private RadioButton topicRadio;
    private RadioButton queueRadio;
    private TextArea payloadArea;
    private Button sendButton;
    private Button saveConfigButton;
    private TextArea logArea;
    private JMSSender jmsSender;
    private ConfigurationManager configManager;
    private ServerConfiguration currentConfig;

    public void show(Stage primaryStage) {
        jmsSender = new JMSSender();
        configManager = new ConfigurationManager();
        
        primaryStage.setTitle("JMS Message Sender");
        primaryStage.setScene(createScene());
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(650);
        primaryStage.show();

        // Load saved configuration
        loadConfiguration();

        primaryStage.setOnCloseRequest(e -> {
            saveCurrentConfiguration();
            jmsSender.close();
            Platform.exit();
        });
    }

    private Scene createScene() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        // Server Connection Section
        Label serverLabel = new Label("Server Connection:");
        serverLabel.setStyle("-fx-font-weight: bold;");
        
        serverAddressField = new TextField();
        serverAddressField.setPromptText("tcp://localhost:61616");
        serverAddressField.setText("tcp://localhost:61616");
        
        usernameField = new TextField();
        usernameField.setPromptText("Username (optional)");
        
        passwordField = new PasswordField();
        passwordField.setPromptText("Password (optional)");
        
        GridPane serverGrid = new GridPane();
        serverGrid.setHgap(10);
        serverGrid.setVgap(5);
        serverGrid.add(new Label("Server URL:"), 0, 0);
        serverGrid.add(serverAddressField, 1, 0);
        serverGrid.add(new Label("Username:"), 0, 1);
        serverGrid.add(usernameField, 1, 1);
        serverGrid.add(new Label("Password:"), 0, 2);
        serverGrid.add(passwordField, 1, 2);
        
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(80);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        serverGrid.getColumnConstraints().addAll(col1, col2);
        
        VBox serverSection = new VBox(8);
        serverSection.getChildren().addAll(serverLabel, serverGrid);

        // Destination Section
        Label destLabel = new Label("Destination:");
        destinationNameField = new TextField();
        destinationNameField.setPromptText("queue.test or topic.test");

        ToggleGroup destTypeGroup = new ToggleGroup();
        topicRadio = new RadioButton("Topic");
        queueRadio = new RadioButton("Queue");
        topicRadio.setToggleGroup(destTypeGroup);
        queueRadio.setToggleGroup(destTypeGroup);
        queueRadio.setSelected(true);

        HBox radioBox = new HBox(10);
        radioBox.getChildren().addAll(queueRadio, topicRadio);

        VBox destSection = new VBox(5);
        destSection.getChildren().addAll(destLabel, destinationNameField, radioBox);

        // Payload Section
        Label payloadLabel = new Label("Message Payload (JSON):");
        payloadArea = new TextArea();
        payloadArea.setPromptText("{\n  \"message\": \"Hello World\",\n  \"timestamp\": \"2024-01-01T12:00:00Z\"\n}");
        payloadArea.setPrefRowCount(10);
        payloadArea.setWrapText(true);

        VBox payloadSection = new VBox(5);
        payloadSection.getChildren().addAll(payloadLabel, payloadArea);
        VBox.setVgrow(payloadArea, Priority.ALWAYS);

        // Action Buttons
        sendButton = new Button("Send Message");
        sendButton.setPrefWidth(120);
        sendButton.setOnAction(e -> sendMessage());
        
        saveConfigButton = new Button("Save Config");
        saveConfigButton.setPrefWidth(100);
        saveConfigButton.setOnAction(e -> saveCurrentConfiguration());

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(sendButton, saveConfigButton);

        // Log Area
        Label logLabel = new Label("Log:");
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefRowCount(6);
        logArea.setWrapText(true);
        logArea.setStyle("-fx-font-family: monospace;");

        VBox logSection = new VBox(5);
        logSection.getChildren().addAll(logLabel, logArea);

        // Add all sections to root
        root.getChildren().addAll(
            serverSection,
            new Separator(),
            destSection,
            new Separator(),
            payloadSection,
            buttonBox,
            new Separator(),
            logSection
        );

        VBox.setVgrow(payloadSection, Priority.ALWAYS);

        return new Scene(root, 800, 600);
    }

    private void sendMessage() {
        String serverAddress = serverAddressField.getText().trim();
        String destinationName = destinationNameField.getText().trim();
        String payload = payloadArea.getText().trim();
        boolean isTopic = topicRadio.isSelected();

        if (serverAddress.isEmpty()) {
            logMessage("ERROR: Server address is required");
            return;
        }

        if (destinationName.isEmpty()) {
            logMessage("ERROR: Destination name is required");
            return;
        }

        if (payload.isEmpty()) {
            logMessage("ERROR: Message payload is required");
            return;
        }

        sendButton.setDisable(true);
        logMessage("Sending message to " + (isTopic ? "topic" : "queue") + " '" + destinationName + "' on server " + serverAddress);

        // Run in background thread to avoid blocking UI
        Thread sendThread = new Thread(() -> {
            try {
                String username = usernameField.getText().trim();
                String password = passwordField.getText();
                jmsSender.sendMessage(serverAddress, username, password, destinationName, payload, isTopic);
                Platform.runLater(() -> {
                    logMessage("SUCCESS: Message sent successfully");
                    sendButton.setDisable(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    logMessage("ERROR: " + e.getMessage());
                    sendButton.setDisable(false);
                });
            }
        });

        sendThread.setDaemon(true);
        sendThread.start();
    }

    private void logMessage(String message) {
        String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String logEntry = "[" + timestamp + "] " + message + "\n";
        logArea.appendText(logEntry);
    }
    
    private void loadConfiguration() {
        try {
            currentConfig = configManager.loadConfiguration();
            
            // Apply loaded configuration to UI
            serverAddressField.setText(currentConfig.getServerUrl());
            usernameField.setText(currentConfig.getUsername());
            passwordField.setText(currentConfig.getPassword());
            destinationNameField.setText(currentConfig.getLastDestination());
            
            if (currentConfig.isTopicSelected()) {
                topicRadio.setSelected(true);
            } else {
                queueRadio.setSelected(true);
            }
            
            if (configManager.configurationExists()) {
                logMessage("Configuration loaded from: " + configManager.getConfigurationPath());
            } else {
                logMessage("Using default configuration (none saved)");
            }
            
        } catch (Exception e) {
            logMessage("ERROR: Failed to load configuration: " + e.getMessage());
            currentConfig = new ServerConfiguration();
        }
    }
    
    private void saveCurrentConfiguration() {
        try {
            currentConfig.setServerUrl(serverAddressField.getText().trim());
            currentConfig.setUsername(usernameField.getText().trim());
            currentConfig.setPassword(passwordField.getText());
            currentConfig.setLastDestination(destinationNameField.getText().trim());
            currentConfig.setTopicSelected(topicRadio.isSelected());
            
            configManager.saveConfiguration(currentConfig);
            logMessage("Configuration saved successfully");
            
        } catch (Exception e) {
            logMessage("ERROR: Failed to save configuration: " + e.getMessage());
        }
    }
}