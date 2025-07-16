package com.example.projet_semestre6;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert.AlertType;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

// L'import de BCrypt est commenté, comme dans votre code original.
// Il est fortement recommandé d'utiliser BCrypt pour la sécurité des mots de passe.
// import org.mindrot.jbcrypt.BCrypt;

public class LoginController { // Renommé de HelloController à LoginController
    DB db = DB.getInstance(); // Votre instance de classe DB

    @FXML
    private ImageView userIcon;
    @FXML
    private TextField usernameField; // Utilisé pour le nom d'utilisateur (Admin) ou le prénom (Étudiant)
    @FXML
    private PasswordField passwordField; // Utilisé pour le mot de passe (Admin) ou l'adresse (Étudiant)
    @FXML
    private Button loginButton;
    @FXML
    private Hyperlink forgotPasswordLink;

    private int generated2FACode; // Code 2FA généré (pour la logique 2FA si réactivée)

    @FXML
    public void initialize() {
        // Chargez l'icône utilisateur. Assurez-vous que le chemin est correct.
        // Si 'user-icon.png' est directement dans 'src/main/resources/com/example/projet_semestre6/'
        Image image = new Image(getClass().getResourceAsStream("/com/example/projet_semestre6/user-icon.png"));
        userIcon.setImage(image);
    }

    @FXML
    protected void handleLogin(ActionEvent event) {
        String inputIdentifier = usernameField.getText(); // Peut être nom_utilisateur ou prénom
        String inputSecret = passwordField.getText();     // Peut être mot de passe ou adresse

        if (inputIdentifier.isEmpty() || inputSecret.isEmpty()) {
            showAlert(AlertType.WARNING, "Attention", "Veuillez remplir tous les champs.");
            return;
        }

        Connection conn = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            conn = db.getConnection();
            if (conn == null) {
                showAlert(AlertType.ERROR, "Erreur de connexion", "Impossible de se connecter à la base de données.");
                return;
            }

            // --- TENTATIVE DE CONNEXION EN TANT QU'ADMINISTRATEUR ---
            // On cherche d'abord dans la table 'Utilisateurs' pour un rôle 'admin'
            String adminSql = "SELECT mot_de_passe_hash, role FROM Utilisateurs WHERE nom_utilisateur = ? AND role = 'admin'";
            pstm = conn.prepareStatement(adminSql);
            pstm.setString(1, inputIdentifier);
            rs = pstm.executeQuery();

            if (rs.next()) {
                String mot_de_passe_stocke = rs.getString("mot_de_passe_hash");
                String role = rs.getString("role");

                // Comparaison directe du mot de passe (sans BCrypt, comme dans votre code original)
                if (inputSecret.equals(mot_de_passe_stocke)) {
                    System.out.println("DEBUG: Connexion administrateur réussie pour " + inputIdentifier);
                    SessionManager.getInstance().setLoggedInUser(role, -1); // -1 car pas d'ID étudiant pour l'admin

                    showAlert(AlertType.INFORMATION, "Connexion Réussie", "Bienvenue, Administrateur " + inputIdentifier + " !");
                    // Redirection vers la page d'accueil de l'administrateur
                    Outils.load(event, "PROJET_6 - Page d'Accueil Admin", "/com/example/projet_semestre6/AccueilController.fxml");
                    return; // Sort de la méthode après une connexion réussie
                }
            }
            closeResources(rs, pstm, null); // Ferme les ressources pour la première requête
            rs = null;
            pstm = null;

            // --- TENTATIVE DE CONNEXION EN TANT QU'ÉTUDIANT ---
            // Si ce n'est pas un admin, on cherche dans la table 'Etudiants'
            String etudiantSql = "SELECT id, nom, prenom, adresse FROM Etudiants WHERE prenom = ? AND email = ?";
            pstm = conn.prepareStatement(etudiantSql);
            pstm.setString(1, inputIdentifier); // Le prénom est dans usernameField
            pstm.setString(2, inputSecret);     // L'adresse est dans passwordField
            rs = pstm.executeQuery();

            if (rs.next()) {
                int etudiantId = rs.getInt("id");
                String nomEtudiant = rs.getString("nom");
                String prenomEtudiant = rs.getString("prenom");

                System.out.println("DEBUG: Connexion étudiant réussie pour " + prenomEtudiant + " " + nomEtudiant + " (ID: " + etudiantId + ")");
                SessionManager.getInstance().setLoggedInUser("etudiant", etudiantId); // Stocke le rôle et l'ID étudiant

                showAlert(AlertType.INFORMATION, "Connexion Réussie", "Bienvenue, Étudiant " + prenomEtudiant + " " + nomEtudiant + " !");
                // Redirection vers la nouvelle page du tableau de bord étudiant
                Outils.load(event, "PROJET_6 - Tableau de Bord Étudiant", "/com/example/projet_semestre6/StudentDashboard.fxml");
                return; // Sort de la méthode après une connexion réussie
            }

            // --- AUCUNE CONNEXION RÉUSSIE ---
            showAlert(AlertType.ERROR, "Échec de connexion", "Identifiant, prénom ou mot de passe/adresse incorrect(e).");

        } catch (SQLException ex) {
            showAlert(AlertType.ERROR, "Erreur de base de données", "Une erreur est survenue lors de la connexion : " + ex.getMessage());
            ex.printStackTrace();
        } catch (IOException ex) {
            showAlert(AlertType.ERROR, "Erreur de chargement", "Impossible de charger la page suivante : " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            // Assurez-vous de fermer les ressources JDBC
            closeResources(rs, pstm, conn);
        }
    }

    @FXML
    protected void handleForgotPassword() {
        showAlert(AlertType.INFORMATION, "Fonctionnalité", "Cette fonctionnalité n'est pas encore implémentée.");
    }

    // Les méthodes 2FA sont conservées mais ne sont pas utilisées dans la logique de connexion actuelle
    // pour simplifier comme demandé.
    private int generate2FACode() {
        Random random = new Random();
        return 100000 + random.nextInt(900000); // Code entre 100000 et 999999
    }

    private boolean verify2FACode() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Vérification 2FA Requise");
        dialog.setHeaderText("Authentification à Deux Facteurs");
        dialog.setContentText("Veuillez saisir le code à 6 chiffres (voir console pour le code de test) :");

        return dialog.showAndWait()
                .map(input -> {
                    try {
                        return Integer.parseInt(input) == generated2FACode;
                    } catch (NumberFormatException e) {
                        showAlert(AlertType.ERROR, "Saisie Invalide", "Veuillez saisir un nombre valide pour le code 2FA.");
                        return false;
                    }
                })
                .orElse(false);
    }

    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void closeResources(ResultSet rs, PreparedStatement pstm, Connection conn) {
        try {
            if (rs != null) rs.close();
            if (pstm != null) pstm.close();
            if (conn != null) conn.close(); // Ferme la connexion ici car elle est ouverte par handleLogin
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}