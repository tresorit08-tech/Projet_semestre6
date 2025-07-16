package com.example.projet_semestre6;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.application.Platform;
import javafx.scene.layout.VBox; // Import pour le nouveau VBox des détails d'absence
import javafx.scene.text.Font; // Import pour la police de texte des labels dynamiques

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator; // Pour le tri des rangs
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap; // Pour maintenir l'ordre d'insertion des notes par matière

/**
 * Contrôleur pour l'affichage détaillé du bulletin de notes d'un étudiant.
 * Cette classe est responsable de la récupération, du calcul et de la présentation
 * des moyennes par matière, des mentions et du rang de l'étudiant,
 * ainsi que des informations sur les absences et l'appréciation générale.
 */
public class BulletinController {

    // --- Éléments FXML liés à l'interface utilisateur ---
    @FXML
    private Label lblEtablissementName; // Nom de l'établissement
    @FXML
    private Label lblAcademicYear;      // Année scolaire
    @FXML
    private Label lblStudentName;       // Nom complet de l'étudiant
    @FXML
    private Label lblStudentClass;      // Classe de l'étudiant

    @FXML
    private TableView<BulletinDetailMatiere> tblMatiereDetails; // Tableau des détails par matière
    @FXML
    private TableColumn<BulletinDetailMatiere, String> colMatiereNom;          // Nom de la matière
    @FXML
    private TableColumn<BulletinDetailMatiere, Double> colMatiereCoeff;         // Coefficient de la matière
    @FXML
    private TableColumn<BulletinDetailMatiere, List<String>> colNotesIndividuelles; // Notes individuelles formatées
    @FXML
    private TableColumn<BulletinDetailMatiere, Double> colMoyenneMatiere;      // Moyenne de la matière
    @FXML
    private TableColumn<BulletinDetailMatiere, String> colMentionMatiere;      // Mention de la matière

    @FXML
    private Label lblGeneralAverage;    // Moyenne générale de l'étudiant
    @FXML
    private Label lblGeneralMention;    // Mention générale de l'étudiant
    @FXML
    private Label lblRank;              // Rang de l'étudiant dans sa classe
    @FXML
    private Label lblGeneralAppreciation; // Label pour l'appréciation générale

    // --- Labels pour les absences ---
    @FXML
    private Label lblTotalAbsences;
    @FXML
    private Label lblJustifiedAbsences;
    @FXML
    private Label lblUnjustifiedAbsences;
    @FXML
    private VBox vboxDetailedAbsences; // VBox pour afficher la liste des absences détaillées

    @FXML
    private Button btnReturn;           // Bouton de retour

    // --- ObservableList pour lier les données au TableView ---
    private ObservableList<BulletinDetailMatiere> matiereDetailsList = FXCollections.observableArrayList();

    // --- Instance de la classe de connexion à la base de données (Singleton) ---
    private DB db = DB.getInstance();

    // --- Constante pour le nom de l'établissement ---
    private static final String ETABLISSEMENT_NAME = "UNIVERSITE TRESOR SCHOOL";

    // Pour formater les moyennes à deux décimales
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.00");


    /**
     * Méthode d'initialisation du contrôleur. Appelée automatiquement par JavaFX.
     * Initialise les colonnes du TableView.
     */
    @FXML
    public void initialize() {
        System.out.println("DEBUG BulletinController: initialize() appelé.");
        // Liaison des colonnes du TableView aux propriétés de BulletinDetailMatiere
        colMatiereNom.setCellValueFactory(new PropertyValueFactory<>("matiereNom"));
        colMatiereCoeff.setCellValueFactory(new PropertyValueFactory<>("matiereCoefficient"));
        colNotesIndividuelles.setCellValueFactory(new PropertyValueFactory<>("notesIndividuelles"));
        colMoyenneMatiere.setCellValueFactory(new PropertyValueFactory<>("moyenneMatiere"));
        colMentionMatiere.setCellValueFactory(new PropertyValueFactory<>("mentionMatiere"));

        tblMatiereDetails.setItems(matiereDetailsList);

        // Configuration spécifique pour la colonne des notes individuelles (List<String>)
        colNotesIndividuelles.setCellFactory(tc -> new TableCell<BulletinDetailMatiere, List<String>>() {
            @Override
            protected void updateItem(List<String> notes, boolean empty) {
                super.updateItem(notes, empty);
                if (empty || notes == null || notes.isEmpty()) {
                    setText(null);
                } else {
                    setText(String.join("\n", notes)); // Joint toutes les notes formatées avec une virgule
                }
            }
        });
        System.out.println("DEBUG BulletinController: initialize() terminé. TableView configuré.");
    }

