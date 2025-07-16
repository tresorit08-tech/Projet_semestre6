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

public class EnseignantMatiereController {
    @FXML
    private Button BTNRETOUR;

    @FXML
    private ComboBox<Enseignant> SELECTIONENSEIGNANT; // Type spécifique pour les objets Enseignant

    @FXML
    private ComboBox<Matiere> SELECTIONMATIERE; // Type spécifique pour les objets Matiere

    @FXML
    private Button BTNAFFECTATION;

    @FXML
    private Button BTNSUPPRIMERAFFECTATION; // Ajouté pour la suppression

    @FXML
    private TableView<EnseignantsMatieres> TBLENSEIGNATMATIERE; // Type spécifique pour les objets EnseignantsMatieres

    @FXML
    private TableColumn<EnseignantsMatieres, Integer> CLIDENSEIGNANTMATIERE; // ID d'affectation (ou combo ID)

    @FXML
    private TableColumn<EnseignantsMatieres, String> CLENSEIGNANT; // Affichera le nom de l'enseignant

    @FXML
    private TableColumn<EnseignantsMatieres, String> CLMATIERE; // Affichera le nom de la matière

    private ObservableList<Enseignant> enseignants = FXCollections.observableArrayList();
    private ObservableList<Matiere> matieres = FXCollections.observableArrayList();
    private ObservableList<EnseignantsMatieres> affectations = FXCollections.observableArrayList();
    private DB db = DB.getInstance();

    @FXML
    public void initialize() {
        initTableView();
        loadEnseignants();
        loadMatieres();
        loadAffectations();
    }

    private void initTableView() {
        // Associe les colonnes aux propriétés des objets EnseignantsMatieres
        // CLIDENSEIGNANTMATIERE pourrait montrer enseignantId ou une combinaison des deux pour débogage
        CLIDENSEIGNANTMATIERE.setCellValueFactory(new PropertyValueFactory<>("enseignantId")); // Ou un champ calculé si vous voulez un ID unique pour l'affectation
        CLENSEIGNANT.setCellValueFactory(new PropertyValueFactory<>("enseignantNomComplet")); // Propriété pour le nom complet de l'enseignant
        CLMATIERE.setCellValueFactory(new PropertyValueFactory<>("matiereNom")); // Propriété pour le nom de la matière

        TBLENSEIGNATMATIERE.setItems(affectations);
    }

    private void loadEnseignants() {
        enseignants.clear();
        Connection conn = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        try {
            conn = db.getConnection();
            String sql = "SELECT id, nom, prenom FROM Enseignants";
            pstm = conn.prepareStatement(sql);
            rs = pstm.executeQuery();
            while (rs.next()) {
                // Utiliser la classe Enseignant existante
                enseignants.add(new Enseignant(rs.getInt("id"), rs.getString("nom"), rs.getString("prenom"), null, null, null));
            }
            SELECTIONENSEIGNANT.setItems(enseignants);
            // La méthode toString() de Enseignant gérera l'affichage dans la ComboBox
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Erreur BD", "Impossible de charger les enseignants : " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(rs, pstm, conn);
        }
    }

    private void loadMatieres() {
        matieres.clear();
        Connection conn = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        try {
            conn = db.getConnection();
            String sql = "SELECT id, nom_matiere FROM Matieres";
            pstm = conn.prepareStatement(sql);
            rs = pstm.executeQuery();
            while (rs.next()) {
                // Utiliser la classe Matiere existante
                matieres.add(new Matiere(rs.getInt("id"), rs.getString("nom_matiere"), 0.0)); // Coefficient à 0.0 car non utilisé ici
            }
            SELECTIONMATIERE.setItems(matieres);
            // La méthode toString() de Matiere gérera l'affichage dans la ComboBox
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Erreur BD", "Impossible de charger les matières : " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(rs, pstm, conn);
        }
    }

    private void loadAffectations() {
        affectations.clear();
        Connection conn = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        try {
            conn = db.getConnection();
            // Requête pour récupérer les affectations avec les noms des enseignants et matières
            String sql = "SELECT em.enseignant_id, e.nom, e.prenom, em.matiere_id, m.nom_matiere " +
                    "FROM Enseignants_Matieres em " +
                    "JOIN Enseignants e ON em.enseignant_id = e.id " +
                    "JOIN Matieres m ON em.matiere_id = m.id";
            pstm = conn.prepareStatement(sql);
            rs = pstm.executeQuery();
            while (rs.next()) {
                int enseignantId = rs.getInt("enseignant_id");
                String enseignantNomComplet = rs.getString("nom") + " " + rs.getString("prenom");
                int matiereId = rs.getInt("matiere_id");
                String matiereNom = rs.getString("nom_matiere");
                affectations.add(new EnseignantsMatieres(enseignantId, enseignantNomComplet, matiereId, matiereNom));
            }
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Erreur BD", "Impossible de charger les affectations : " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(rs, pstm, conn);
        }
    }

