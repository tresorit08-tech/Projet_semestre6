package com.example.projet_semestre6;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL; // Import nécessaire pour la méthode load(URL location)

public class Outils {

    // Cette instance est utilisée par les méthodes non statiques (loadPage, loadAndWaitPage)
    // et leurs wrappers statiques (load, loadandwait).
    // Elle est nécessaire car getClass().getResource() est une méthode d'instance.
    // Pour des raisons de cohérence et de simplicité, nous allons faire en sorte que toutes les méthodes de chargement
    // utilisent cette approche d'instance pour getResource, même si elles sont appelées via une méthode statique.
    // Cela évite les problèmes de ClassLoader.
    private static final Outils instance = new Outils(); // Singleton interne pour l'accès aux ressources

    // Constructeur privé pour le singleton interne
    private Outils() {}

    // Méthode loadAndWait existante (vide dans votre code fourni, mais conservée)
    public static void loadAndWait(ActionEvent event, String s) {
        // Implémentation si nécessaire, actuellement vide comme dans votre code
    }

    /**
     * Méthode d'instance pour charger une nouvelle page FXML.
     * Cache la fenêtre actuelle et ouvre une nouvelle fenêtre.
     * @param event L'ActionEvent qui a déclenché la navigation.
     * @param titre Le titre de la nouvelle fenêtre.
     * @param pageacharger Le chemin relatif vers le fichier FXML.
     * @throws IOException Si le fichier FXML ne peut pas être chargé.
     */
    private void loadPage(ActionEvent event, String titre, String pageacharger) throws IOException {
        // Cache la fenêtre actuelle
        ((Node)event.getSource()).getScene().getWindow().hide();

        // Charge le FXML
        Parent root = FXMLLoader.load(getClass().getResource(pageacharger));
        Scene scene = new Scene(root);
        Stage stage = new Stage(); // Crée une nouvelle fenêtre
        stage.setScene(scene);
        stage.setTitle(titre);
        stage.show();
    }

    // Méthode statique pour appeler la méthode d'instance loadPage
    public static Parent load(URL location) throws IOException {
        FXMLLoader loader = new FXMLLoader(location);
        return loader.load();
    }

    /**
     * Méthode statique pour charger une page FXML en cachant la fenêtre actuelle.
     * C'est une surcharge de 'load' pour correspondre à votre signature existante.
     * @param event L'ActionEvent qui a déclenché la navigation.
     * @param Titre Le titre de la nouvelle fenêtre.
     * @param url Le chemin relatif vers le fichier FXML.
     * @throws IOException Si le fichier FXML ne peut pas être chargé.
     */
    public static void load(ActionEvent event, String Titre, String url) throws IOException {
        instance.loadPage(event, Titre, url); // Utilise l'instance interne pour appeler loadPage
    }

