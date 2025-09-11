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

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class JMSGuiController {

    private TextField serverAddressField;
    private TextField usernameField;
    private PasswordField passwordField;
    private TextField destinationNameField;
    private RadioButton topicRadio;
    private RadioButton queueRadio;
    private CheckBox useSslCheckBox;
    private TextField trustStorePathField;
    private PasswordField trustStorePasswordField;
    private TextField keyStorePathField;
    private PasswordField keyStorePasswordField;
    private CheckBox skipCertValidationCheckBox;
    private Button browseTrustStoreButton;
    private Button browseKeyStoreButton;
    private TextArea payloadArea;
    private TableView<HeaderEntry> headersTable;
    private ObservableList<HeaderEntry> headersList;
    private Button addHeaderButton;
    private Button removeHeaderButton;
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

        // SSL Configuration Section
        Label sslLabel = new Label("SSL Configuration:");
        sslLabel.setStyle("-fx-font-weight: bold;");
        
        useSslCheckBox = new CheckBox("Use SSL/TLS");
        useSslCheckBox.setOnAction(e -> toggleSslFields());
        
        trustStorePathField = new TextField();
        trustStorePathField.setPromptText("Path to truststore or PEM certificate (optional)");
        trustStorePathField.setDisable(true);
        
        browseTrustStoreButton = new Button("Browse...");
        browseTrustStoreButton.setOnAction(e -> browseTrustStore());
        browseTrustStoreButton.setDisable(true);
        
        trustStorePasswordField = new PasswordField();
        trustStorePasswordField.setPromptText("Truststore password (not needed for PEM)");
        trustStorePasswordField.setDisable(true);
        
        keyStorePathField = new TextField();
        keyStorePathField.setPromptText("Path to keystore (optional)");
        keyStorePathField.setDisable(true);
        
        browseKeyStoreButton = new Button("Browse...");
        browseKeyStoreButton.setOnAction(e -> browseKeyStore());
        browseKeyStoreButton.setDisable(true);
        
        keyStorePasswordField = new PasswordField();
        keyStorePasswordField.setPromptText("Keystore password");
        keyStorePasswordField.setDisable(true);
        
        skipCertValidationCheckBox = new CheckBox("Skip certificate validation (insecure)");
        skipCertValidationCheckBox.setDisable(true);
        
        GridPane sslGrid = new GridPane();
        sslGrid.setHgap(10);
        sslGrid.setVgap(5);
        sslGrid.add(useSslCheckBox, 0, 0, 2, 1);
        sslGrid.add(new Label("Trust Store/PEM:"), 0, 1);
        sslGrid.add(trustStorePathField, 1, 1);
        sslGrid.add(browseTrustStoreButton, 2, 1);
        sslGrid.add(new Label("Trust Store Password:"), 0, 2);
        sslGrid.add(trustStorePasswordField, 1, 2);
        sslGrid.add(new Label("Key Store:"), 0, 3);
        sslGrid.add(keyStorePathField, 1, 3);
        sslGrid.add(browseKeyStoreButton, 2, 3);
        sslGrid.add(new Label("Key Store Password:"), 0, 4);
        sslGrid.add(keyStorePasswordField, 1, 4);
        sslGrid.add(skipCertValidationCheckBox, 0, 5, 2, 1);
        
        ColumnConstraints sslCol1 = new ColumnConstraints();
        sslCol1.setMinWidth(120);
        ColumnConstraints sslCol2 = new ColumnConstraints();
        sslCol2.setHgrow(Priority.ALWAYS);
        ColumnConstraints sslCol3 = new ColumnConstraints();
        sslCol3.setMinWidth(80);
        sslGrid.getColumnConstraints().addAll(sslCol1, sslCol2, sslCol3);
        
        VBox sslSection = new VBox(8);
        sslSection.getChildren().addAll(sslLabel, sslGrid);

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
        payloadArea.setPromptText("{\n  \"message\": \"Hello World\",\n  \"timestamp\": \"2025-01-01T12:00:00Z\"\n}");
        payloadArea.setPrefRowCount(10);
        payloadArea.setWrapText(true);

        VBox payloadSection = new VBox(5);
        payloadSection.getChildren().addAll(payloadLabel, payloadArea);
        VBox.setVgrow(payloadArea, Priority.ALWAYS);

        // Headers Section
        Label headersLabel = new Label("Custom JMS Headers:");
        headersLabel.setStyle("-fx-font-weight: bold;");
        
        // Initialize headers list and table
        headersList = FXCollections.observableArrayList();
        headersTable = new TableView<>(headersList);
        headersTable.setEditable(true);
        headersTable.setPrefHeight(150);
        
        // Create table columns
        TableColumn<HeaderEntry, String> keyColumn = new TableColumn<>("Header Name");
        keyColumn.setCellValueFactory(cellData -> cellData.getValue().keyProperty());
        keyColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        keyColumn.setOnEditCommit(event -> event.getRowValue().setKey(event.getNewValue()));
        keyColumn.setPrefWidth(150);
        
        TableColumn<HeaderEntry, String> valueColumn = new TableColumn<>("Header Value");
        valueColumn.setCellValueFactory(cellData -> cellData.getValue().valueProperty());
        valueColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        valueColumn.setOnEditCommit(event -> event.getRowValue().setValue(event.getNewValue()));
        valueColumn.setPrefWidth(200);
        
        TableColumn<HeaderEntry, String> typeColumn = new TableColumn<>("Type");
        typeColumn.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        typeColumn.setCellFactory(col -> {
            ComboBox<String> combo = new ComboBox<>();
            combo.getItems().addAll("String", "Integer", "Long", "Boolean", "Double", "Float");
            combo.setValue("String");
            
            TableCell<HeaderEntry, String> cell = new TableCell<HeaderEntry, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        combo.setValue(item);
                        combo.setOnAction(e -> {
                            if (getTableRow() != null && getTableRow().getItem() != null) {
                                getTableRow().getItem().setType(combo.getValue());
                            }
                        });
                        setGraphic(combo);
                    }
                }
            };
            return cell;
        });
        typeColumn.setPrefWidth(100);
        
        headersTable.getColumns().addAll(keyColumn, valueColumn, typeColumn);
        
        // Header buttons
        addHeaderButton = new Button("Add Header");
        addHeaderButton.setOnAction(e -> headersList.add(new HeaderEntry("", "", "String")));
        
        removeHeaderButton = new Button("Remove Selected");
        removeHeaderButton.setOnAction(e -> {
            HeaderEntry selected = headersTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                headersList.remove(selected);
            }
        });
        
        HBox headerButtonBox = new HBox(10);
        headerButtonBox.getChildren().addAll(addHeaderButton, removeHeaderButton);
        
        VBox headersSection = new VBox(5);
        headersSection.getChildren().addAll(headersLabel, headersTable, headerButtonBox);

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
            sslSection,
            new Separator(),
            destSection,
            new Separator(),
            payloadSection,
            new Separator(),
            headersSection,
            buttonBox,
            new Separator(),
            logSection
        );

        VBox.setVgrow(payloadSection, Priority.ALWAYS);

        return new Scene(root, 1024, 768);
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
                
                // Collect custom headers
                Map<String, Object> customHeaders = new HashMap<>();
                for (HeaderEntry header : headersList) {
                    String key = header.getKey();
                    String value = header.getValue();
                    String type = header.getType();
                    
                    if (key != null && !key.trim().isEmpty() && value != null && !value.trim().isEmpty()) {
                        try {
                            Object convertedValue = convertHeaderValue(value.trim(), type);
                            customHeaders.put(key.trim(), convertedValue);
                        } catch (Exception e) {
                            Platform.runLater(() -> logMessage("WARNING: Failed to convert header '" + key + "' with value '" + value + "' to type " + type + ": " + e.getMessage()));
                        }
                    }
                }
                
                jmsSender.sendMessage(serverAddress, username, password, destinationName, payload, isTopic, currentConfig, customHeaders);
                Platform.runLater(() -> {
                    String headerInfo = customHeaders.isEmpty() ? "" : " with " + customHeaders.size() + " custom headers";
                    logMessage("SUCCESS: Message sent successfully" + headerInfo);
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
            
            // Load SSL configuration
            useSslCheckBox.setSelected(currentConfig.isUseSsl());
            trustStorePathField.setText(currentConfig.getTrustStorePath());
            trustStorePasswordField.setText(currentConfig.getTrustStorePassword());
            keyStorePathField.setText(currentConfig.getKeyStorePath());
            keyStorePasswordField.setText(currentConfig.getKeyStorePassword());
            skipCertValidationCheckBox.setSelected(currentConfig.isSkipCertificateValidation());
            
            // Enable/disable SSL fields based on checkbox
            toggleSslFields();
            
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
            
            // Save SSL configuration
            currentConfig.setUseSsl(useSslCheckBox.isSelected());
            currentConfig.setTrustStorePath(trustStorePathField.getText().trim());
            currentConfig.setTrustStorePassword(trustStorePasswordField.getText());
            currentConfig.setKeyStorePath(keyStorePathField.getText().trim());
            currentConfig.setKeyStorePassword(keyStorePasswordField.getText());
            currentConfig.setSkipCertificateValidation(skipCertValidationCheckBox.isSelected());
            
            configManager.saveConfiguration(currentConfig);
            logMessage("Configuration saved successfully");
            
        } catch (Exception e) {
            logMessage("ERROR: Failed to save configuration: " + e.getMessage());
        }
    }
    
    private void toggleSslFields() {
        boolean enableSsl = useSslCheckBox.isSelected();
        
        trustStorePathField.setDisable(!enableSsl);
        browseTrustStoreButton.setDisable(!enableSsl);
        trustStorePasswordField.setDisable(!enableSsl);
        keyStorePathField.setDisable(!enableSsl);
        browseKeyStoreButton.setDisable(!enableSsl);
        keyStorePasswordField.setDisable(!enableSsl);
        skipCertValidationCheckBox.setDisable(!enableSsl);
        
        if (!enableSsl) {
            // Clear SSL fields when disabled
            trustStorePathField.clear();
            trustStorePasswordField.clear();
            keyStorePathField.clear();
            keyStorePasswordField.clear();
            skipCertValidationCheckBox.setSelected(false);
        }
    }
    
    private void browseTrustStore() {
        File selected = browseForFile("Select Trust Store or Certificate", 
            "Certificate Files", "*.jks", "*.p12", "*.keystore", "*.pem", "*.crt", "*.cer", "*.cert");
        if (selected != null) {
            trustStorePathField.setText(selected.getAbsolutePath());
        }
    }
    
    private void browseKeyStore() {
        File selected = browseForFile("Select Key Store", "Key Store Files", "*.jks", "*.p12", "*.keystore");
        if (selected != null) {
            keyStorePathField.setText(selected.getAbsolutePath());
        }
    }
    
    private File browseForFile(String title, String description, String... extensions) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter(description, extensions);
        fileChooser.getExtensionFilters().add(filter);
        
        // Set initial directory to user home
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        
        return fileChooser.showOpenDialog(sendButton.getScene().getWindow());
    }
    
    private Object convertHeaderValue(String value, String type) throws NumberFormatException {
        switch (type) {
            case "String":
                return value;
            case "Integer":
                return Integer.valueOf(value);
            case "Long":
                return Long.valueOf(value);
            case "Boolean":
                return Boolean.valueOf(value);
            case "Double":
                return Double.valueOf(value);
            case "Float":
                return Float.valueOf(value);
            default:
                return value;
        }
    }
    
    // Inner class for header table entries
    public static class HeaderEntry {
        private final SimpleStringProperty key;
        private final SimpleStringProperty value;
        private final SimpleStringProperty type;
        
        public HeaderEntry(String key, String value, String type) {
            this.key = new SimpleStringProperty(key);
            this.value = new SimpleStringProperty(value);
            this.type = new SimpleStringProperty(type);
        }
        
        public String getKey() { return key.get(); }
        public void setKey(String key) { this.key.set(key); }
        public SimpleStringProperty keyProperty() { return key; }
        
        public String getValue() { return value.get(); }
        public void setValue(String value) { this.value.set(value); }
        public SimpleStringProperty valueProperty() { return value; }
        
        public String getType() { return type.get(); }
        public void setType(String type) { this.type.set(type); }
        public SimpleStringProperty typeProperty() { return type; }
    }
}