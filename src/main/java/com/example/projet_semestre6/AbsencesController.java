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
import java.time.LocalDate;

public class AbsencesController {
    @FXML
    private TableView<Absence> TBLABSENCE; // Type spécifié
    @FXML
    private TableColumn<Absence, Integer> CLID; // Type spécifié
    @FXML
    private TableColumn<Absence, String> CLETUID; // Affichera le nom de l'étudiant
    @FXML
    private TableColumn<Absence, String> CLMATID; // Affichera le nom de la matière
    @FXML
    private TableColumn<Absence, LocalDate> CLDATEABS; // Type spécifié
    @FXML
    private TableColumn<Absence, String> CLTYPEABS; // Type spécifié
    @FXML
    private TableColumn<Absence, String> CLJUSTIF; // Type spécifié

    @FXML
    private ComboBox<Etudiant> ETUDIANT_ID; // Type spécifié
    @FXML
    private ComboBox<Matiere> MATIERE_ID; // Type spécifié

    @FXML
    private DatePicker DATE_ABS;
    @FXML
    private TextField TYPE_ABSENCE; // Un TextField pour le type d'absence
    @FXML
    private TextArea JUSTIFICATION; // Utilisation de TextArea pour la justification

    @FXML
    private Button BTNAJOUTER;
    @FXML
    private Button BTNMODIFIER;
    @FXML
    private Button BTNSUPPRIMER;
    @FXML
    private Button BTNRETOUR;

    private ObservableList<Absence> absencesList = FXCollections.observableArrayList();
    private ObservableList<Etudiant> etudiants = FXCollections.observableArrayList();
    private ObservableList<Matiere> matieres = FXCollections.observableArrayList();
    private DB db = DB.getInstance();

    @FXML
    public void initialize() {
        initTableView();
        loadEtudiants();
        loadMatieres();
        loadAbsences(); // Charge les absences après les dépendances
        setupTableViewSelectionListener();
    }

    private void initTableView() {
        CLID.setCellValueFactory(new PropertyValueFactory<>("id"));
        CLETUID.setCellValueFactory(new PropertyValueFactory<>("etudiantNomComplet"));
        CLMATID.setCellValueFactory(new PropertyValueFactory<>("matiereNom"));
        CLDATEABS.setCellValueFactory(new PropertyValueFactory<>("dateAbsence"));
        CLTYPEABS.setCellValueFactory(new PropertyValueFactory<>("typeAbsence"));
        CLJUSTIF.setCellValueFactory(new PropertyValueFactory<>("justification"));

        TBLABSENCE.setItems(absencesList);
    }