    /**
     * Méthode d'instance pour charger une page FXML et attendre sans cacher la fenêtre actuelle.
     * (Basé sur votre implémentation fournie, qui ne cache pas la fenêtre).
     * @param event L'ActionEvent qui a déclenché la navigation.
     * @param titre Le titre de la nouvelle fenêtre.
     * @param pageacharger Le chemin relatif vers le fichier FXML.
     * @throws IOException Si le fichier FXML ne peut pas être chargé.
     */
    private void loadAndWaitPage(ActionEvent event, String titre, String pageacharger) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(pageacharger));
        Scene scene = new Scene(root);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle(titre);
        stage.show();
    }

    /**
     * Méthode statique pour appeler la méthode d'instance loadAndWaitPage.
     * @param event L'ActionEvent qui a déclenché la navigation.
     * @param Titre Le titre de la nouvelle fenêtre.
     * @param url Le chemin relatif vers le fichier FXML.
     * @throws IOException Si le fichier FXML ne peut pas être chargé.
     */
    public static void loadandwait(ActionEvent event, String Titre, String url) throws IOException {
        instance.loadAndWaitPage(event, Titre, url); // Utilise l'instance interne
    }

    /**
     * Charge la scène des notes de l'étudiant et passe l'ID de l'étudiant au contrôleur.
     * Cache la fenêtre actuelle et ouvre une nouvelle fenêtre.
     * @param event L'ActionEvent qui a déclenché la navigation.
     * @param title Le titre de la nouvelle fenêtre.
     * @param fxmlPath Le chemin vers le fichier FXML des notes de l'étudiant.
     * @param etudiantId L'ID de l'étudiant dont les notes doivent être affichées.
     * @throws IOException Si le fichier FXML ne peut pas être chargé.
     */
    public static void loadStudentNotesPage(ActionEvent event, String title, String fxmlPath, int etudiantId) throws IOException {
        // Cache la fenêtre actuelle
        ((Node)event.getSource()).getScene().getWindow().hide();

        FXMLLoader loader = new FXMLLoader(instance.getClass().getResource(fxmlPath)); // Utilise l'instance interne
        Parent root = loader.load();

        Object controller = loader.getController();
        System.out.println("DEBUG Outils: Type de contrôleur chargé pour " + fxmlPath + " est: " + (controller != null ? controller.getClass().getName() : "null"));

        if (controller instanceof StudentNotesController) {
            StudentNotesController studentNotesController = (StudentNotesController) controller;
            studentNotesController.showStudentNotes(etudiantId); // Appelle la méthode spécifique du contrôleur
            System.out.println("DEBUG Outils: Le contrôleur est un StudentNotesController. Tentative d'appel de showStudentNotes(" + etudiantId + ").");
        } else {
            System.err.println("ERREUR Outils: Le contrôleur pour " + fxmlPath + " n'est pas un StudentNotesController.");
        }

        Scene scene = new Scene(root);
        Stage stage = new Stage(); // Crée une nouvelle fenêtre
        stage.setTitle(title);
        stage.setScene(scene);
        stage.show();
        System.out.println("DEBUG Outils: Page chargée: " + fxmlPath + " avec titre: " + title + " pour etudiantId: " + etudiantId);
    }

    /**
     * Charge la scène du bulletin de notes et passe l'ID de l'étudiant au contrôleur.
     * Cache la fenêtre actuelle et ouvre une nouvelle fenêtre.
     * @param event L'ActionEvent qui a déclenché la navigation.
     * @param title Le titre de la nouvelle fenêtre.
     * @param fxmlPath Le chemin vers le fichier FXML du bulletin.
     * @param etudiantId L'ID de l'étudiant dont le bulletin doit être affiché.
     * @throws IOException Si le fichier FXML ne peut pas être chargé.
     */
    public static void loadBulletinPage(ActionEvent event, String title, String fxmlPath, int etudiantId) throws IOException {
        // Cache la fenêtre actuelle
        ((Node)event.getSource()).getScene().getWindow().hide();

        FXMLLoader loader = new FXMLLoader(instance.getClass().getResource(fxmlPath)); // Utilise l'instance interne
        Parent root = loader.load();

        Object controller = loader.getController();
        System.out.println("DEBUG Outils: Type de contrôleur chargé pour " + fxmlPath + " est: " + (controller != null ? controller.getClass().getName() : "null"));

        // Vérifie si le contrôleur est une instance de BulletinController
        if (controller instanceof BulletinController) {
            BulletinController bulletinController = (BulletinController) controller;
            bulletinController.showBulletin(etudiantId); // Appelle la méthode spécifique du contrôleur
            System.out.println("DEBUG Outils: Le contrôleur est un BulletinController. Tentative d'appel de showBulletin(" + etudiantId + ").");
        } else {
            System.err.println("ERREUR Outils: Le contrôleur pour " + fxmlPath + " n'est pas un BulletinController.");
        }

        Scene scene = new Scene(root);
        Stage stage = new Stage(); // Crée une nouvelle fenêtre
        stage.setTitle(title);
        stage.setScene(scene);
        stage.show();
        System.out.println("DEBUG Outils: Page chargée: " + fxmlPath + " avec titre: " + title + " pour etudiantId: " + etudiantId);
    }
}
