#!/bin/bash

# Run the JMS GUI Sender application
java --add-modules javafx.controls,javafx.fxml --add-opens javafx.graphics/com.sun.javafx.application=ALL-UNNAMED --enable-native-access=javafx.graphics -jar target/jms-gui-sender-1.0.0.jar