package com.example.projet_semestre6;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Charge hello-view.fxml qui est maintenant votre page de connexion
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml")); // CHANGÉ ICI
        Scene scene = new Scene(fxmlLoader.load(), 770, 583); // Ajustez la taille si nécessaire
        stage.setTitle("Connexion à l'Application"); // Titre de la fenêtre de connexion
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}