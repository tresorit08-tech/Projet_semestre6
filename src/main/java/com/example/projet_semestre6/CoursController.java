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
import java.time.LocalTime; // Importez LocalTime

public class CoursController {
    @FXML
    private ComboBox<Matiere> MATIERE_ID; // Spécifié le type
    @FXML
    private ComboBox<Classe> CLASSE_ID; // Spécifié le type
    @FXML
    private ComboBox<Enseignant> ENSEIGNANT_ID; // Spécifié le type

    @FXML
    private DatePicker JOUR_SEMAINE;
    @FXML
    private TextField SALL; // Reste un TextField pour la salle
    // Changé en TextField pour les heures pour une saisie simple "HH:MM"
    @FXML
    private TextField H_DEBUT;
    @FXML
    private TextField H_FIN;

    @FXML
    private Button BTNAJOUTER; // Ajouté pour l'opération d'ajout
    @FXML
    private Button BTNMODIFIER; // Ajouté pour l'opération de modification
    @FXML
    private Button BTNSUPPRIMER; // Ajouté pour l'opération de suppression

    @FXML
    private TableView<Cours> TBLCOURS; // Spécifié le type Cours
    @FXML
    private TableColumn<Cours, Integer> CLIDCOURS; // Spécifié le type Cours
    @FXML
    private TableColumn<Cours, String> CLMATIEREID; // Affichera le nom de la matière
    @FXML
    private TableColumn<Cours, String> CLENSEIGNANTID; // Affichera le nom de l'enseignant
    @FXML
    private TableColumn<Cours, String> CLCLASSEID; // Affichera le nom de la classe
    @FXML
    private TableColumn<Cours, LocalDate> CLJOURSEMAINE;
    @FXML
    private TableColumn<Cours, LocalTime> CLHDEBUT; // Spécifié LocalTime
    @FXML
    private TableColumn<Cours, LocalTime> CLHFIN; // Spécifié LocalTime
    @FXML
    private TableColumn<Cours, String> CLSALL;

    @FXML
    private Button BTNRETOUR;

    private ObservableList<Cours> coursList = FXCollections.observableArrayList();
    private ObservableList<Matiere> matieres = FXCollections.observableArrayList();
    private ObservableList<Classe> classes = FXCollections.observableArrayList();
    private ObservableList<Enseignant> enseignants = FXCollections.observableArrayList();
    private DB db = DB.getInstance();

    @FXML
    public void initialize() {
        initTableView();
        loadMatieres();
        loadClasses();
        loadEnseignants();
        loadCours(); // Charge les cours après avoir chargé les dépendances
        setupTableViewSelectionListener();
    }