    private void loadEtudiants() {
        etudiants.clear();
        Connection conn = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        try {
            conn = db.getConnection();
            String sql = "SELECT id, nom, prenom FROM Etudiants";
            pstm = conn.prepareStatement(sql);
            rs = pstm.executeQuery();
            while (rs.next()) {
                etudiants.add(new Etudiant(rs.getInt("id"), rs.getString("nom"), rs.getString("prenom"), null, null, null, null, 0, null, null));
            }
            ETUDIANT_ID.setItems(etudiants);
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Erreur BD", "Impossible de charger les étudiants : " + e.getMessage());
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
                matieres.add(new Matiere(rs.getInt("id"), rs.getString("nom_matiere"), 0.0));
            }
            MATIERE_ID.setItems(matieres);
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Erreur BD", "Impossible de charger les matières : " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(rs, pstm, conn);
        }
    }

    private void loadAbsences() {
        absencesList.clear();
        Connection conn = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            conn = db.getConnection();
            String sql = "SELECT a.id, a.etudiant_id, e.nom AS etudiant_nom, e.prenom AS etudiant_prenom, " +
                    "a.matiere_id, m.nom_matiere, " +
                    "a.date_absence, a.type_absence, a.justification " +
                    "FROM Absences a " +
                    "JOIN Etudiants e ON a.etudiant_id = e.id " +
                    "JOIN Matieres m ON a.matiere_id = m.id";
            pstm = conn.prepareStatement(sql);
            rs = pstm.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                int etudiantId = rs.getInt("etudiant_id");
                String etudiantNomComplet = rs.getString("etudiant_nom") + " " + rs.getString("etudiant_prenom");
                int matiereId = rs.getInt("matiere_id");
                String matiereNom = rs.getString("nom_matiere");
                LocalDate dateAbsence = rs.getDate("date_absence").toLocalDate();
                String typeAbsence = rs.getString("type_absence");
                String justification = rs.getString("justification");

                absencesList.add(new Absence(id, etudiantId, etudiantNomComplet, matiereId, matiereNom,
                        dateAbsence, typeAbsence, justification));
            }
        } catch (SQLException ex) {
            showAlert(AlertType.ERROR, "Erreur BD", "Impossible de charger les absences : " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            closeResources(rs, pstm, conn);
        }
    }

    private void setupTableViewSelectionListener() {
        TBLABSENCE.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                // Sélectionner l'étudiant
                ETUDIANT_ID.getSelectionModel().select(
                        etudiants.stream().filter(e -> e.getId() == newSelection.getEtudiantId()).findFirst().orElse(null)
                );
                // Sélectionner la matière
                MATIERE_ID.getSelectionModel().select(
                        matieres.stream().filter(m -> m.getId() == newSelection.getMatiereId()).findFirst().orElse(null)
                );

                DATE_ABS.setValue(newSelection.getDateAbsence());
                TYPE_ABSENCE.setText(newSelection.getTypeAbsence());
                JUSTIFICATION.setText(newSelection.getJustification());
            } else {
                clearFields();
            }
        });
    }

    @FXML
    private void handleAjouter(ActionEvent event) {
        if (TBLABSENCE.getSelectionModel().getSelectedItem() != null) {
            showAlert(AlertType.INFORMATION, "Action", "Veuillez désélectionner l'absence pour en ajouter une nouvelle.");
            clearFields();
            return;
        }
        saveAbsence(null);
    }

    @FXML
    private void handleModifier(ActionEvent event) {
        Absence selectedAbsence = TBLABSENCE.getSelectionModel().getSelectedItem();
        if (selectedAbsence != null) {
            saveAbsence(selectedAbsence);
        } else {
            showAlert(AlertType.WARNING, "Attention", "Veuillez sélectionner une absence à modifier.");
        }
    }

    private void saveAbsence(Absence absenceToModify) {
        Etudiant selectedEtudiant = ETUDIANT_ID.getSelectionModel().getSelectedItem();
        Matiere selectedMatiere = MATIERE_ID.getSelectionModel().getSelectedItem();
        LocalDate dateAbsence = DATE_ABS.getValue();
        String typeAbsence = TYPE_ABSENCE.getText();
        String justification = JUSTIFICATION.getText();

        // Validation des champs
        if (selectedEtudiant == null || selectedMatiere == null || dateAbsence == null ||
                typeAbsence.isEmpty() || justification.isEmpty()) {
            showAlert(AlertType.WARNING, "Champs Manquants", "Veuillez remplir tous les champs obligatoires (Étudiant, Matière, Date, Type, Justification).");
            return;
        }

        Connection conn = null;
        PreparedStatement pstm = null;

        try {
            conn = db.getConnection();
            String sql;
            if (absenceToModify == null) { // Mode Ajout
                sql = "INSERT INTO Absences (etudiant_id, matiere_id, date_absence, type_absence, justification) VALUES (?, ?, ?, ?, ?)";
                pstm = conn.prepareStatement(sql);
                pstm.setInt(1, selectedEtudiant.getId());
                pstm.setInt(2, selectedMatiere.getId());
                pstm.setObject(3, dateAbsence); // LocalDate directement
                pstm.setString(4, typeAbsence);
                pstm.setString(5, justification);
                showAlert(AlertType.INFORMATION, "Succès", "Absence ajoutée avec succès !");
            } else { // Mode Modification
                sql = "UPDATE Absences SET etudiant_id = ?, matiere_id = ?, date_absence = ?, type_absence = ?, justification = ? WHERE id = ?";
                pstm = conn.prepareStatement(sql);
                pstm.setInt(1, selectedEtudiant.getId());
                pstm.setInt(2, selectedMatiere.getId());
                pstm.setObject(3, dateAbsence);
                pstm.setString(4, typeAbsence);
                pstm.setString(5, justification);
                pstm.setInt(6, absenceToModify.getId());
                showAlert(AlertType.INFORMATION, "Succès", "Absence modifiée avec succès !");
            }
            pstm.executeUpdate();
            loadAbsences(); // Recharger les données
            clearFields();
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Erreur BD", "Erreur lors de l'enregistrement de l'absence : " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(null, pstm, conn);
        }
    }

    @FXML
    private void handleSupprimer(ActionEvent event) {
        Absence selectedAbsence = TBLABSENCE.getSelectionModel().getSelectedItem();
        if (selectedAbsence == null) {
            showAlert(AlertType.WARNING, "Attention", "Veuillez sélectionner une absence à supprimer.");
            return;
        }

        Alert confirmAlert = new Alert(AlertType.CONFIRMATION, "Voulez-vous vraiment supprimer l'absence de " + selectedAbsence.getEtudiantNomComplet() + " pour la matière " + selectedAbsence.getMatiereNom() + " à la date du " + selectedAbsence.getDateAbsence() + " ?", ButtonType.YES, ButtonType.NO);
        confirmAlert.setTitle("Confirmation de Suppression");
        confirmAlert.setHeaderText(null);
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                Connection conn = null;
                PreparedStatement pstm = null;
                try {
                    conn = db.getConnection();
                    String sql = "DELETE FROM Absences WHERE id = ?";
                    pstm = conn.prepareStatement(sql);
                    pstm.setInt(1, selectedAbsence.getId());
                    pstm.executeUpdate();
                    showAlert(AlertType.INFORMATION, "Succès", "Absence supprimée avec succès !");
                    loadAbsences(); // Recharger les données
                    clearFields();
                } catch (SQLException e) {
                    showAlert(AlertType.ERROR, "Erreur BD", "Erreur lors de la suppression de l'absence : " + e.getMessage());
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
        ETUDIANT_ID.getSelectionModel().clearSelection();
        MATIERE_ID.getSelectionModel().clearSelection();
        DATE_ABS.setValue(null);
        TYPE_ABSENCE.clear();
        JUSTIFICATION.clear();
        TBLABSENCE.getSelectionModel().clearSelection();
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
            // Ne pas fermer la connexion DB ici si elle est gérée en singleton.
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void RETOUR(ActionEvent e) throws IOException {
        Outils.load(e,"page etudiant","/com/example/projet_semestre6/AccueilController.fxml");
    }
}