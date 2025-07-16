package com.example.projet_semestre6;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MatiereController {
    @FXML
    private Button BTNRETOUR;

    @FXML
    private TextField TXTNOMMATIERE;

    @FXML
    private TextField TXTCOEFFICIENT;

    @FXML
    private Button BTNAJOUTER;

    @FXML
    private Button BTNMODIFIER;

    @FXML
    private Button BTNSUPPRIMER;

    @FXML
    private TextField TXTRECHERCHER;

    @FXML
    private TableView<Matiere> TBLMATIERE; // Spécifié le type Matiere

    @FXML
    private TableColumn<Matiere, Integer> CLIDMATIERE; // Spécifié le type Matiere

    @FXML
    private TableColumn<Matiere, String> CLNOMMATIERE; // Spécifié le type Matiere

    @FXML
    private TableColumn<Matiere, Double> CLCOEFFICIENT; // Spécifié le type Matiere

    private ObservableList<Matiere> matieres = FXCollections.observableArrayList();
    private DB db = DB.getInstance();

    @FXML
    public void initialize() {
        initTableView();
        loadMatieres();
        setupTableViewSelectionListener();
    }

    private void initTableView() {
        // Associe les colonnes de la TableView aux propriétés de la classe Matiere
        CLIDMATIERE.setCellValueFactory(new PropertyValueFactory<>("id"));
        CLNOMMATIERE.setCellValueFactory(new PropertyValueFactory<>("nomMatiere"));
        CLCOEFFICIENT.setCellValueFactory(new PropertyValueFactory<>("coefficient"));

        TBLMATIERE.setItems(matieres);
    }

    private void loadMatieres() {
        matieres.clear(); // Vide la liste avant de charger
        Connection conn = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            conn = db.getConnection();
            String sql = "SELECT id, nom_matiere, coefficient FROM Matieres";
            pstm = conn.prepareStatement(sql);
            rs = pstm.executeQuery();

            while (rs.next()) {
                matieres.add(new Matiere(
                        rs.getInt("id"),
                        rs.getString("nom_matiere"),
                        rs.getDouble("coefficient")
                ));
            }
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Erreur BD", "Impossible de charger les matières : " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(rs, pstm, conn);
        }
    }

    private void setupTableViewSelectionListener() {
        TBLMATIERE.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                // Remplir les champs de saisie avec les données de la matière sélectionnée
                TXTNOMMATIERE.setText(newSelection.getNomMatiere());
                TXTCOEFFICIENT.setText(String.valueOf(newSelection.getCoefficient())); // Convertir double en String
            } else {
                clearFields(); // Effacer les champs si aucune sélection
            }
        });
    }

    @FXML
    private void handleAjouter(ActionEvent event) {
        // Vérifier si une matière est sélectionnée pour éviter l'ajout si une modification est en cours
        if (TBLMATIERE.getSelectionModel().getSelectedItem() != null) {
            showAlert(AlertType.INFORMATION, "Action", "Veuillez désélectionner la matière pour ajouter une nouvelle.");
            return;
        }
        saveMatiere(null); // Appelle saveMatiere avec null pour indiquer un ajout
    }

    @FXML
    private void handleModifier(ActionEvent event) {
        Matiere selectedMatiere = TBLMATIERE.getSelectionModel().getSelectedItem();
        if (selectedMatiere != null) {
            saveMatiere(selectedMatiere); // Appelle saveMatiere pour une modification
        } else {
            showAlert(AlertType.WARNING, "Attention", "Veuillez sélectionner une matière à modifier.");
        }
    }

    private void saveMatiere(Matiere matiereToModify) {
        String nomMatiere = TXTNOMMATIERE.getText();
        String coefficientText = TXTCOEFFICIENT.getText();

        // Validation des champs
        if (nomMatiere.isEmpty() || coefficientText.isEmpty()) {
            showAlert(AlertType.WARNING, "Champs Manquants", "Veuillez remplir tous les champs (Nom de la matière, Coefficient).");
            return;
        }

        double coefficient;
        try {
            coefficient = Double.parseDouble(coefficientText);
        } catch (NumberFormatException e) {
            showAlert(AlertType.ERROR, "Saisie Invalide", "Le coefficient doit être un nombre valide.");
            return;
        }

        Connection conn = null;
        PreparedStatement pstm = null;

        try {
            conn = db.getConnection();
            String sql;
            if (matiereToModify == null) { // Mode Ajout
                sql = "INSERT INTO Matieres (nom_matiere, coefficient) VALUES (?, ?)";
                pstm = conn.prepareStatement(sql);
                // Définir les paramètres pour l'insertion
                pstm.setString(1, nomMatiere);
                pstm.setDouble(2, coefficient);
                showAlert(AlertType.INFORMATION, "Succès", "Matière ajoutée avec succès !");

            } else { // Mode Modification
                sql = "UPDATE Matieres SET nom_matiere = ?, coefficient = ? WHERE id = ?";
                pstm = conn.prepareStatement(sql);
                // Définir les paramètres pour la mise à jour
                pstm.setString(1, nomMatiere);
                pstm.setDouble(2, coefficient);
                pstm.setInt(3, matiereToModify.getId());
                showAlert(AlertType.INFORMATION, "Succès", "Matière modifiée avec succès !");
            }

            pstm.executeUpdate();
            loadMatieres(); // Recharger les données dans la TableView
            clearFields(); // Vider les champs après l'opération

        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Erreur BD", "Erreur lors de l'enregistrement de la matière : " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(null, pstm, conn);
        }
    }

    @FXML
    private void handleSupprimer(ActionEvent event) {
        Matiere selectedMatiere = TBLMATIERE.getSelectionModel().getSelectedItem();
        if (selectedMatiere == null) {
            showAlert(AlertType.WARNING, "Attention", "Veuillez sélectionner une matière à supprimer.");
            return;
        }

        Alert confirmAlert = new Alert(AlertType.CONFIRMATION, "Voulez-vous vraiment supprimer la matière '" + selectedMatiere.getNomMatiere() + "' ?", ButtonType.YES, ButtonType.NO);
        confirmAlert.setTitle("Confirmation de Suppression");
        confirmAlert.setHeaderText(null);
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                Connection conn = null;
                PreparedStatement pstm = null;
                try {
                    conn = db.getConnection();
                    String sql = "DELETE FROM Matieres WHERE id = ?";
                    pstm = conn.prepareStatement(sql);
                    pstm.setInt(1, selectedMatiere.getId());
                    pstm.executeUpdate();
                    showAlert(AlertType.INFORMATION, "Succès", "Matière supprimée avec succès !");
                    loadMatieres(); // Recharger les données après suppression
                    clearFields(); // Vider les champs
                } catch (SQLException e) {
                    showAlert(AlertType.ERROR, "Erreur BD", "Erreur lors de la suppression de la matière : " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    closeResources(null, pstm, conn);
                }
            }
        });
    }

    @FXML
    private void handleRechercher() { // Déclenché par onKeyReleased sur le TextField
        String searchText = TXTRECHERCHER.getText().toLowerCase();
        ObservableList<Matiere> filteredList = FXCollections.observableArrayList();

        if (searchText.isEmpty()) {
            TBLMATIERE.setItems(matieres); // Afficher toute la liste si la recherche est vide
            return;
        }

        for (Matiere matiere : matieres) {
            if (matiere.getNomMatiere().toLowerCase().contains(searchText) ||
                    String.valueOf(matiere.getCoefficient()).toLowerCase().contains(searchText)) { // Rechercher aussi par coefficient
                filteredList.add(matiere);
            }
        }
        TBLMATIERE.setItems(filteredList);
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
        TXTNOMMATIERE.clear();
        TXTCOEFFICIENT.clear();
        TBLMATIERE.getSelectionModel().clearSelection(); // Désélectionne la matière dans la TableView
    }

    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Méthode utilitaire pour fermer les ressources JDBC
    private void closeResources(ResultSet rs, PreparedStatement pstm, Connection conn) {
        try {
            if (rs != null) rs.close();
            if (pstm != null) pstm.close();
            // Comme pour les autres contrôleurs, on ne ferme pas la connexion DB ici si elle est gérée en singleton.
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void RETOUR(ActionEvent e) throws IOException {
        Outils.load(e,"page etudiant","/com/example/projet_semestre6/AccueilController.fxml");
    }
}