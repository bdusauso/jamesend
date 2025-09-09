package com.example.jmsguisender;

import javafx.application.Application;
import javafx.stage.Stage;

public class JMSGuiSenderApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        JMSGuiController controller = new JMSGuiController();
        controller.show(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}