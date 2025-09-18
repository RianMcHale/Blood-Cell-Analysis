package com.example.bloodcellanalysis;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bloodcellanalysis/MainView.fxml"));
        Scene scene = new Scene(loader.load(), 650, 600);
        primaryStage.setTitle("Blood Cell Analyser");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