    @FXML
    private void handleAffectation(ActionEvent event) {
        Enseignant selectedEnseignant = SELECTIONENSEIGNANT.getSelectionModel().getSelectedItem();
        Matiere selectedMatiere = SELECTIONMATIERE.getSelectionModel().getSelectedItem();

        if (selectedEnseignant == null || selectedMatiere == null) {
            showAlert(AlertType.WARNING, "Champs Manquants", "Veuillez sélectionner un enseignant et une matière.");
            return;
        }

        // Vérifier si l'affectation existe déjà
        boolean alreadyExists = affectations.stream().anyMatch(
                a -> a.getEnseignantId() == selectedEnseignant.getId() && a.getMatiereId() == selectedMatiere.getId()
        );

        if (alreadyExists) {
            showAlert(AlertType.INFORMATION, "Affectation Existante", "Cet enseignant est déjà affecté à cette matière.");
            return;
        }

        Connection conn = null;
        PreparedStatement pstm = null;
        try {
            conn = db.getConnection();
            String sql = "INSERT INTO Enseignants_Matieres (enseignant_id, matiere_id) VALUES (?, ?)";
            pstm = conn.prepareStatement(sql);
            pstm.setInt(1, selectedEnseignant.getId());
            pstm.setInt(2, selectedMatiere.getId());
            pstm.executeUpdate();
            showAlert(AlertType.INFORMATION, "Succès", "Affectation ajoutée avec succès !");
            loadAffectations(); // Recharger les affectations
            clearFields();
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Erreur BD", "Erreur lors de l'ajout de l'affectation : " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(null, pstm, conn);
        }
    }

    @FXML
    private void handleSupprimerAffectation(ActionEvent event) {
        EnseignantsMatieres selectedAffectation = TBLENSEIGNATMATIERE.getSelectionModel().getSelectedItem();
        if (selectedAffectation == null) {
            showAlert(AlertType.WARNING, "Attention", "Veuillez sélectionner une affectation à supprimer dans le tableau.");
            return;
        }

        Alert confirmAlert = new Alert(AlertType.CONFIRMATION, "Voulez-vous vraiment supprimer l'affectation : " + selectedAffectation.getEnseignantNomComplet() + " - " + selectedAffectation.getMatiereNom() + " ?", ButtonType.YES, ButtonType.NO);
        confirmAlert.setTitle("Confirmation de Suppression");
        confirmAlert.setHeaderText(null);
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                Connection conn = null;
                PreparedStatement pstm = null;
                try {
                    conn = db.getConnection();
                    String sql = "DELETE FROM Enseignants_Matieres WHERE enseignant_id = ? AND matiere_id = ?";
                    pstm = conn.prepareStatement(sql);
                    pstm.setInt(1, selectedAffectation.getEnseignantId());
                    pstm.setInt(2, selectedAffectation.getMatiereId());
                    pstm.executeUpdate();
                    showAlert(AlertType.INFORMATION, "Succès", "Affectation supprimée avec succès !");
                    loadAffectations(); // Recharger les affectations
                    clearFields();
                } catch (SQLException e) {
                    showAlert(AlertType.ERROR, "Erreur BD", "Erreur lors de la suppression de l'affectation : " + e.getMessage());
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
        SELECTIONENSEIGNANT.getSelectionModel().clearSelection();
        SELECTIONMATIERE.getSelectionModel().clearSelection();
        TBLENSEIGNATMATIERE.getSelectionModel().clearSelection();
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
            // Important:Ne pas fermer la connexion ici si DB.getConnection() gère un singleton
            // carcela fermerait la connexion globale pour toute l'application.
            // La classe DB elle-même devrait gérer la fermeture de sa connexion
            // en fin d'application (par exemple, avec un ShutdownHook).
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void RETOUR(ActionEvent e) throws IOException {
        Outils.load(e,"page etudiant","/com/example/projet_semestre6/AccueilController.fxml");
    }
}