    /**
     * Méthode principale pour afficher le bulletin d'un étudiant donné.
     * Cette méthode doit être appelée par le contrôleur précédent (ex: EtudiantController, DeliberationController)
     * pour passer l'ID de l'étudiant sélectionné.
     *
     * @param etudiantId L'ID de l'étudiant pour lequel générer le bulletin.
     */
    public void showBulletin(int etudiantId) {
        System.out.println("DEBUG BulletinController: showBulletin() appelé pour etudiantId = " + etudiantId + " (lancement d'un thread de données).");
        new Thread(() -> { // Démarre un nouveau thread pour les opérations de base de données
            BulletinEtudiant bulletin = generateBulletinData(etudiantId);
            System.out.println("DEBUG BulletinController: generateBulletinData() terminé. Bulletin est null ? " + (bulletin == null));

            Platform.runLater(() -> { // Revient sur le JavaFX Application Thread pour mettre à jour l'UI
                if (bulletin != null) {
                    displayBulletin(bulletin);
                    System.out.println("DEBUG BulletinController: displayBulletin() appelé et terminé.");
                } else {
                    showAlert(AlertType.ERROR, "Erreur", "Impossible de générer le bulletin pour l'étudiant ID: " + etudiantId + ". Veuillez vérifier les données.");
                }
            });
        }).start(); // Démarre le thread
    }

