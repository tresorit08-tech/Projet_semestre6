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

public class EnseignantController {
    @FXML
    private Button BTNRETOUR;

    @FXML
    private Button BTNAJOUTER;

    @FXML
    private Button BTNMODIFIER;

    @FXML
    private Button BTNSUPPRIMER;

    @FXML
    private TextField TXTRECHERCHER;

    @FXML
    private SplitMenuButton SPECIALITE; // SplitMenuButton pour la spécialité

    @FXML
    private TextField TXTNOM;

    @FXML
    private TextField TXTPRENOM;

    @FXML
    private TextField TXTTELEPHONE;

    @FXML
    private TextField TXTEMAIL;

    @FXML
    private TableView<Enseignant> TBLENSEIGNANT; // Spécifié le type Enseignant

    @FXML
    private TableColumn<Enseignant, Integer> CLID; // Spécifié le type Enseignant

    @FXML
    private TableColumn<Enseignant, String> CLNOM; // Spécifié le type Enseignant

    @FXML
    private TableColumn<Enseignant, String> CLPRENOM; // Spécifié le type Enseignant

    @FXML
    private TableColumn<Enseignant, String> CLSPECIALITE; // Spécifié le type Enseignant

    @FXML
    private TableColumn<Enseignant, String> CLTELEPHONE; // Spécifié le type Enseignant

    @FXML
    private TableColumn<Enseignant, String> CLEMAIL; // Spécifié le type Enseignant

    private ObservableList<Enseignant> enseignants = FXCollections.observableArrayList();
    private DB db = DB.getInstance();

    @FXML
    public void initialize() {
        initTableView();
        loadEnseignants();
        setupTableViewSelectionListener();
        // Optionnel:Définir un texte par défaut pour le SplitMenuButton
        // SPECIALITE.setText("Sélectionner une spécialité");
    }

    private void initTableView() {
        // Associe les colonnes de la TableView aux propriétés de la classe Enseignant
        CLID.setCellValueFactory(new PropertyValueFactory<>("id"));
        CLNOM.setCellValueFactory(new PropertyValueFactory<>("nom"));
        CLPRENOM.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        CLSPECIALITE.setCellValueFactory(new PropertyValueFactory<>("specialite"));
        CLTELEPHONE.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        CLEMAIL.setCellValueFactory(new PropertyValueFactory<>("email"));

        TBLENSEIGNANT.setItems(enseignants);
    }

