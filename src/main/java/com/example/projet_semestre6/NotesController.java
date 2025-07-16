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

public class NotesController {
    @FXML
    private TableView<Note> TBLNOTE; // Type spécifié
    @FXML
    private TableColumn<Note, Integer> CLIDNOTE; // Type spécifié
    @FXML
    private TableColumn<Note, String> CLETUID; // Affichera le nom de l'étudiant
    @FXML
    private TableColumn<Note, String> CLMATID; // Affichera le nom de la matière
    @FXML
    private TableColumn<Note, String> CLENSEIID; // Affichera le nom de l'enseignant
    @FXML
    private TableColumn<Note, LocalDate> CLDATENOTE;
    @FXML
    private TableColumn<Note, Double> CLVALNOTE; // Type spécifié
    @FXML
    private TableColumn<Note, String> CLTYPEEVALUATION;
    @FXML
    private TableColumn<Note, Double> CLCOEFFNOTE; // Type spécifié

    @FXML
    private ComboBox<Etudiant> ETUDIANT_ID; // Type spécifié
    @FXML
    private ComboBox<Matiere> MATIERE_ID; // Type spécifié
    @FXML
    private ComboBox<Enseignant> ENSEIGNANT_ID; // Type spécifié

    @FXML
    private DatePicker DATE_NOTE;
    @FXML
    private TextField TYPE_EVALUATION;
    @FXML
    private TextField COEFF_NOTE; // TextField pour le coefficient
    @FXML
    private TextField VALEUR_NOTE; // TextField pour la valeur de la note

    @FXML
    private Button BTNAJOUTER; // Bouton Ajouter
    @FXML
    private Button BTNMODIFIER; // Bouton Modifier
    @FXML
    private Button BTNSUPPRIMER; // Bouton Supprimer
    @FXML
    private Button BTNRETOUR; // Bouton Retour

    private ObservableList<Note> notesList = FXCollections.observableArrayList();
    private ObservableList<Etudiant> etudiants = FXCollections.observableArrayList();
    private ObservableList<Matiere> matieres = FXCollections.observableArrayList();
    private ObservableList<Enseignant> enseignants = FXCollections.observableArrayList();
    private DB db = DB.getInstance();

    @FXML
    public void initialize() {
        initTableView();
        loadEtudiants();
        loadMatieres();
        loadEnseignants();
        loadNotes(); // Charge les notes après les dépendances
        setupTableViewSelectionListener();
    }

    private void initTableView() {
        CLIDNOTE.setCellValueFactory(new PropertyValueFactory<>("id"));
        CLETUID.setCellValueFactory(new PropertyValueFactory<>("etudiantNomComplet"));
        CLMATID.setCellValueFactory(new PropertyValueFactory<>("matiereNom"));
        CLENSEIID.setCellValueFactory(new PropertyValueFactory<>("enseignantNomComplet"));
        CLDATENOTE.setCellValueFactory(new PropertyValueFactory<>("dateNote"));
        CLVALNOTE.setCellValueFactory(new PropertyValueFactory<>("valeurNote"));
        CLTYPEEVALUATION.setCellValueFactory(new PropertyValueFactory<>("typeEvaluation"));
        CLCOEFFNOTE.setCellValueFactory(new PropertyValueFactory<>("coeffNote"));

        TBLNOTE.setItems(notesList);
    }