    /**
     * Génère les données complètes du bulletin pour un étudiant.
     * Cette méthode interagit avec la base de données et effectue tous les calculs (moyennes, rang, absences, appréciation).
     *
     * @param etudiantId L'ID de l'étudiant.
     * @return Un objet BulletinEtudiant rempli avec toutes les données, ou null si une erreur survient ou si l'étudiant n'est pas trouvé.
     */
    BulletinEtudiant generateBulletinData(int etudiantId) {
        System.out.println("DEBUG BulletinController: generateBulletinData() - Début pour etudiantId: " + etudiantId);
        Connection conn = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        BulletinEtudiant bulletin = null;

        try {
            conn = db.getConnection(); // Obtient la connexion à la base de données
            if (conn == null || conn.isClosed()) {
                System.err.println("ERREUR DEBUG BulletinController: Connexion à la BD invalide pour générer le bulletin.");
                Platform.runLater(() -> showAlert(AlertType.ERROR, "Erreur Connexion BD", "La connexion à la base de données est invalide pour générer le bulletin."));
                return null;
            }
            System.out.println("DEBUG BulletinController: Connexion à la BD établie.");

            // --- Étape 1: Récupérer les informations de l'étudiant et de sa classe ---
            String studentInfoSql = "SELECT e.nom, e.prenom, c.nom_classe, c.annee_scolaire " +
                    "FROM Etudiants e JOIN Classes c ON e.classe_id = c.id " +
                    "WHERE e.id = ?";
            pstm = conn.prepareStatement(studentInfoSql);
            pstm.setInt(1, etudiantId);
            System.out.println("DEBUG BulletinController: Exécution SQL (Student Info): " + studentInfoSql + " avec ID=" + etudiantId);
            rs = pstm.executeQuery();

            if (rs.next()) {
                String nomEtudiant = rs.getString("nom") + " " + rs.getString("prenom");
                String classeEtudiant = rs.getString("nom_classe");
                String anneeScolaire = rs.getString("annee_scolaire");
                // Crée l'objet BulletinEtudiant avec les informations de base
                bulletin = new BulletinEtudiant(ETABLISSEMENT_NAME, nomEtudiant, classeEtudiant, anneeScolaire, etudiantId);
                System.out.println("DEBUG BulletinController: Informations étudiant récupérées: " + nomEtudiant + ", Classe: " + classeEtudiant + ", Année: " + anneeScolaire);
            } else {
                Platform.runLater(() -> showAlert(AlertType.WARNING, "Étudiant introuvable", "Aucun étudiant trouvé avec l'ID: " + etudiantId + " dans la base de données."));
                System.out.println("DEBUG BulletinController: Aucun étudiant trouvé avec l'ID: " + etudiantId);
                return null;
            }
            closeResources(rs, pstm, null);
            rs = null;
            pstm = null;

            // --- Étape 2: Récupérer toutes les notes individuelles de l'étudiant avec les détails des matières ---
            Map<Integer, BulletinDetailMatiere> matiereDetailsMap = new LinkedHashMap<>();
            Map<Integer, Double> sumWeightedNotesByMatiere = new HashMap<>();
            Map<Integer, Double> sumCoeffNotesByMatiere = new HashMap<>();

            String notesDetailsSql = "SELECT n.valeur_note, n.type_evaluation, n.coeff_note, " +
                    "m.id AS matiere_id, m.nom_matiere, m.coefficient AS matiere_global_coefficient " +
                    "FROM Notes n JOIN Matieres m ON n.matiere_id = m.id " +
                    "WHERE n.etudiant_id = ?";
            pstm = conn.prepareStatement(notesDetailsSql);
            pstm.setInt(1, etudiantId);
            System.out.println("DEBUG BulletinController: Exécution SQL (Notes Details): " + notesDetailsSql + " avec ID=" + etudiantId);
            rs = pstm.executeQuery();

            int notesCount = 0;
            while (rs.next()) {
                notesCount++;
                int matiereId = rs.getInt("matiere_id");
                String matiereNom = rs.getString("nom_matiere");
                double matiereGlobalCoeff = rs.getDouble("matiere_global_coefficient");
                double valeurNote = rs.getDouble("valeur_note");
                String typeEvaluation = rs.getString("type_evaluation");
                double coeffNote = rs.getDouble("coeff_note");

                if (!matiereDetailsMap.containsKey(matiereId)) {
                    matiereDetailsMap.put(matiereId, new BulletinDetailMatiere(matiereNom, matiereGlobalCoeff, 0.0, "N/A"));
                }
                matiereDetailsMap.get(matiereId).addNoteIndividuelle(
                        DECIMAL_FORMAT.format(valeurNote) + "/20 (" + typeEvaluation + " - Coeff " + DECIMAL_FORMAT.format(coeffNote) + ")"
                );

                sumWeightedNotesByMatiere.put(matiereId, sumWeightedNotesByMatiere.getOrDefault(matiereId, 0.0) + (valeurNote * coeffNote));
                sumCoeffNotesByMatiere.put(matiereId, sumCoeffNotesByMatiere.getOrDefault(matiereId, 0.0) + coeffNote);
            }
            System.out.println("DEBUG BulletinController: " + notesCount + " notes individuelles récupérées pour l'étudiant ID: " + etudiantId);
            closeResources(rs, pstm, null);
            rs = null;
            pstm = null;

            // --- Étape 3: Calculer la moyenne par matière et la moyenne générale ---
            double totalWeightedGeneralAverage = 0.0;
            double totalGeneralCoefficient = 0.0;

            for (Map.Entry<Integer, BulletinDetailMatiere> entry : matiereDetailsMap.entrySet()) {
                int matiereId = entry.getKey();
                BulletinDetailMatiere detail = entry.getValue();

                double sumCoeff = sumCoeffNotesByMatiere.getOrDefault(matiereId, 0.0);
                if (sumCoeff > 0) {
                    double moyenneMatiere = sumWeightedNotesByMatiere.get(matiereId) / sumCoeff;
                    detail.setMoyenneMatiere(moyenneMatiere);
                    detail.setMentionMatiere(getMention(moyenneMatiere));

                    totalWeightedGeneralAverage += (moyenneMatiere * detail.getMatiereCoefficient());
                    totalGeneralCoefficient += detail.getMatiereCoefficient();
                    System.out.println("DEBUG BulletinController: Matière " + detail.getMatiereNom() + ": Moyenne=" + DECIMAL_FORMAT.format(moyenneMatiere) + ", Coeff global=" + detail.getMatiereCoefficient());
                } else {
                    detail.setMoyenneMatiere(0.0);
                    detail.setMentionMatiere("N/A (Pas de notes)");
                    System.out.println("DEBUG BulletinController: Matière " + detail.getMatiereNom() + ": Aucune note valide trouvée.");
                }
                bulletin.addDetailMatiere(detail);
            }
            System.out.println("DEBUG BulletinController: Nombre de matières avec détails: " + bulletin.getDetailsMatieres().size());

            if (totalGeneralCoefficient > 0) {
                double moyenneGenerale = totalWeightedGeneralAverage / totalGeneralCoefficient;
                bulletin.setMoyenneGenerale(moyenneGenerale);
                bulletin.setMentionGenerale(getMention(moyenneGenerale));
                bulletin.setAppreciationGenerale(getAppreciation(moyenneGenerale)); // Définit l'appréciation générale
                System.out.println("DEBUG BulletinController: Moyenne générale calculée: " + DECIMAL_FORMAT.format(moyenneGenerale));
            } else {
                bulletin.setMoyenneGenerale(0.0);
                bulletin.setMentionGenerale("Aucune note globale");
                bulletin.setAppreciationGenerale("Aucune appréciation (pas de notes globales)");
                System.out.println("DEBUG BulletinController: Aucune moyenne générale calculable (totalGeneralCoefficient est 0).");
            }

            // --- Étape 4: Calculer le rang de l'étudiant dans sa classe ---
            System.out.println("DEBUG BulletinController: Démarrage du calcul du rang pour la classe de l'étudiant ID: " + etudiantId);
            List<Map.Entry<Integer, Double>> classAverages = new ArrayList<>();

            String classAveragesSql = "SELECT n.etudiant_id, SUM(n.valeur_note * n.coeff_note) / SUM(n.coeff_note) AS moyenne_ponderee_matiere_temp, m.coefficient AS matiere_global_coefficient " +
                    "FROM Notes n JOIN Matieres m ON n.matiere_id = m.id JOIN Etudiants e ON n.etudiant_id = e.id " +
                    "WHERE e.classe_id = (SELECT classe_id FROM Etudiants WHERE id = ?) " +
                    "GROUP BY n.etudiant_id, m.id, m.coefficient";

            pstm = conn.prepareStatement(classAveragesSql);
            pstm.setInt(1, etudiantId);
            System.out.println("DEBUG BulletinController: Exécution SQL (Class Averages for Rank): " + classAveragesSql + " avec ID=" + etudiantId);
            rs = pstm.executeQuery();

            Map<Integer, Double> studentOverallWeightedSum = new HashMap<>();
            Map<Integer, Double> studentOverallCoeffSum = new HashMap<>();

            int classNotesCount = 0;
            while(rs.next()) {
                classNotesCount++;
                int currentStudentId = rs.getInt("etudiant_id");
                double moyenneMatiereTemp = rs.getDouble("moyenne_ponderee_matiere_temp");
                double matiereGlobalCoeff = rs.getDouble("matiere_global_coefficient");

                studentOverallWeightedSum.put(currentStudentId, studentOverallWeightedSum.getOrDefault(currentStudentId, 0.0) + (moyenneMatiereTemp * matiereGlobalCoeff));
                studentOverallCoeffSum.put(currentStudentId, studentOverallCoeffSum.getOrDefault(currentStudentId, 0.0) + matiereGlobalCoeff);
            }
            System.out.println("DEBUG BulletinController: " + classNotesCount + " lignes de notes de classe récupérées pour le calcul du rang.");
            closeResources(rs, pstm, null);
            rs = null;
            pstm = null;

            for (Map.Entry<Integer, Double> entry : studentOverallWeightedSum.entrySet()) {
                int currentStudentId = entry.getKey();
                double overallCoeff = studentOverallCoeffSum.getOrDefault(currentStudentId, 0.0);
                if (overallCoeff > 0) {
                    classAverages.add(Map.entry(currentStudentId, entry.getValue() / overallCoeff));
                    System.out.println("DEBUG BulletinController: Moyenne générale calculée pour l'étudiant ID " + currentStudentId + ": " + DECIMAL_FORMAT.format(entry.getValue() / overallCoeff));
                } else {
                    classAverages.add(Map.entry(currentStudentId, 0.0));
                    System.out.println("DEBUG BulletinController: Moyenne générale 0 pour l'étudiant ID " + currentStudentId + " (pas de notes valides).");
                }
            }
            System.out.println("DEBUG BulletinController: " + classAverages.size() + " moyennes d'étudiants de la classe pour le rang.");

            classAverages.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
            System.out.println("DEBUG BulletinController: Moyennes de classe triées.");

            for (int i = 0; i < classAverages.size(); i++) {
                if (classAverages.get(i).getKey() == etudiantId) {
                    bulletin.setRang(i + 1);
                    System.out.println("DEBUG BulletinController: Rang de l'étudiant ID " + etudiantId + " trouvé: " + (i + 1));
                    break;
                }
            }

            // --- Étape 5: Récupérer les informations d'absences ---
            int totalAbs = 0;
            int justifiedAbs = 0;
            int unjustifiedAbs = 0;
            List<String> detailedAbs = new ArrayList<>();

            String absencesSql = "SELECT date_absence, type_absence, justification " +
                    "FROM Absences " +
                    "WHERE etudiant_id = ? ORDER BY date_absence";
            pstm = conn.prepareStatement(absencesSql);
            pstm.setInt(1, etudiantId);
            System.out.println("DEBUG BulletinController: Exécution SQL (Absences): " + absencesSql + " avec ID=" + etudiantId);
            rs = pstm.executeQuery();

            while (rs.next()) {
                totalAbs++;
                String dateAbs = rs.getDate("date_absence").toString();
                String typeAbsence = rs.getString("type_absence");
                String justification = rs.getString("justification");

                boolean isJustified = justification != null && !justification.trim().isEmpty() && !typeAbsence.toLowerCase().contains("non justifiée");

                if (isJustified) {
                    justifiedAbs++;
                } else {
                    unjustifiedAbs++;
                }
                detailedAbs.add("Date: " + dateAbs + ", Type: " + typeAbsence + ", Justifié: " + (isJustified ? "Oui" : "Non") + (justification != null && !justification.trim().isEmpty() ? " (" + justification + ")" : ""));
            }
            System.out.println("DEBUG BulletinController: " + totalAbs + " absences récupérées pour l'étudiant ID: " + etudiantId);
            bulletin.setTotalAbsences(totalAbs);
            bulletin.setJustifiedAbsences(justifiedAbs);
            bulletin.setUnjustifiedAbsences(unjustifiedAbs);
            bulletin.setDetailedAbsences(detailedAbs);

        } catch (SQLException e) {
            final String errorMessage = e.getMessage();
            Platform.runLater(() -> showAlert(AlertType.ERROR, "Erreur BD Bulletin", "Erreur lors de la génération du bulletin : " + errorMessage));
            e.printStackTrace();
            System.err.println("SQL Exception lors de generateBulletinData: " + e.getMessage());
            return null;
        } finally {
            closeResources(rs, pstm, conn);
            System.out.println("DEBUG BulletinController: generateBulletinData() - Fin.");
        }
        return bulletin;
    }

