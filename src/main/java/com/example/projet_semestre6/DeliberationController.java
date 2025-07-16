package com.example.projet_semestre6;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.application.Platform;
import javafx.util.StringConverter; // Import nécessaire pour StringConverter

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Contrôleur pour la page de gestion des délibérations.
 * Permet de calculer et d'afficher les moyennes pondérées par matière pour
 * un étudiant spécifique ou pour l'ensemble des étudiants d'une classe.
 */
public class DeliberationController {
    @FXML
    private Button btnbulletin; // Le fx:id du bouton BULLETIN dans Deliberation.fxml


    // --- Éléments FXML liés à l'interface utilisateur (définis dans Deliberation.fxml) ---
    @FXML
    private ComboBox<Etudiant> CBETUDIANT; // ComboBox pour sélectionner un étudiant
    @FXML
    private ComboBox<Classe> CBCLASSE;     // ComboBox pour sélectionner une classe

    @FXML
    private Button BTNCALCULER; // Bouton pour déclencher le calcul des résultats
    @FXML
    private Button BTNRETOUR;   // Bouton pour revenir à la page principale

    @FXML
    private TableView<DeliberationResult> TBLDELIBERATION; // Tableau pour afficher les résultats de délibération
    @FXML
    private TableColumn<DeliberationResult, String> CLMATIERE;     // Colonne pour le nom de la matière
    @FXML
    private TableColumn<DeliberationResult, Double> CLMOYENNE;     // Colonne pour la moyenne calculée
    @FXML
    private TableColumn<DeliberationResult, Double> CLCOEFFICIENT; // Colonne pour le coefficient global de la matière
    @FXML
    private TableColumn<DeliberationResult, String> CLMENTION;     // Colonne pour la mention attribuée

    // --- Listes observables pour stocker les données et les lier aux composants JavaFX ---
    private ObservableList<Etudiant> etudiants = FXCollections.observableArrayList();
    private ObservableList<Classe> classes = FXCollections.observableArrayList();
    private ObservableList<DeliberationResult> deliberationResults = FXCollections.observableArrayList();

    // --- Instance de la classe de connexion à la base de données (Singleton) ---
    private DB db = DB.getInstance();

