package com.example.projet_semestre6;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ClasseController {
    @FXML
    private Button btnretour;

    @FXML
    private SplitMenuButton niveau; // SplitMenuButton pour le niveau

    @FXML
    private TextField txtnomclasse;

    @FXML
    private TextField txtanneescolaire;

    @FXML
    private TableView<Classe> tblclasse; // Spécifié le type Classe

    @FXML
    private TableColumn<Classe, Integer> clid; // Spécifié le type Classe

    @FXML
    private TableColumn<Classe, String> clnomclasse; // Spécifié le type Classe

    @FXML
    private TableColumn<Classe, String> clanneescolaire; // Spécifié le type Classe

    @FXML
    private TableColumn<Classe, String> clniveau; // Spécifié le type Classe

    @FXML
    private Button btnajouter;

    @FXML
    private Button btnsupprimer;

    @FXML
    private Button btnmodifier; // Ajouté le bouton modifier si l'intention est de l'avoir

    private ObservableList<Classe> classes = FXCollections.observableArrayList();
    private DB db = DB.getInstance();

    @FXML
    public void initialize() {
        initTableView();
        loadClasses();
        setupTableViewSelectionListener();
        // Optionnel Définir un texte par défaut pour le SplitMenuButton
        // niveau.setText("Sélectionner un niveau");
    }

    private void initTableView() {
        // Associe les colonnes de la TableView aux propriétés de la classe Classe
        clid.setCellValueFactory(new PropertyValueFactory<>("id"));
        clnomclasse.setCellValueFactory(new PropertyValueFactory<>("nomClasse"));
        clanneescolaire.setCellValueFactory(new PropertyValueFactory<>("anneeScolaire"));
        clniveau.setCellValueFactory(new PropertyValueFactory<>("niveau"));

        tblclasse.setItems(classes);
    }

    private void loadClasses() {
        classes.clear(); // Vide la liste avant de charger
        Connection conn = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            conn = db.getConnection();
            String sql = "SELECT id, nom_classe, annee_scolaire, niveau FROM Classes";
            pstm = conn.prepareStatement(sql);
            rs = pstm.executeQuery();

            while (rs.next()) {
                classes.add(new Classe(
                        rs.getInt("id"),
                        rs.getString("nom_classe"),
                        rs.getString("annee_scolaire"),
                        rs.getString("niveau")
                ));
            }
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Erreur BD", "Impossible de charger les classes : " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(rs, pstm, conn);
        }
    }

    private void setupTableViewSelectionListener() {
        tblclasse.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                // Remplir les champs de saisie avec les données de la classe sélectionnée
                txtnomclasse.setText(newSelection.getNomClasse());
                txtanneescolaire.setText(newSelection.getAnneeScolaire());
                niveau.setText(newSelection.getNiveau()); // Met à jour le texte du SplitMenuButton
            } else {
                clearFields(); // Effacer les champs si aucune sélection
            }
        });
    }

    // Gestionnaire pour la sélection du niveau via le SplitMenuButton
    @FXML
    private void handleNiveauSelection(ActionEvent event) {
        // L'objet source de l'événement est le MenuItem cliqué
        MenuItem selectedMenuItem = (MenuItem) event.getSource();
        niveau.setText(selectedMenuItem.getText()); // Met à jour le texte du SplitMenuButton avec le niveau sélectionné
    }

    @FXML
    private void handleAjouter(ActionEvent event) {
        String nomClasse = txtnomclasse.getText();
        String anneeScolaire = txtanneescolaire.getText();
        String niveauSelected = niveau.getText(); // Récupère le texte du SplitMenuButton

        // Valider les champs
        if (nomClasse.isEmpty() || anneeScolaire.isEmpty() || niveauSelected.isEmpty() || niveauSelected.equals("NIVEAU")) {
            showAlert(AlertType.WARNING, "Champs Manquants", "Veuillez remplir tous les champs (Nom de la Classe, Année Scolaire, Niveau).");
            return;
        }

        Connection conn = null;
        PreparedStatement pstm = null;

        try {
            conn = db.getConnection();
            String sql = "INSERT INTO Classes (nom_classe, annee_scolaire, niveau) VALUES (?, ?, ?)";
            pstm = conn.prepareStatement(sql);
            pstm.setString(1, nomClasse);
            pstm.setString(2, anneeScolaire);
            pstm.setString(3, niveauSelected); // Stocke le niveau sélectionné

            pstm.executeUpdate();
            showAlert(AlertType.INFORMATION, "Succès", "Classe ajoutée avec succès !");
            loadClasses(); // Recharger les données
            clearFields(); // Vider les champs
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Erreur BD", "Erreur lors de l'ajout de la classe : " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(null, pstm, conn);
        }
    }

    @FXML
    private void handleModifier(ActionEvent event) {
        Classe selectedClasse = tblclasse.getSelectionModel().getSelectedItem();
        if (selectedClasse == null) {
            showAlert(AlertType.WARNING, "Attention", "Veuillez sélectionner une classe à modifier.");
            return;
        }

        String nomClasse = txtnomclasse.getText();
        String anneeScolaire = txtanneescolaire.getText();
        String niveauSelected = niveau.getText();

        if (nomClasse.isEmpty() || anneeScolaire.isEmpty() || niveauSelected.isEmpty() || niveauSelected.equals("NIVEAU")) {
            showAlert(AlertType.WARNING, "Champs Manquants", "Veuillez remplir tous les champs obligatoires.");
            return;
        }

        Connection conn = null;
        PreparedStatement pstm = null;

        try {
            conn = db.getConnection();
            String sql = "UPDATE Classes SET nom_classe = ?, annee_scolaire = ?, niveau = ? WHERE id = ?";
            pstm = conn.prepareStatement(sql);
            pstm.setString(1, nomClasse);
            pstm.setString(2, anneeScolaire);
            pstm.setString(3, niveauSelected);
            pstm.setInt(4, selectedClasse.getId());

            pstm.executeUpdate();
            showAlert(AlertType.INFORMATION, "Succès", "Classe modifiée avec succès !");
            loadClasses();
            clearFields();
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Erreur BD", "Erreur lors de la modification de la classe : " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(null, pstm, conn);
        }
    }

    @FXML
    private void handleSupprimer(ActionEvent event) {
        Classe selectedClasse = tblclasse.getSelectionModel().getSelectedItem();
        if (selectedClasse == null) {
            showAlert(AlertType.WARNING, "Attention", "Veuillez sélectionner une classe à supprimer.");
            return;
        }

        Alert confirmAlert = new Alert(AlertType.CONFIRMATION, "Voulez-vous vraiment supprimer la classe '" + selectedClasse.getNomClasse() + "' ?", ButtonType.YES, ButtonType.NO);
        confirmAlert.setTitle("Confirmation de Suppression");
        confirmAlert.setHeaderText(null);
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                Connection conn = null;
                PreparedStatement pstm = null;
                try {
                    conn = db.getConnection();
                    String sql = "DELETE FROM Classes WHERE id = ?";
                    pstm = conn.prepareStatement(sql);
                    pstm.setInt(1, selectedClasse.getId());
                    pstm.executeUpdate();
                    showAlert(AlertType.INFORMATION, "Succès", "Classe supprimée avec succès !");
                    loadClasses(); // Recharger les données après suppression
                    clearFields(); // Vider les champs
                } catch (SQLException e) {
                    showAlert(AlertType.ERROR, "Erreur BD", "Erreur lors de la suppression de la classe : " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    closeResources(null, pstm, conn);
                }
            }
        });
    }

    @FXML
    private void handleRetour(ActionEvent event) {
        try {
            // Revenir à l'interface principale (MainView.fxml)
            Outils.load(event, "PROJET_6 - Application Principale", "/com/example/projet_semestre6/MainView.fxml");
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Erreur de Navigation", "Impossible de revenir à la page principale : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void clearFields() {
        txtnomclasse.clear();
        txtanneescolaire.clear();
        niveau.setText("NIVEAU"); // Réinitialise le texte du SplitMenuButton
        tblclasse.getSelectionModel().clearSelection(); // Désélectionne la classe dans la TableView
    }

    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Méthode utilitaire pour fermer les ressources JDBC (ResultSet, PreparedStatement)
    private void closeResources(ResultSet rs, PreparedStatement pstm, Connection conn) {
        try {
            if (rs != null) rs.close();
            if (pstm != null) pstm.close();
            // Important Ne pas fermer la connexion ici si DB.getConnection() gère un singleton
            // carcela fermerait la connexion globale pour toute l'application.
            // La classe DB elle-même devrait gérer la fermeture de sa connexion
            // en fin d'application (par exemple, avec un ShutdownHook).
            // Pour des requêtes individuelles, ne fermez que rs et pstm.
            // Si db.getConnection() renvoie une nouvelle connexion à chaque appel,
            // alors conn.close() serait nécessaire ici.
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void RETOUR(ActionEvent e) throws IOException {
        Outils.load(e,"page etudiant","/com/example/projet_semestre6/AccueilController.fxml");
    }
}