    /**
     * Affiche les données de l'objet BulletinEtudiant dans l'interface utilisateur.
     * Cette méthode DOIT être appelée sur le JavaFX Application Thread.
     * @param bulletin L'objet BulletinEtudiant à afficher.
     */
    private void displayBulletin(BulletinEtudiant bulletin) {
        System.out.println("DEBUG BulletinController: displayBulletin() - Début de mise à jour UI.");
        lblEtablissementName.setText(bulletin.getNomEtablissement());
        lblAcademicYear.setText("Année Scolaire : " + bulletin.getAnneeScolaire());
        lblStudentName.setText(bulletin.getNomEtudiant());
        lblStudentClass.setText(bulletin.getClasseEtudiant());

        System.out.println("DEBUG BulletinController: UI Labels (infos étudiant) mis à jour.");
        System.out.println("DEBUG BulletinController: Nombre de matières dans le bulletin: " + bulletin.getDetailsMatieres().size());
        matiereDetailsList.setAll(bulletin.getDetailsMatieres());
        System.out.println("DEBUG BulletinController: TableView (détails matières) mis à jour.");

        lblGeneralAverage.setText(DECIMAL_FORMAT.format(bulletin.getMoyenneGenerale()));
        lblGeneralMention.setText(bulletin.getMentionGenerale());
        lblRank.setText(String.valueOf(bulletin.getRang()));
        lblGeneralAppreciation.setText(bulletin.getAppreciationGenerale()); // Met à jour le label d'appréciation
        System.out.println("DEBUG BulletinController: Résumé général UI mis à jour.");

        lblTotalAbsences.setText(String.valueOf(bulletin.getTotalAbsences()));
        lblJustifiedAbsences.setText(String.valueOf(bulletin.getJustifiedAbsences()));
        lblUnjustifiedAbsences.setText(String.valueOf(bulletin.getUnjustifiedAbsences()));
        System.out.println("DEBUG BulletinController: Labels d'absences mis à jour.");

        vboxDetailedAbsences.getChildren().clear();
        if (bulletin.getDetailedAbsences().isEmpty()) {
            Label noAbsenceLabel = new Label("Aucune absence enregistrée.");
            noAbsenceLabel.setFont(new Font(12.0));
            vboxDetailedAbsences.getChildren().add(noAbsenceLabel);
        } else {
            for (String absenceDetail : bulletin.getDetailedAbsences()) {
                Label detailLabel = new Label("• " + absenceDetail);
                detailLabel.setFont(new Font(12.0));
                vboxDetailedAbsences.getChildren().add(detailLabel);
            }
        }
        System.out.println("DEBUG BulletinController: Détails des absences dans VBox mis à jour.");

        System.out.println("DEBUG BulletinController: displayBulletin() - Fin de mise à jour UI.");
    }