    /**
     * Méthode d'initialisation du contrôleur. Appelé automatiquement par JavaFX
     * après le chargement du fichier FXML.
     * Configure le tableau et charge les données initiales pour les ComboBox.
     */
    @FXML
    public void initialize() {
        initTableView();
        initComboBoxes(); // Nouvelle méthode pour configurer les ComboBoxes
        loadEtudiants();
        loadClasses();

        CBETUDIANT.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("DEBUG DeliberationController: CBETUDIANT selection changed. Old: " + oldVal + ", New: " + newVal); // DEBUG
            if (newVal != null) {
                clearResultsAndClassSelection();
            }
        });

        CBCLASSE.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("DEBUG DeliberationController: CBCLASSE selection changed. Old: " + oldVal + ", New: " + newVal); // DEBUG
            if (newVal != null) {
                clearResultsAndStudentSelection();
            }
        });
    }

    /**
     * Configure les CellValueFactory pour chaque colonne de la TableView.
     */
    private void initTableView() {
        CLMATIERE.setCellValueFactory(new PropertyValueFactory<>("matiereNom"));
        CLMOYENNE.setCellValueFactory(new PropertyValueFactory<>("moyenneMatiere"));
        CLCOEFFICIENT.setCellValueFactory(new PropertyValueFactory<>("coefficientMatiere"));
        CLMENTION.setCellValueFactory(new PropertyValueFactory<>("mention"));

        TBLDELIBERATION.setItems(deliberationResults);
    }

    /**
     * Configure les ComboBoxes pour qu'elles affichent les noms des objets
     * (Etudiant.nom + Etudiant.prenom et Classe.nomClasse) correctement.
     */
    private void initComboBoxes() {
        // Configuration de la ComboBox des étudiants
        CBETUDIANT.setCellFactory(lv -> new ListCell<Etudiant>() {
            @Override
            protected void updateItem(Etudiant etudiant, boolean empty) {
                super.updateItem(etudiant, empty);
                setText(empty ? "" : etudiant.getNom() + " " + etudiant.getPrenom());
            }
        });

        CBETUDIANT.setButtonCell(new ListCell<Etudiant>() {
            @Override
            protected void updateItem(Etudiant etudiant, boolean empty) {
                super.updateItem(etudiant, empty);
                setText(empty ? "" : etudiant.getNom() + " " + etudiant.getPrenom());
            }
        });

        // Configuration de la ComboBox des classes
        CBCLASSE.setCellFactory(lv -> new ListCell<Classe>() {
            @Override
            protected void updateItem(Classe classe, boolean empty) { // Correction: void updateItem
                super.updateItem(classe, empty);
                setText(empty ? "" : classe.getNomClasse());
            }
        });

        CBCLASSE.setButtonCell(new ListCell<Classe>() {
            @Override
            protected void updateItem(Classe classe, boolean empty) {
                super.updateItem(classe, empty);
                setText(empty ? "" : classe.getNomClasse());
            }
        });

        // Liaison initiale des ObservableLists aux ComboBoxes (important pour qu'elles soient observées)
        CBETUDIANT.setItems(etudiants);
        CBCLASSE.setItems(classes);
    }


    /**
     * Charge la liste des étudiants depuis la base de données.
     * Les mises à jour de l'UI (ComboBox) sont effectuées via Platform.runLater()
     * pour garantir la sécurité des threads.
     */
    private void loadEtudiants() {
        System.out.println("DEBUG: Démarrage de loadEtudiants()...");
        Connection conn = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        List<Etudiant> fetchedEtudiants = new ArrayList<>();

        try {
            conn = db.getConnection();
            if (conn == null || conn.isClosed()) {
                System.err.println("ERREUR DEBUG: La connexion à la BD pour les étudiants est nulle ou fermée.");
                showAlert(AlertType.ERROR, "Erreur Connexion BD", "La connexion à la base de données est invalide pour charger les étudiants.");
                return;
            }
            System.out.println("DEBUG: Connexion à la BD pour les étudiants établie.");

            String sql = "SELECT id, nom, prenom FROM Etudiants";
            pstm = conn.prepareStatement(sql);
            rs = pstm.executeQuery();
            System.out.println("DEBUG: Requête SQL pour les étudiants exécutée: " + sql);

            if (!rs.isBeforeFirst()) {
                System.out.println("DEBUG: Le ResultSet des étudiants est vide. Aucune donnée n'a été récupérée.");
            }

            int count = 0;
            while (rs.next()) {
                // Utilise le constructeur simplifié d'Etudiant pour la ComboBox
                fetchedEtudiants.add(new Etudiant(rs.getInt("id"), rs.getString("nom"), rs.getString("prenom"), null, null, null, null, 0, null, null));
                count++;
            }
            System.out.println("DEBUG: " + count + " étudiants ont été récupérés dans la liste temporaire.");

            Platform.runLater(() -> {
                System.out.println("DEBUG (Platform.runLater): Tentative de mise à jour de la ComboBox des étudiants.");
                CBETUDIANT.getSelectionModel().clearSelection();
                etudiants.setAll(fetchedEtudiants);
                CBETUDIANT.setItems(etudiants); // Décommenté
                System.out.println("DEBUG (Platform.runLater): ObservableList 'etudiants' mise à jour avec " + etudiants.size() + " éléments. ComboBox Set.");
            });

        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Erreur Base de Données", "Impossible de charger les étudiants : " + e.getMessage());
            e.printStackTrace();
            System.err.println("ERREUR SQL lors du chargement des étudiants: " + e.getMessage());
        } finally {
            closeResources(null, pstm, conn); // Modified: conn should not be closed if it's a singleton
            System.out.println("DEBUG: loadEtudiants() terminé.");
        }
    }

    /**
     * Charge la liste des classes depuis la base de données.
     * Les mises à jour de l'UI (ComboBox) sont effectuées via Platform.runLater()
     * pour garantir la sécurité des threads.
     */
    private void loadClasses() {
        System.out.println("DEBUG: Démarrage de loadClasses()...");
        Connection conn = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        List<Classe> fetchedClasses = new ArrayList<>();

        try {
            conn = db.getConnection();
            if (conn == null || conn.isClosed()) {
                System.err.println("ERREUR DEBUG: La connexion à la BD pour les classes est nulle ou fermée.");
                showAlert(AlertType.ERROR, "Erreur Connexion BD", "La connexion à la base de données est invalide pour charger les classes.");
                return;
            }
            System.out.println("DEBUG: Connexion à la BD pour les classes établie.");

            String sql = "SELECT id, nom_classe FROM Classes";
            pstm = conn.prepareStatement(sql);
            rs = pstm.executeQuery();
            System.out.println("DEBUG: Requête SQL pour les classes exécutée: " + sql);

            if (!rs.isBeforeFirst()) {
                System.out.println("DEBUG: Le ResultSet des classes est vide. Aucune donnée n'a été récupérée.");
            }

            int count = 0;
            while (rs.next()) {
                fetchedClasses.add(new Classe(rs.getInt("id"), rs.getString("nom_classe")));
                count++;
            }
            System.out.println("DEBUG: " + count + " classes ont été récupérées dans la liste temporaire.");

            Platform.runLater(() -> {
                System.out.println("DEBUG (Platform.runLater): Tentative de mise à jour de la ComboBox des classes.");
                CBCLASSE.getSelectionModel().clearSelection();
                classes.setAll(fetchedClasses);
                CBCLASSE.setItems(classes); // Décommenté
                System.out.println("DEBUG (Platform.runLater): ObservableList 'classes' mise à jour avec " + classes.size() + " éléments. ComboBox Set.");
            });

        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Erreur Base de Données", "Impossible de charger les classes : " + e.getMessage());
            e.printStackTrace();
            System.err.println("ERREUR SQL lors du chargement des classes: " + e.getMessage());
        } finally {
            closeResources(null, pstm, conn); // Modified: conn should not be closed if it's a singleton
            System.out.println("DEBUG: loadClasses() terminé.");
        }
    }

    /**
     * Gère l'action du bouton "Calculer".
     * Récupère les notes, calcule les moyennes pondérées par matière et
     * affiche les résultats dans la TableView.
     * Permet le calcul pour un étudiant spécifique ou pour tous les étudiants d'une classe.
     * @param event L'événement d'action (clic sur le bouton).
     */
    @FXML
    private void handleCalculer(ActionEvent event) {
        deliberationResults.clear();

        Etudiant selectedEtudiant = CBETUDIANT.getSelectionModel().getSelectedItem();
        Classe selectedClasse = CBCLASSE.getSelectionModel().getSelectedItem();

        if (selectedEtudiant == null && selectedClasse == null) {
            showAlert(AlertType.WARNING, "Sélection Requise", "Veuillez sélectionner un étudiant OU une classe pour le calcul.");
            return;
        }
        if (selectedEtudiant != null && selectedClasse != null) {
            showAlert(AlertType.INFORMATION, "Sélection Multiple", "Calcul des résultats pour l'étudiant sélectionné. La sélection de la classe sera ignorée.");
            selectedClasse = null;
        }

        Connection conn = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            conn = db.getConnection();
            String sql;

            if (selectedEtudiant != null) {
                sql = "SELECT m.nom_matiere, m.coefficient AS matiere_global_coefficient, " +
                        "SUM(n.valeur_note * n.coeff_note) / SUM(n.coeff_note) AS moyenne_ponderee_matiere " +
                        "FROM Notes n " +
                        "JOIN Matieres m ON n.matiere_id = m.id " +
                        "WHERE n.etudiant_id = ? " +
                        "GROUP BY m.id, m.nom_matiere, m.coefficient";
                pstm = conn.prepareStatement(sql);
                pstm.setInt(1, selectedEtudiant.getId());
                System.out.println("DEBUG SQL (Etudiant): " + sql + " avec etudiant_id=" + selectedEtudiant.getId());
                showAlert(AlertType.INFORMATION, "Résultats pour " + selectedEtudiant.getNom() + " " + selectedEtudiant.getPrenom(), "Affichage des moyennes par matière.");

            } else {
                sql = "SELECT e.nom, e.prenom, m.nom_matiere, m.coefficient AS matiere_global_coefficient, " +
                        "SUM(n.valeur_note * n.coeff_note) / SUM(n.coeff_note) AS moyenne_ponderee_matiere " +
                        "FROM Notes n " +
                        "JOIN Matieres m ON n.matiere_id = m.id " +
                        "JOIN Etudiants e ON n.etudiant_id = e.id " +
                        "WHERE e.classe_id = ? " +
                        "GROUP BY e.id, e.nom, e.prenom, m.id, m.nom_matiere, m.coefficient " +
                        "ORDER BY e.nom, e.prenom, m.nom_matiere";
                pstm = conn.prepareStatement(sql);
                pstm.setInt(1, selectedClasse.getId());
                System.out.println("DEBUG SQL (Classe): " + sql + " avec classe_id=" + selectedClasse.getId());
                showAlert(AlertType.INFORMATION, "Résultats pour la classe " + selectedClasse.getNomClasse(), "Affichage des moyennes par matière pour chaque étudiant.");
            }

            rs = pstm.executeQuery();

            if (!rs.isBeforeFirst()) {
                showAlert(AlertType.INFORMATION, "Aucune Note", "Aucune note trouvée pour la sélection actuelle.");
                System.out.println("DEBUG: Le ResultSet est vide. Aucune note à afficher.");
                return;
            }
            System.out.println("DEBUG: Des notes ont été trouvées. Traitement des résultats...");

            while (rs.next()) {
                String matiereNom = rs.getString("nom_matiere");
                double moyenne = rs.getDouble("moyenne_ponderee_matiere");
                double coeffGlobalMatiere = rs.getDouble("matiere_global_coefficient");
                String mention = getMention(moyenne);

                if (selectedClasse != null && selectedEtudiant == null) {
                    String etudiantNom = rs.getString("nom");
                    String etudiantPrenom = rs.getString("prenom");
                    matiereNom = etudiantNom + " " + etudiantPrenom + " - " + matiereNom;
                }

                DeliberationResult result = new DeliberationResult(matiereNom, moyenne, coeffGlobalMatiere, mention);
                deliberationResults.add(result);
                System.out.println("DEBUG: Ajouté au tableau -> " + result.toString());
            }
            System.out.println("DEBUG: Nombre total de résultats de délibération ajoutés : " + deliberationResults.size());

        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Erreur Base de Données", "Erreur lors du calcul des délibérations : " + e.getMessage());
            e.printStackTrace();
            System.err.println("SQL Exception: " + e.getMessage());
        } finally {
            closeResources(null, pstm, conn); // Modified: conn should not be closed if it's a singleton
        }
    }

    /**
     * Gère l'action du bouton "BULLETIN".
     * Ouvre la page du bulletin pour l'étudiant actuellement sélectionné dans la ComboBox.
     * @param event L'événement d'action (clic sur le bouton).
     */
    @FXML
    public void bulletin(ActionEvent event) { // Renommé le paramètre de 'e' à 'event' pour la cohérence
        Etudiant selectedEtudiant = CBETUDIANT.getSelectionModel().getSelectedItem();
        System.out.println("DEBUG DeliberationController: selectedEtudiant from ComboBox (on button click): " + selectedEtudiant); // AJOUT DE DÉBOGAGE
        if (selectedEtudiant != null) {
            try {
                // Utilise la méthode loadBulletinPage de Outils pour passer l'ID de l'étudiant
                Outils.loadBulletinPage(event, "Bulletin de Notes de " + selectedEtudiant.getNom() + " " + selectedEtudiant.getPrenom(),
                        "/com/example/projet_semestre6/Bulletin.fxml",
                        selectedEtudiant.getId());
            } catch (IOException e) {
                showAlert(AlertType.ERROR, "Erreur de Navigation", "Impossible d'ouvrir le bulletin : " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            showAlert(AlertType.WARNING, "Sélection requise", "Veuillez sélectionner un étudiant pour voir son bulletin détaillé.");
        }
    }


    private String getMention(double moyenne) {
        if (moyenne >= 16) return "Très Bien";
        if (moyenne >= 14) return "Bien";
        if (moyenne >= 12) return "Assez Bien";
        if (moyenne >= 10) return "Admis";
        return "Refusé";
    }

    private void clearResultsAndClassSelection() {
        deliberationResults.clear();
        CBCLASSE.getSelectionModel().clearSelection();
    }

    private void clearResultsAndStudentSelection() {
        deliberationResults.clear();
        CBETUDIANT.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleRetour(ActionEvent event) {
        try {
            Outils.load(event, "PROJET_6 - Application Principale", "/com/example/projet_semestre6/AccueilController.fxml");
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Erreur de Navigation", "Impossible de revenir à la page principale : " + e.getMessage());
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
            // Ne fermez la connexion que si elle n'est pas un singleton et qu'elle a été ouverte spécifiquement ici.
            // Si DB.getConnection() renvoie toujours la même instance, ne pas fermer ici.
            // if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