    private void loadEnseignants() {
        enseignants.clear(); // Vide la liste avant de charger
        Connection conn = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            conn = db.getConnection();
            String sql = "SELECT id, nom, prenom, specialite, telephone, email FROM Enseignants";
            pstm = conn.prepareStatement(sql);
            rs = pstm.executeQuery();

            while (rs.next()) {
                enseignants.add(new Enseignant(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("specialite"),
                        rs.getString("telephone"),
                        rs.getString("email")
                ));
            }
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Erreur BD", "Impossible de charger les enseignants : " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(rs, pstm, conn);
        }
    }

    private void setupTableViewSelectionListener() {
        TBLENSEIGNANT.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                // Remplir les champs de saisie avec les données de l'enseignant sélectionné
                TXTNOM.setText(newSelection.getNom());
                TXTPRENOM.setText(newSelection.getPrenom());
                SPECIALITE.setText(newSelection.getSpecialite()); // Met à jour le texte du SplitMenuButton
                TXTTELEPHONE.setText(newSelection.getTelephone());
                TXTEMAIL.setText(newSelection.getEmail());
            } else {
                clearFields(); // Effacer les champs si aucune sélection
            }
        });
    }

    // Gestionnaire pour la sélection de la spécialité via le SplitMenuButton
    @FXML
    private void handleSpecialiteSelection(ActionEvent event) {
        // L'objet source de l'événement est le MenuItem cliqué
        MenuItem selectedMenuItem = (MenuItem) event.getSource();
        SPECIALITE.setText(selectedMenuItem.getText()); // Met à jour le texte du SplitMenuButton
    }

    @FXML
    private void handleAjouter(ActionEvent event) {
        // Vérifier si un enseignant est sélectionné pour éviter l'ajout si une modification est en cours
        if (TBLENSEIGNANT.getSelectionModel().getSelectedItem() != null) {
            showAlert(AlertType.INFORMATION, "Action", "Veuillez désélectionner l'enseignant pour ajouter un nouveau.");
            return;
        }
        saveEnseignant(null); // Appelle saveEnseignant avec null pour indiquer un ajout
    }

    @FXML
    private void handleModifier(ActionEvent event) {
        Enseignant selectedEnseignant = TBLENSEIGNANT.getSelectionModel().getSelectedItem();
        if (selectedEnseignant != null) {
            saveEnseignant(selectedEnseignant); // Appelle saveEnseignant pour une modification
        } else {
            showAlert(AlertType.WARNING, "Attention", "Veuillez sélectionner un enseignant à modifier.");
        }
    }

    private void saveEnseignant(Enseignant enseignantToModify) {
        String nom = TXTNOM.getText();
        String prenom = TXTPRENOM.getText();
        String specialite = SPECIALITE.getText(); // Récupère le texte du SplitMenuButton
        String telephone = TXTTELEPHONE.getText();
        String email = TXTEMAIL.getText();

        // Validation des champs
        if (nom.isEmpty() || prenom.isEmpty() || specialite.isEmpty() || specialite.equals("SPECIALITE")) {
            showAlert(AlertType.WARNING, "Champs Manquants", "Veuillez remplir tous les champs obligatoires (Nom, Prénom, Spécialité).");
            return;
        }

        Connection conn = null;
        PreparedStatement pstm = null;

        try {
            conn = db.getConnection();
            String sql;
            if (enseignantToModify == null) { // Mode Ajout
                sql = "INSERT INTO Enseignants (nom, prenom, specialite, telephone, email) VALUES (?, ?, ?, ?, ?)";
                pstm = conn.prepareStatement(sql);
                // Définir les paramètres pour l'insertion
                pstm.setString(1, nom);
                pstm.setString(2, prenom);
                pstm.setString(3, specialite);
                pstm.setString(4, telephone);
                pstm.setString(5, email);
                showAlert(AlertType.INFORMATION, "Succès", "Enseignant ajouté avec succès !");

            } else { // Mode Modification
                sql = "UPDATE Enseignants SET nom = ?, prenom = ?, specialite = ?, telephone = ?, email = ? WHERE id = ?";
                pstm = conn.prepareStatement(sql);
                // Définir les paramètres pour la mise à jour
                pstm.setString(1, nom);
                pstm.setString(2, prenom);
                pstm.setString(3, specialite);
                pstm.setString(4, telephone);
                pstm.setString(5, email);
                pstm.setInt(6, enseignantToModify.getId());
                showAlert(AlertType.INFORMATION, "Succès", "Enseignant modifié avec succès !");
            }

            pstm.executeUpdate();
            loadEnseignants(); // Recharger les données dans la TableView
            clearFields(); // Vider les champs après l'opération

        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Erreur BD", "Erreur lors de l'enregistrement de l'enseignant : " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(null, pstm, conn);
        }
    }

    @FXML
    private void handleSupprimer(ActionEvent event) {
        Enseignant selectedEnseignant = TBLENSEIGNANT.getSelectionModel().getSelectedItem();
        if (selectedEnseignant == null) {
            showAlert(AlertType.WARNING, "Attention", "Veuillez sélectionner un enseignant à supprimer.");
            return;
        }

        Alert confirmAlert = new Alert(AlertType.CONFIRMATION, "Voulez-vous vraiment supprimer l'enseignant " + selectedEnseignant.getNom() + " " + selectedEnseignant.getPrenom() + " ?", ButtonType.YES, ButtonType.NO);
        confirmAlert.setTitle("Confirmation de Suppression");
        confirmAlert.setHeaderText(null);
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                Connection conn = null;
                PreparedStatement pstm = null;
                try {
                    conn = db.getConnection();
                    String sql = "DELETE FROM Enseignants WHERE id = ?";
                    pstm = conn.prepareStatement(sql);
                    pstm.setInt(1, selectedEnseignant.getId());
                    pstm.executeUpdate();
                    showAlert(AlertType.INFORMATION, "Succès", "Enseignant supprimé avec succès !");
                    loadEnseignants(); // Recharger les données après suppression
                    clearFields(); // Vider les champs
                } catch (SQLException e) {
                    showAlert(AlertType.ERROR, "Erreur BD", "Erreur lors de la suppression de l'enseignant : " + e.getMessage());
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
        ObservableList<Enseignant> filteredList = FXCollections.observableArrayList();

        if (searchText.isEmpty()) {
            TBLENSEIGNANT.setItems(enseignants); // Afficher toute la liste si la recherche est vide
            return;
        }

        for (Enseignant enseignant : enseignants) {
            if (enseignant.getNom().toLowerCase().contains(searchText) ||
                    enseignant.getPrenom().toLowerCase().contains(searchText) ||
                    (enseignant.getSpecialite() != null && enseignant.getSpecialite().toLowerCase().contains(searchText)) ||
                    (enseignant.getEmail() != null && enseignant.getEmail().toLowerCase().contains(searchText)) ||
                    (enseignant.getTelephone() != null && enseignant.getTelephone().toLowerCase().contains(searchText))) {
                filteredList.add(enseignant);
            }
        }
        TBLENSEIGNANT.setItems(filteredList);
    }



    private void clearFields() {
        TXTNOM.clear();
        TXTPRENOM.clear();
        SPECIALITE.setText("SPECIALITE"); // Réinitialise le texte du SplitMenuButton
        TXTTELEPHONE.clear();
        TXTEMAIL.clear();
        TBLENSEIGNANT.getSelectionModel().clearSelection(); // Désélectionne l'enseignant dans la TableView
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
            // Comme pour EtudiantController, on ne ferme pas la connexion DB ici si elle est gérée en singleton.
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void RETOUR(ActionEvent e) throws IOException {
        Outils.load(e,"page etudiant","/com/example/projet_semestre6/AccueilController.fxml");
    }
}