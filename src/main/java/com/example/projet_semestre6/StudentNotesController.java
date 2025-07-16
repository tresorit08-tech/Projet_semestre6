package com.example.projet_semestre6;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDate;

/**
 * Contrôleur pour l'affichage des notes détaillées d'un étudiant spécifique.
 * Récupère les notes de l'étudiant connecté et les affiche dans un tableau.
 */
public class StudentNotesController {

    @FXML
    private Label lblStudentName; // Label pour afficher le nom de l'étudiant

    @FXML
    private TableView<NoteDetail> tblStudentNotes; // Tableau pour afficher les notes
    @FXML
    private TableColumn<NoteDetail, String> colMatiere;
    @FXML
    private TableColumn<NoteDetail, String> colTypeEvaluation;
    @FXML
    private TableColumn<NoteDetail, Double> colValeurNote;
    @FXML
    private TableColumn<NoteDetail, Double> colCoeffNote;
    @FXML
    private TableColumn<NoteDetail, LocalDate> colDateNote;
    @FXML
    private TableColumn<NoteDetail, String> colEnseignant;

    private ObservableList<NoteDetail> studentNotesList = FXCollections.observableArrayList();
    private DB db = DB.getInstance();
    private int currentEtudiantId; // Pour stocker l'ID de l'étudiant connecté

    // Pour formater les notes à deux décimales
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.00");

    /**
     * Méthode d'initialisation. Configure les colonnes du tableau.
     */
    @FXML
    public void initialize() {
        System.out.println("DEBUG StudentNotesController: initialize() appelé.");
        colMatiere.setCellValueFactory(new PropertyValueFactory<>("matiereNom"));
        colTypeEvaluation.setCellValueFactory(new PropertyValueFactory<>("typeEvaluation"));
        colValeurNote.setCellValueFactory(new PropertyValueFactory<>("valeurNote"));
        colCoeffNote.setCellValueFactory(new PropertyValueFactory<>("coeffNote"));
        colDateNote.setCellValueFactory(new PropertyValueFactory<>("dateNote"));
        colEnseignant.setCellValueFactory(new PropertyValueFactory<>("enseignantNom"));

        tblStudentNotes.setItems(studentNotesList);
        System.out.println("DEBUG StudentNotesController: TableView configuré.");
    }

    /**
     * Méthode appelée par Outils.loadStudentNotesPage pour afficher les notes
     * de l'étudiant spécifié.
     * @param etudiantId L'ID de l'étudiant dont les notes doivent être affichées.
     */
    public void showStudentNotes(int etudiantId) {
        this.currentEtudiantId = etudiantId;
        System.out.println("DEBUG StudentNotesController: showStudentNotes() appelé pour etudiantId = " + etudiantId);
        loadStudentInfoAndNotes(etudiantId);
    }