    /**
     * Méthode utilitaire pour attribuer une mention académique basée sur la moyenne.
     * @param moyenne La moyenne numérique à évaluer.
     * @return La mention textuelle correspondante.
     */
    private String getMention(double moyenne) {
        if (moyenne >= 16.0) return "Très Bien";
        if (moyenne >= 14.0) return "Bien";
        if (moyenne >= 12.0) return "Assez Bien";
        if (moyenne >= 10.0) return "Admis";
        return "Refusé";
    }

    /**
     * Méthode utilitaire pour attribuer une appréciation générale basée sur la moyenne.
     * @param moyenne La moyenne numérique à évaluer.
     * @return L'appréciation textuelle correspondante.
     */
    private String getAppreciation(double moyenne) {
        if (moyenne >= 18.0) return "Excellent travail, félicitations !";
        if (moyenne >= 16.0) return "Très bon niveau, continuez ainsi.";
        if (moyenne >= 14.0) return "Bon travail, encourageant.";
        if (moyenne >= 12.0) return "Travail satisfaisant, peut mieux faire.";
        if (moyenne >= 10.0) return "Niveau juste suffisant, des efforts sont nécessaires.";
        return "Des difficultés importantes, un soutien est recommandé.";
    }

    /**
     * Gère l'action du bouton "Retour".
     * Permet de naviguer vers la page appropriée selon le rôle de l'utilisateur.
     * @param event L'événement d'action.
     */
    @FXML
    private void handleReturn(ActionEvent event) {
        try {
            if (SessionManager.getInstance().isEtudiant()) {
                // Si l'utilisateur est un étudiant, retourner au tableau de bord étudiant
                Outils.load(event, "PROJET_6 - Tableau de Bord Étudiant", "/com/example/projet_semestre6/StudentDashboard.fxml");
            } else {
                // Sinon (par exemple, administrateur), retourner à la page d'accueil de l'administrateur
                Outils.load(event, "PROJET_6 - Application Principale", "/com/example/projet_semestre6/AccueilController.fxml");
            }
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Erreur de Navigation", "Impossible de revenir à la page précédente : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Méthode utilitaire pour afficher des boîtes de dialogue d'alerte.
     * Utilise Platform.runLater pour s'assurer que l'alerte est affichée sur le thread JavaFX.
     * @param type Le type d'alerte (INFORMATION, WARNING, ERROR, etc.).
     * @param title Le titre de la boîte de dialogue.
     * @param content Le contenu (message) de l'alerte.
     */
    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        Platform.runLater(() -> alert.showAndWait());
    }

    /**
     * Méthode utilitaire pour fermer les ressources JDBC (ResultSet, PreparedStatement).
     * La connexion n'est pas fermée ici si elle est gérée comme un singleton.
     * @param rs Le ResultSet à fermer (peut être null).
     * @param pstm Le PreparedStatement à fermer (peut être null).
     * @param conn La connexion (peut être null si déjà fermée ou gérée ailleurs comme un singleton).
     */
    private void closeResources(ResultSet rs, PreparedStatement pstm, Connection conn) {
        try {
            if (rs != null) rs.close();
            if (pstm != null) pstm.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
