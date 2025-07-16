package com.example.projet_semestre6;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.application.Platform; // Import additionnel pour Platform.runLater

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Contrôleur pour le tableau de bord de l'étudiant.
 * Cette classe gère l'affichage des informations de bienvenue pour l'étudiant connecté
 * et la navigation vers ses notes et son bulletin. Elle inclut également la fonctionnalité
 * de génération de bulletin au format PDF.
 */
public class StudentDashboardController {

    @FXML
    private Label lblWelcomeMessage; // Label pour afficher le message de bienvenue

    private DB db = DB.getInstance(); // Instance de la classe de connexion à la base de données

    /**
     * Méthode d'initialisation du contrôleur. Appelé automatiquement par JavaFX.
     * Récupère les informations de l'étudiant connecté via SessionManager
     * et met à jour le message de bienvenue.
     */
    @FXML
    public void initialize() {
        System.out.println("DEBUG StudentDashboardController: initialize() appelé.");
        // Vérifie si un étudiant est bien connecté via SessionManager
        if (SessionManager.getInstance().isEtudiant()) {
            int etudiantId = SessionManager.getInstance().getCurrentEtudiantId();
            System.out.println("DEBUG StudentDashboardController: Etudiant connecté avec ID: " + etudiantId);
            loadStudentInfo(etudiantId); // Charge les informations de l'étudiant pour le message de bienvenue
        } else {
            // Si aucun étudiant n'est connecté (cas inattendu), affiche un message générique
            lblWelcomeMessage.setText("Bienvenue ! (Utilisateur non étudiant)");
            showAlert(AlertType.WARNING, "Accès non autorisé", "Veuillez vous connecter en tant qu'étudiant.");
            System.err.println("ERREUR: StudentDashboardController initialisé sans utilisateur étudiant connecté.");
        }
    }