    /**
     * Charge les informations de l'étudiant et ses notes depuis la base de données.
     * @param etudiantId L'ID de l'étudiant.
     */
    private void loadStudentInfoAndNotes(int etudiantId) {
        Connection conn = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        studentNotesList.clear(); // Efface les notes précédentes

        try {
            conn = db.getConnection();
            if (conn == null || conn.isClosed()) {
                showAlert(AlertType.ERROR, "Erreur Connexion BD", "La connexion à la base de données est invalide.");
                return;
            }

            // Récupérer le nom de l'étudiant pour le label
            String studentNameSql = "SELECT nom, prenom FROM Etudiants WHERE id = ?";
            pstm = conn.prepareStatement(studentNameSql);
            pstm.setInt(1, etudiantId);
            rs = pstm.executeQuery();
            if (rs.next()) {
                lblStudentName.setText("Notes pour : " + rs.getString("prenom") + " " + rs.getString("nom"));
            }
            closeResources(rs, pstm, null);
            rs = null;
            pstm = null;

            // Récupérer toutes les notes de l'étudiant connecté
            String notesSql = "SELECT n.valeur_note, n.type_evaluation, n.coeff_note, n.date_note, " +
                    "m.nom_matiere, ens.nom AS enseignant_nom, ens.prenom AS enseignant_prenom " +
                    "FROM Notes n " +
                    "JOIN Matieres m ON n.matiere_id = m.id " +
                    "JOIN Enseignants ens ON n.enseignant_id = ens.id " +
                    "WHERE n.etudiant_id = ? " + // FILTRAGE PAR L'ID DE L'ÉTUDIANT CONNECTÉ
                    "ORDER BY m.nom_matiere, n.date_note";

            pstm = conn.prepareStatement(notesSql);
            pstm.setInt(1, etudiantId); // Lie l'ID de l'étudiant connecté à la requête
            System.out.println("DEBUG StudentNotesController: Exécution SQL (Notes): " + notesSql + " avec etudiant_id=" + etudiantId);
            rs = pstm.executeQuery();

            int notesCount = 0;
            while (rs.next()) {
                notesCount++;
                String matiereNom = rs.getString("nom_matiere");
                String typeEvaluation = rs.getString("type_evaluation");
                double valeurNote = rs.getDouble("valeur_note");
                double coeffNote = rs.getDouble("coeff_note");
                LocalDate dateNote = rs.getDate("date_note").toLocalDate();
                String enseignantNom = rs.getString("enseignant_prenom") + " " + rs.getString("enseignant_nom");

                // AJOUT DE LIGNES DE DÉBOGAGE POUR VÉRIFIER LES DONNÉES LUES
                System.out.println("DEBUG Note lue: Matière=" + matiereNom + ", Type=" + typeEvaluation +
                        ", Note=" + valeurNote + ", Coeff=" + coeffNote +
                        ", Date=" + dateNote + ", Enseignant=" + enseignantNom);

                studentNotesList.add(new NoteDetail(matiereNom, typeEvaluation, valeurNote, coeffNote, dateNote, enseignantNom));
            }
            System.out.println("DEBUG StudentNotesController: " + notesCount + " notes récupérées pour l'étudiant ID: " + etudiantId);

            if (notesCount == 0) {
                showAlert(AlertType.INFORMATION, "Aucune Note", "Aucune note trouvée pour cet étudiant.");
            }

        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Erreur Base de Données", "Impossible de charger les notes : " + e.getMessage());
            e.printStackTrace();
            System.err.println("SQL Exception dans loadStudentInfoAndNotes: " + e.getMessage());
        } finally {
            closeResources(rs, pstm, conn);
        }
    }

    /**
     * Gère l'action du bouton "Retour au Tableau de Bord".
     * @param event L'événement d'action.
     */
    @FXML
    public void handleReturnToDashboard(ActionEvent event) { // Rendu public pour être accessible depuis FXML
        try {
            Outils.load(event, "PROJET_6 - Tableau de Bord Étudiant", "/com/example/projet_semestre6/StudentDashboard.fxml");
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Erreur de Navigation", "Impossible de revenir au tableau de bord : " + e.getMessage());
            e.printStackTrace();
        }
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
            if (conn != null) conn.close(); // Ferme la connexion car elle est ouverte spécifiquement ici
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Classe interne pour représenter une ligne de note détaillée dans le TableView.
     * Cette classe doit avoir des getters pour toutes les propriétés utilisées par PropertyValueFactory.
     */
    public static class NoteDetail {
        private final String matiereNom;
        private final String typeEvaluation;
        private final double valeurNote;
        private final double coeffNote;
        private final LocalDate dateNote;
        private final String enseignantNom;

        public NoteDetail(String matiereNom, String typeEvaluation, double valeurNote, double coeffNote, LocalDate dateNote, String enseignantNom) {
            this.matiereNom = matiereNom;
            this.typeEvaluation = typeEvaluation;
            this.valeurNote = valeurNote;
            this.coeffNote = coeffNote;
            this.dateNote = dateNote;
            this.enseignantNom = enseignantNom;
        }

        // Getters nécessaires pour PropertyValueFactory
        public String getMatiereNom() { return matiereNom; }
        public String getTypeEvaluation() { return typeEvaluation; }
        public double getValeurNote() { return valeurNote; }
        public double getCoeffNote() { return coeffNote; }
        public LocalDate getDateNote() { return dateNote; }
        public String getEnseignantNom() { return enseignantNom; }
    }
}
