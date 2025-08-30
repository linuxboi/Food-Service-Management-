package com.mycompany.mavenproject2;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
        // Test database connection first
        com.mycompany.mavenproject2.utils.DatabaseUtil.testConnection();
        
        // Load the login page
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
        Scene scene = new Scene(root);
        
        // Add CSS if you have it
        scene.getStylesheets().add("/styles/main.css");
        
        stage.setTitle("Restaurant Login");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}