    /**
     * Charge le nom et prénom de l'étudiant depuis la base de données
     * pour personnaliser le message de bienvenue.
     * @param etudiantId L'ID de l'étudiant connecté.
     */
    private void loadStudentInfo(int etudiantId) {
        Connection conn = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            conn = db.getConnection();
            if (conn == null) {
                System.err.println("ERREUR: Connexion à la BD nulle dans loadStudentInfo.");
                lblWelcomeMessage.setText("Bienvenue ! (Erreur de connexion)");
                return;
            }

            String sql = "SELECT nom, prenom FROM Etudiants WHERE id = ?";
            pstm = conn.prepareStatement(sql);
            pstm.setInt(1, etudiantId);
            rs = pstm.executeQuery();

            if (rs.next()) {
                String nom = rs.getString("nom");
                String prenom = rs.getString("prenom");
                lblWelcomeMessage.setText("Bienvenue, " + prenom + " " + nom + " !");
                System.out.println("DEBUG StudentDashboardController: Message de bienvenue mis à jour pour " + prenom + " " + nom);
            } else {
                lblWelcomeMessage.setText("Bienvenue ! (Étudiant introuvable)");
                System.err.println("ERREUR: Aucun étudiant trouvé avec l'ID: " + etudiantId);
            }

        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Erreur BD", "Impossible de charger les informations de l'étudiant : " + e.getMessage());
            e.printStackTrace();
            System.err.println("SQL Exception dans loadStudentInfo: " + e.getMessage());
        } finally {
            closeResources(rs, pstm, conn);
        }
    }

    /**
     * Gère l'action du bouton "Voir mes notes".
     * Redirige l'étudiant vers la page affichant ses notes détaillées.
     * @param event L'événement d'action.
     */
    @FXML
    private void handleViewNotes(ActionEvent event) {
        System.out.println("DEBUG StudentDashboardController: handleViewNotes() appelé.");
        try {
            Outils.loadStudentNotesPage(event, "Mes Notes", "/com/example/projet_semestre6/StudentNotes.fxml", SessionManager.getInstance().getCurrentEtudiantId());
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Erreur de Navigation", "Impossible d'ouvrir la page des notes : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Gère l'action du bouton "Voir mon bulletin".
     * Redirige l'étudiant vers la page de son bulletin de notes.
     * @param event L'événement d'action.
     */
    @FXML
    private void handleViewBulletin(ActionEvent event) {
        System.out.println("DEBUG StudentDashboardController: handleViewBulletin() appelé.");
        try {
            Outils.loadBulletinPage(event, "Mon Bulletin de Notes", "/com/example/projet_semestre6/Bulletin.fxml", SessionManager.getInstance().getCurrentEtudiantId());
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Erreur de Navigation", "Impossible d'ouvrir le bulletin : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Gère l'action du bouton "Tirer le bulletin en PDF".
     * Génère le bulletin de l'étudiant connecté au format PDF.
     * @param event L'événement d'action.
     */
    @FXML
    private void handleGeneratePdfBulletin(ActionEvent event) {
        System.out.println("DEBUG StudentDashboardController: handleGeneratePdfBulletin() appelé.");
        int etudiantId = SessionManager.getInstance().getCurrentEtudiantId();

        if (etudiantId == -1) {
            showAlert(AlertType.WARNING, "Erreur de Session", "Aucun étudiant n'est connecté pour générer le bulletin.");
            return;
        }

        // On crée un BulletinController temporaire pour réutiliser la logique de génération de données
        // C'est une approche simple pour éviter de dupliquer la logique de requête BD.
        // Dans une application plus grande, la logique de récupération de données serait dans une couche de service séparée.
        BulletinController bulletinGenerator = new BulletinController();

        // Exécuter la génération des données du bulletin dans un thread séparé
        new Thread(() -> {
            BulletinEtudiant bulletinData = bulletinGenerator.generateBulletinData(etudiantId); // Utilise la méthode privée de BulletinController

            if (bulletinData != null) {
                Platform.runLater(() -> {
                    // Ouvre une boîte de dialogue pour choisir l'emplacement et le nom du fichier PDF
                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setTitle("Enregistrer le Bulletin en PDF");
                    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
                    fileChooser.setInitialFileName("Bulletin_" + bulletinData.getNomEtudiant().replace(" ", "_") + ".pdf");

                    // Obtenir la Stage actuelle pour le FileChooser
                    // Le cast vers Button est sûr car l'événement provient d'un bouton.
                    Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
                    File file = fileChooser.showSaveDialog(stage);

                    if (file != null) {
                        try {
                            // Appelle la classe utilitaire pour générer le PDF
                            PdfGenerator.generateBulletinPdf(bulletinData, file.getAbsolutePath());
                            showAlert(AlertType.INFORMATION, "Succès", "Le bulletin PDF a été généré avec succès à :\n" + file.getAbsolutePath());
                            System.out.println("DEBUG StudentDashboardController: Bulletin PDF généré à " + file.getAbsolutePath());
                        } catch (Exception e) {
                            showAlert(AlertType.ERROR, "Erreur de Génération PDF", "Une erreur est survenue lors de la génération du PDF : " + e.getMessage());
                            e.printStackTrace();
                            System.err.println("ERREUR StudentDashboardController: Erreur lors de la génération du PDF: " + e.getMessage());
                        }
                    } else {
                        System.out.println("DEBUG StudentDashboardController: Génération PDF annulée par l'utilisateur.");
                    }
                });
            } else {
                Platform.runLater(() -> showAlert(AlertType.ERROR, "Erreur de Données", "Impossible de récupérer les données du bulletin pour la génération PDF."));
            }
        }).start();
    }


    /**
     * Gère l'action du bouton "Déconnexion".
     * Réinitialise la session et redirige vers la page de connexion.
     * @param event L'événement d'action.
     */
    @FXML
    private void handleLogout(ActionEvent event) {
        System.out.println("DEBUG StudentDashboardController: handleLogout() appelé.");
        SessionManager.getInstance().logout(); // Déconnecte l'utilisateur de la session
        try {
            Outils.load(event, "Connexion à l'Application", "/com/example/projet_semestre6/hello-view.fxml");
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Erreur de Déconnexion", "Impossible de revenir à la page de connexion : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Méthode utilitaire pour afficher des boîtes de dialogue d'alerte.
     * @param type Le type d'alerte (INFORMATION, WARNING, ERROR, etc.).
     * @param title Le titre de la boîte de dialogue.
     * @param content Le contenu (message) de l'alerte.
     */
    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Méthode utilitaire pour fermer les ressources JDBC.
     * @param rs Le ResultSet à fermer (peut être null).
     * @param pstm Le PreparedStatement à fermer (peut être null).
     * @param conn La connexion à fermer (peut être null).
     */
    private void closeResources(ResultSet rs, PreparedStatement pstm, Connection conn) {
        try {
            if (rs != null) rs.close();
            if (pstm != null) pstm.close();
            // La connexion est gérée par le Singleton DB, donc ne la fermons pas ici
            // si elle est destinée à rester ouverte pour d'autres opérations.
            // Si chaque appel à db.getConnection() ouvre une nouvelle connexion, alors décommenter ci-dessous.
            // if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}