    private void initTableView() {
        CLIDCOURS.setCellValueFactory(new PropertyValueFactory<>("id"));
        CLMATIEREID.setCellValueFactory(new PropertyValueFactory<>("matiereNom")); // Propriété du nom
        CLENSEIGNANTID.setCellValueFactory(new PropertyValueFactory<>("enseignantNomComplet")); // Propriété du nom
        CLCLASSEID.setCellValueFactory(new PropertyValueFactory<>("classeNom")); // Propriété du nom
        CLJOURSEMAINE.setCellValueFactory(new PropertyValueFactory<>("jourSemaine"));
        CLHDEBUT.setCellValueFactory(new PropertyValueFactory<>("heureDebut"));
        CLHFIN.setCellValueFactory(new PropertyValueFactory<>("heureFin"));
        CLSALL.setCellValueFactory(new PropertyValueFactory<>("salle"));

        TBLCOURS.setItems(coursList);
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

    private void loadClasses() {
        classes.clear();
        Connection conn = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        try {
            conn = db.getConnection();
            String sql = "SELECT id, nom_classe FROM Classes";
            pstm = conn.prepareStatement(sql);
            rs = pstm.executeQuery();
            while (rs.next()) {
                classes.add(new Classe(rs.getInt("id"), rs.getString("nom_classe")));
            }
            CLASSE_ID.setItems(classes);
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Erreur BD", "Impossible de charger les classes : " + e.getMessage());
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

    private void loadCours() {
        coursList.clear();
        Connection conn = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            conn = db.getConnection();
            String sql = "SELECT c.id, c.matiere_id, m.nom_matiere, " +
                    "c.enseignant_id, e.nom AS enseignant_nom, e.prenom AS enseignant_prenom, " +
                    "c.classe_id, cl.nom_classe, " +
                    "c.jour_semaine, c.heure_debut, c.heure_fin, c.salle " +
                    "FROM Cours c " +
                    "JOIN Matieres m ON c.matiere_id = m.id " +
                    "JOIN Enseignants e ON c.enseignant_id = e.id " +
                    "JOIN Classes cl ON c.classe_id = cl.id";
            pstm = conn.prepareStatement(sql);
            rs = pstm.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                int matiereId = rs.getInt("matiere_id");
                String matiereNom = rs.getString("nom_matiere");
                int enseignantId = rs.getInt("enseignant_id");
                String enseignantNomComplet = rs.getString("enseignant_nom") + " " + rs.getString("enseignant_prenom");
                int classeId = rs.getInt("classe_id");
                String classeNom = rs.getString("nom_classe");
                LocalDate jourSemaine = rs.getDate("jour_semaine").toLocalDate(); // Convertir java.sql.Date en LocalDate
                LocalTime heureDebut = rs.getTime("heure_debut").toLocalTime();   // Convertir java.sql.Time en LocalTime
                LocalTime heureFin = rs.getTime("heure_fin").toLocalTime();       // Convertir java.sql.Time en LocalTime
                String salle = rs.getString("salle");

                coursList.add(new Cours(id, matiereId, matiereNom, enseignantId, enseignantNomComplet,
                        classeId, classeNom, jourSemaine, heureDebut, heureFin, salle));
            }
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Erreur BD", "Impossible de charger les cours : " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(rs, pstm, conn);
        }
    }

    private void setupTableViewSelectionListener() {
        TBLCOURS.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                // Sélectionner la matière dans la ComboBox
                MATIERE_ID.getSelectionModel().select(
                        matieres.stream().filter(m -> m.getId() == newSelection.getMatiereId()).findFirst().orElse(null)
                );
                // Sélectionner l'enseignant dans la ComboBox
                ENSEIGNANT_ID.getSelectionModel().select(
                        enseignants.stream().filter(e -> e.getId() == newSelection.getEnseignantId()).findFirst().orElse(null)
                );
                // Sélectionner la classe dans la ComboBox
                CLASSE_ID.getSelectionModel().select(
                        classes.stream().filter(cl -> cl.getId() == newSelection.getClasseId()).findFirst().orElse(null)
                );
                JOUR_SEMAINE.setValue(newSelection.getJourSemaine());
                H_DEBUT.setText(newSelection.getHeureDebut().toString()); // Convertir LocalTime en String
                H_FIN.setText(newSelection.getHeureFin().toString());     // Convertir LocalTime en String
                SALL.setText(newSelection.getSalle());
            } else {
                clearFields();
            }
        });
    }

    @FXML
    private void handleAjouter(ActionEvent event) {
        if (TBLCOURS.getSelectionModel().getSelectedItem() != null) {
            showAlert(AlertType.INFORMATION, "Action", "Veuillez désélectionner le cours pour ajouter un nouveau.");
            clearFields();
            return;
        }
        saveCours(null);
    }

    @FXML
    private void handleModifier(ActionEvent event) {
        Cours selectedCours = TBLCOURS.getSelectionModel().getSelectedItem();
        if (selectedCours != null) {
            saveCours(selectedCours);
        } else {
            showAlert(AlertType.WARNING, "Attention", "Veuillez sélectionner un cours à modifier.");
        }
    }

    private void saveCours(Cours coursToModify) {
        Matiere selectedMatiere = MATIERE_ID.getSelectionModel().getSelectedItem();
        Enseignant selectedEnseignant = ENSEIGNANT_ID.getSelectionModel().getSelectedItem();
        Classe selectedClasse = CLASSE_ID.getSelectionModel().getSelectedItem();
        LocalDate jourSemaine = JOUR_SEMAINE.getValue();
        String heureDebutText = H_DEBUT.getText();
        String heureFinText = H_FIN.getText();
        String salle = SALL.getText();

        // Validation des champs
        if (selectedMatiere == null || selectedEnseignant == null || selectedClasse == null ||
                jourSemaine == null || heureDebutText.isEmpty() || heureFinText.isEmpty() || salle.isEmpty()) {
            showAlert(AlertType.WARNING, "Champs Manquants", "Veuillez remplir tous les champs obligatoires.");
            return;
        }

        LocalTime heureDebut;
        LocalTime heureFin;
        try {
            heureDebut = LocalTime.parse(heureDebutText); // Parse "HH:MM"
            heureFin = LocalTime.parse(heureFinText);     // Parse "HH:MM"
        } catch (java.time.format.DateTimeParseException e) {
            showAlert(AlertType.ERROR, "Format d'Heure Invalide", "Veuillez saisir les heures au format HH:MM (ex: 08:30).");
            return;
        }

        Connection conn = null;
        PreparedStatement pstm = null;

        try {
            conn = db.getConnection();
            String sql;
            if (coursToModify == null) { // Mode Ajout
                sql = "INSERT INTO Cours (matiere_id, enseignant_id, classe_id, jour_semaine, heure_debut, heure_fin, salle) VALUES (?, ?, ?, ?, ?, ?, ?)";
                pstm = conn.prepareStatement(sql);
                pstm.setInt(1, selectedMatiere.getId());
                pstm.setInt(2, selectedEnseignant.getId());
                pstm.setInt(3, selectedClasse.getId());
                pstm.setObject(4, jourSemaine); // LocalDate directly
                pstm.setObject(5, heureDebut);   // LocalTime directly
                pstm.setObject(6, heureFin);     // LocalTime directly
                pstm.setString(7, salle);
                showAlert(AlertType.INFORMATION, "Succès", "Cours ajouté avec succès !");
            } else { // Mode Modification
                sql = "UPDATE Cours SET matiere_id = ?, enseignant_id = ?, classe_id = ?, jour_semaine = ?, heure_debut = ?, heure_fin = ?, salle = ? WHERE id = ?";
                pstm = conn.prepareStatement(sql);
                pstm.setInt(1, selectedMatiere.getId());
                pstm.setInt(2, selectedEnseignant.getId());
                pstm.setInt(3, selectedClasse.getId());
                pstm.setObject(4, jourSemaine);
                pstm.setObject(5, heureDebut);
                pstm.setObject(6, heureFin);
                pstm.setString(7, salle);
                pstm.setInt(8, coursToModify.getId());
                showAlert(AlertType.INFORMATION, "Succès", "Cours modifié avec succès !");
            }
            pstm.executeUpdate();
            loadCours(); // Recharger les données
            clearFields();
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Erreur BD", "Erreur lors de l'enregistrement du cours : " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(null, pstm, conn);
        }
    }

    @FXML
    private void handleSupprimer(ActionEvent event) {
        Cours selectedCours = TBLCOURS.getSelectionModel().getSelectedItem();
        if (selectedCours == null) {
            showAlert(AlertType.WARNING, "Attention", "Veuillez sélectionner un cours à supprimer.");
            return;
        }

        Alert confirmAlert = new Alert(AlertType.CONFIRMATION, "Voulez-vous vraiment supprimer ce cours (Matière: " + selectedCours.getMatiereNom() + ", Salle: " + selectedCours.getSalle() + ") ?", ButtonType.YES, ButtonType.NO);
        confirmAlert.setTitle("Confirmation de Suppression");
        confirmAlert.setHeaderText(null);
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                Connection conn = null;
                PreparedStatement pstm = null;
                try {
                    conn = db.getConnection();
                    String sql = "DELETE FROM Cours WHERE id = ?";
                    pstm = conn.prepareStatement(sql);
                    pstm.setInt(1, selectedCours.getId());
                    pstm.executeUpdate();
                    showAlert(AlertType.INFORMATION, "Succès", "Cours supprimé avec succès !");
                    loadCours(); // Recharger les données
                    clearFields();
                } catch (SQLException e) {
                    showAlert(AlertType.ERROR, "Erreur BD", "Erreur lors de la suppression du cours : " + e.getMessage());
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
        MATIERE_ID.getSelectionModel().clearSelection();
        CLASSE_ID.getSelectionModel().clearSelection();
        ENSEIGNANT_ID.getSelectionModel().clearSelection();
        JOUR_SEMAINE.setValue(null);
        H_DEBUT.clear();
        H_FIN.clear();
        SALL.clear();
        TBLCOURS.getSelectionModel().clearSelection();
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