    private void loadEtudiants() {
        etudiants.clear();
        Connection conn = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        try {
            conn = db.getConnection();
            // Récupère l'ID, nom et prénom de l'étudiant
            String sql = "SELECT id, nom, prenom FROM Etudiants";
            pstm = conn.prepareStatement(sql);
            rs = pstm.executeQuery();
            while (rs.next()) {
                // Créer un objet Etudiant minimal pour la ComboBox
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
                matieres.add(new Matiere(rs.getInt("id"), rs.getString("nom_matiere"), 0.0)); // 0.0 pour coefficient non utilisé
            }
            MATIERE_ID.setItems(matieres);
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Erreur BD", "Impossible de charger les matières : " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(rs, pstm, conn);
        }
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
                enseignants.add(new Enseignant(rs.getInt("id"), rs.getString("nom"), rs.getString("prenom"), null, null, null)); // null pour spécialité/tel/email non utilisés
            }
            ENSEIGNANT_ID.setItems(enseignants);
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Erreur BD", "Impossible de charger les enseignants : " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(rs, pstm, conn);
        }
    }

    private void loadNotes() {
        notesList.clear();
        Connection conn = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            conn = db.getConnection();
            String sql = "SELECT n.id, n.etudiant_id, e.nom AS etudiant_nom, e.prenom AS etudiant_prenom, " +
                    "n.matiere_id, m.nom_matiere, " +
                    "n.enseignant_id, ens.nom AS enseignant_nom, ens.prenom AS enseignant_prenom, " +
                    "n.date_note, n.valeur_note, n.type_evaluation, n.coeff_note " +
                    "FROM Notes n " +
                    "JOIN Etudiants e ON n.etudiant_id = e.id " +
                    "JOIN Matieres m ON n.matiere_id = m.id " +
                    "JOIN Enseignants ens ON n.enseignant_id = ens.id";
            pstm = conn.prepareStatement(sql);
            rs = pstm.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                int etudiantId = rs.getInt("etudiant_id");
                String etudiantNomComplet = rs.getString("etudiant_nom") + " " + rs.getString("etudiant_prenom");
                int matiereId = rs.getInt("matiere_id");
                String matiereNom = rs.getString("nom_matiere");
                int enseignantId = rs.getInt("enseignant_id");
                String enseignantNomComplet = rs.getString("enseignant_nom") + " " + rs.getString("enseignant_prenom");
                LocalDate dateNote = rs.getDate("date_note").toLocalDate();
                double valeurNote = rs.getDouble("valeur_note");
                String typeEvaluation = rs.getString("type_evaluation");
                double coeffNote = rs.getDouble("coeff_note");

                notesList.add(new Note(id, etudiantId, etudiantNomComplet, matiereId, matiereNom,
                        enseignantId, enseignantNomComplet, dateNote,
                        valeurNote, typeEvaluation, coeffNote));
            }
        } catch (SQLException ex) {
            showAlert(AlertType.ERROR, "Erreur Base de Données", "Impossible de charger les notes : " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            closeResources(rs, pstm, conn);
        }
    }

    private void setupTableViewSelectionListener() {
        TBLNOTE.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                // Sélectionner l'étudiant
                ETUDIANT_ID.getSelectionModel().select(
                        etudiants.stream().filter(e -> e.getId() == newSelection.getEtudiantId()).findFirst().orElse(null)
                );
                // Sélectionner la matière
                MATIERE_ID.getSelectionModel().select(
                        matieres.stream().filter(m -> m.getId() == newSelection.getMatiereId()).findFirst().orElse(null)
                );
                // Sélectionner l'enseignant
                ENSEIGNANT_ID.getSelectionModel().select(
                        enseignants.stream().filter(ens -> ens.getId() == newSelection.getEnseignantId()).findFirst().orElse(null)
                );

                DATE_NOTE.setValue(newSelection.getDateNote());
                VALEUR_NOTE.setText(String.valueOf(newSelection.getValeurNote()));
                TYPE_EVALUATION.setText(newSelection.getTypeEvaluation());
                COEFF_NOTE.setText(String.valueOf(newSelection.getCoeffNote()));
            } else {
                clearFields();
            }
        });
    }

    @FXML
    private void handleAjouter(ActionEvent event) {
        if (TBLNOTE.getSelectionModel().getSelectedItem() != null) {
            showAlert(AlertType.INFORMATION, "Action", "Veuillez désélectionner la note pour en ajouter une nouvelle.");
            clearFields();
            return;
        }
        saveNote(null);
    }

    @FXML
    private void handleModifier(ActionEvent event) {
        Note selectedNote = TBLNOTE.getSelectionModel().getSelectedItem();
        if (selectedNote != null) {
            saveNote(selectedNote);
        } else {
            showAlert(AlertType.WARNING, "Attention", "Veuillez sélectionner une note à modifier.");
        }
    }

    private void saveNote(Note noteToModify) {
        Etudiant selectedEtudiant = ETUDIANT_ID.getSelectionModel().getSelectedItem();
        Matiere selectedMatiere = MATIERE_ID.getSelectionModel().getSelectedItem();
        Enseignant selectedEnseignant = ENSEIGNANT_ID.getSelectionModel().getSelectedItem();
        LocalDate dateNote = DATE_NOTE.getValue();
        String valeurNoteText = VALEUR_NOTE.getText();
        String typeEvaluation = TYPE_EVALUATION.getText();
        String coeffNoteText = COEFF_NOTE.getText();

        // Validation des champs
        if (selectedEtudiant == null || selectedMatiere == null || selectedEnseignant == null ||
                dateNote == null || valeurNoteText.isEmpty() || typeEvaluation.isEmpty() || coeffNoteText.isEmpty()) {
            showAlert(AlertType.WARNING, "Champs Manquants", "Veuillez remplir tous les champs obligatoires.");
            return;
        }

        double valeurNote;
        double coeffNote;
        try {
            valeurNote = Double.parseDouble(valeurNoteText);
            coeffNote = Double.parseDouble(coeffNoteText);
        } catch (NumberFormatException e) {
            showAlert(AlertType.ERROR, "Saisie Invalide", "La valeur et le coefficient de la note doivent être des nombres.");
            return;
        }

        Connection conn = null;
        PreparedStatement pstm = null;

        try {
            conn = db.getConnection();
            String sql;
            if (noteToModify == null) { // Mode Ajout
                sql = "INSERT INTO Notes (etudiant_id, matiere_id, enseignant_id, date_note, valeur_note, type_evaluation, coeff_note) VALUES (?, ?, ?, ?, ?, ?, ?)";
                pstm = conn.prepareStatement(sql);
                pstm.setInt(1, selectedEtudiant.getId());
                pstm.setInt(2, selectedMatiere.getId());
                pstm.setInt(3, selectedEnseignant.getId());
                pstm.setObject(4, dateNote); // LocalDate directement
                pstm.setDouble(5, valeurNote);
                pstm.setString(6, typeEvaluation);
                pstm.setDouble(7, coeffNote);
                showAlert(AlertType.INFORMATION, "Succès", "Note ajoutée avec succès !");
            } else { // Mode Modification
                sql = "UPDATE Notes SET etudiant_id = ?, matiere_id = ?, enseignant_id = ?, date_note = ?, valeur_note = ?, type_evaluation = ?, coeff_note = ? WHERE id = ?";
                pstm = conn.prepareStatement(sql);
                pstm.setInt(1, selectedEtudiant.getId());
                pstm.setInt(2, selectedMatiere.getId());
                pstm.setInt(3, selectedEnseignant.getId());
                pstm.setObject(4, dateNote);
                pstm.setDouble(5, valeurNote);
                pstm.setString(6, typeEvaluation);
                pstm.setDouble(7, coeffNote);
                pstm.setInt(8, noteToModify.getId());
                showAlert(AlertType.INFORMATION, "Succès", "Note modifiée avec succès !");
            }
            pstm.executeUpdate();
            loadNotes(); // Recharger les données
            clearFields();
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Erreur BD", "Erreur lors de l'enregistrement de la note : " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(null, pstm, conn);
        }
    }

    @FXML
    private void handleSupprimer(ActionEvent event) {
        Note selectedNote = TBLNOTE.getSelectionModel().getSelectedItem();
        if (selectedNote == null) {
            showAlert(AlertType.WARNING, "Attention", "Veuillez sélectionner une note à supprimer.");
            return;
        }

        Alert confirmAlert = new Alert(AlertType.CONFIRMATION, "Voulez-vous vraiment supprimer cette note pour " + selectedNote.getEtudiantNomComplet() + " en " + selectedNote.getMatiereNom() + " (" + selectedNote.getValeurNote() + ") ?", ButtonType.YES, ButtonType.NO);
        confirmAlert.setTitle("Confirmation de Suppression");
        confirmAlert.setHeaderText(null);
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                Connection conn = null;
                PreparedStatement pstm = null;
                try {
                    conn = db.getConnection();
                    String sql = "DELETE FROM Notes WHERE id = ?";
                    pstm = conn.prepareStatement(sql);
                    pstm.setInt(1, selectedNote.getId());
                    pstm.executeUpdate();
                    showAlert(AlertType.INFORMATION, "Succès", "Note supprimée avec succès !");
                    loadNotes(); // Recharger les données
                    clearFields();
                } catch (SQLException e) {
                    showAlert(AlertType.ERROR, "Erreur BD", "Erreur lors de la suppression de la note : " + e.getMessage());
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
        ENSEIGNANT_ID.getSelectionModel().clearSelection();
        DATE_NOTE.setValue(null);
        VALEUR_NOTE.clear();
        TYPE_EVALUATION.clear();
        COEFF_NOTE.clear();
        TBLNOTE.getSelectionModel().clearSelection();
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