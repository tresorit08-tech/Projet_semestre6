package com.example.projet_semestre6;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter; // Pour formater les dates (si besoin, mais LocalDate le gère bien)

public class EtudiantController {
    @FXML
    private Button btnretour;

    @FXML
    private TableView<Etudiant> tbletudiant;

    @FXML
    private TableColumn<Etudiant, Integer> clid;

    @FXML
    private TableColumn<Etudiant, String> clnom;

    @FXML
    private TableColumn<Etudiant, String> clprenom;

    @FXML
    private TableColumn<Etudiant, LocalDate> cldatenaissance; // Type changé pour LocalDate

    @FXML
    private TableColumn<Etudiant, String> cladresse;

    @FXML
    private TableColumn<Etudiant, String> cltelephone;

    @FXML
    private TableColumn<Etudiant, String> clemail;

    @FXML
    private TableColumn<Etudiant, String> clclasse; // Affichera le nom de la classe (propriété classeNom)

    @FXML
    private TableColumn<Etudiant, String> climage; // Contient le chemin de l'image

    @FXML
    private Button btnajouter; // Bouton AJOUTER (assurez-vous de l'avoir dans le FXML)

    @FXML
    private Button btnmodifier;

    @FXML
    private Button btnsupprimer;

    @FXML
    private TextField txtrecherche;

    @FXML
    private TextField txtnom;

    @FXML
    private TextField txtprenom;

    @FXML
    private DatePicker txtdatenaissance; // Type changé de TextField à DatePicker

    @FXML
    private TextField txtadresse;

    @FXML
    private TextField txttelephone;

    @FXML
    private TextField txtemail;

    @FXML
    private ComboBox<Classe> comboxeclasse; // Le type de la ComboBox doit être Classe

    @FXML
    private Button btnimage;

    @FXML
    private ImageView imageViewEtudiant; // Pour afficher l'image de l'étudiant sélectionné

    private ObservableList<Etudiant> etudiants = FXCollections.observableArrayList();
    private ObservableList<Classe> classes = FXCollections.observableArrayList(); // Liste des classes
    private DB db = DB.getInstance();
    private String selectedImagePath; // Stocke le chemin de l'image sélectionnée ou du champ image existant

    @FXML
    public void initialize() {
        initTableView();
        loadClasses(); // Charge les classes avant les étudiants pour que la ComboBox soit remplie
        loadEtudiants();
        setupTableViewSelectionListener(); // Configure l'écouteur de sélection de TableView
    }

    private void initTableView() {
        // Associe les colonnes de la TableView aux propriétés (getters) de la classe Etudiant
        // Les noms ici doivent correspondre aux noms des propriétés (sans "get")
        clid.setCellValueFactory(new PropertyValueFactory<>("id"));
        clnom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        clprenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        cldatenaissance.setCellValueFactory(new PropertyValueFactory<>("dateNaissance")); // Correspond à getDateNaissance() (LocalDate)
        cladresse.setCellValueFactory(new PropertyValueFactory<>("adresse"));
        cltelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        clemail.setCellValueFactory(new PropertyValueFactory<>("email"));
        clclasse.setCellValueFactory(new PropertyValueFactory<>("classeNom")); // Correspond à getClasseNom() pour l'affichage
        climage.setCellValueFactory(new PropertyValueFactory<>("imagePath")); // Correspond à getImagePath()

        tbletudiant.setItems(etudiants);
    }

    private void loadEtudiants() {
        etudiants.clear(); // Vide la liste actuelle
        Connection conn = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            conn = db.getConnection();
            // Requête SQL pour récupérer les étudiants avec le NOM de leur classe
            String sql = "SELECT e.id, e.nom, e.prenom, e.date_naissance, e.adresse, e.telephone, e.email, e.classe_id, c.nom_classe, e.image_path " +
                    "FROM Etudiants e LEFT JOIN Classes c ON e.classe_id = c.id"; // Jointure pour obtenir le nom de la classe
            pstm = conn.prepareStatement(sql);
            rs = pstm.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String nom = rs.getString("nom");
                String prenom = rs.getString("prenom");
                // Convertir String de la DB en LocalDate
                LocalDate dateNaissance = (rs.getString("date_naissance") != null) ? LocalDate.parse(rs.getString("date_naissance")) : null;
                String adresse = rs.getString("adresse");
                String telephone = rs.getString("telephone");
                String email = rs.getString("email");
                int classeId = rs.getInt("classe_id");
                String classeNom = rs.getString("nom_classe"); // Récupère le nom de la classe pour l'affichage
                String imagePath = rs.getString("image_path");

                etudiants.add(new Etudiant(id, nom, prenom, dateNaissance, adresse, telephone, email, classeId, classeNom, imagePath));
            }
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Erreur Base de Données", "Impossible de charger les étudiants : " + e.getMessage());
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

        System.out.println("Début du chargement des classes..."); // Debug print 1

        try {
            conn = db.getConnection();
            String sql = "SELECT id, nom_classe FROM Classes"; // Récupère l'ID et le nom de la classe
            pstm = conn.prepareStatement(sql);
            rs = pstm.executeQuery();

            while (rs.next()) {
                classes.add(new Classe(rs.getInt("id"), rs.getString("nom_classe")));
            }
            System.out.println("Fin du chargement des classes. " + classes.size() + " classes chargées."); // Debug print 3
            comboxeclasse.setItems(classes);
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Erreur Base de Données", "Impossible de charger les classes : " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(rs, pstm, conn);
        }
    }

    private void setupTableViewSelectionListener() {
        // Ajoute un écouteur de sélection au TableView
        tbletudiant.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                // Remplir les champs de saisie avec les données de l'étudiant sélectionné
                txtnom.setText(newSelection.getNom());
                txtprenom.setText(newSelection.getPrenom());
                txtdatenaissance.setValue(newSelection.getDateNaissance()); // Pour DatePicker
                txtadresse.setText(newSelection.getAdresse());
                txttelephone.setText(newSelection.getTelephone());
                txtemail.setText(newSelection.getEmail());

                // Sélectionner la classe correcte dans la ComboBox
                if (newSelection.getClasseId() > 0) { // S'il y a un ID de classe valide
                    Classe selectedClassInComboBox = classes.stream()
                            .filter(c -> c.getId() == newSelection.getClasseId()) // Trouver la classe par ID
                            .findFirst()
                            .orElse(null);
                    comboxeclasse.getSelectionModel().select(selectedClassInComboBox);
                } else {
                    comboxeclasse.getSelectionModel().clearSelection(); // Aucune classe assignée
                }

                // Afficher l'image de l'étudiant
                selectedImagePath = newSelection.getImagePath(); // Met à jour le chemin de l'image sélectionnée
                if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
                    File file = new File(selectedImagePath);
                    if (file.exists()) {
                        imageViewEtudiant.setImage(new Image(file.toURI().toString()));
                    } else {
                        imageViewEtudiant.setImage(null); // Efface l'image si le fichier n'existe pas
                        showAlert(AlertType.WARNING, "Image non trouvée", "Le fichier image spécifié n'existe pas : " + selectedImagePath);
                    }
                } else {
                    imageViewEtudiant.setImage(null); // Efface l'image s'il n'y a pas de chemin
                }
            } else {
                clearFields(); // Effacer les champs si aucune sélection n'est active
            }
        });
    }

    @FXML
    private void handleAjouter(ActionEvent event) {
        // Vérifier si un étudiant est sélectionné, si oui, forcer la désélection pour un ajout
        if (tbletudiant.getSelectionModel().getSelectedItem() != null) {
            showAlert(AlertType.INFORMATION, "Action", "Veuillez d'abord désélectionner l'étudiant dans le tableau pour ajouter un nouveau.");
            clearFields(); // Vider les champs et désélectionner pour préparer l'ajout
            return;
        }
        saveEtudiant(null); // Appelle la méthode d'enregistrement pour un nouvel étudiant
    }

    @FXML
    private void handleModifier(ActionEvent event) {
        Etudiant selectedEtudiant = tbletudiant.getSelectionModel().getSelectedItem();
        if (selectedEtudiant != null) {
            saveEtudiant(selectedEtudiant); // Appelle la méthode d'enregistrement pour modifier l'étudiant sélectionné
        } else {
            showAlert(AlertType.WARNING, "Attention", "Veuillez sélectionner un étudiant à modifier.");
        }
    }

    @FXML
    private void saveEtudiant(Etudiant etudiantToModify) {
        String nom = txtnom.getText();
        String prenom = txtprenom.getText();
        LocalDate dateNaissance = txtdatenaissance.getValue(); // Récupère la valeur du DatePicker
        String adresse = txtadresse.getText();
        String telephone = txttelephone.getText();
        String email = txtemail.getText();
        Classe selectedClass = comboxeclasse.getSelectionModel().getSelectedItem(); // Récupère l'objet Classe sélectionné

        // Validation des champs obligatoires
        if (nom.isEmpty() || prenom.isEmpty() || dateNaissance == null || selectedClass == null) {
            showAlert(AlertType.WARNING, "Champs Manquants", "Veuillez remplir tous les champs obligatoires : Nom, Prénom, Date de Naissance, et Classe.");
            return;
        }

        Connection conn = null;
        PreparedStatement pstm = null;

        try {
            conn = db.getConnection();
            String sql;
            if (etudiantToModify == null) { // Mode Ajout (nouvel étudiant)
                sql = "INSERT INTO Etudiants (nom, prenom, date_naissance, adresse, telephone, email, classe_id, image_path) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                pstm = conn.prepareStatement(sql);
                // Définir les paramètres
                pstm.setString(1, nom);
                pstm.setString(2, prenom);
                pstm.setString(3, dateNaissance.toString()); // Convertir LocalDate en String pour la DB (YYYY-MM-DD)
                pstm.setString(4, adresse);
                pstm.setString(5, telephone);
                pstm.setString(6, email);
                pstm.setInt(7, selectedClass.getId()); // Utilise l'ID de la classe
                pstm.setString(8, selectedImagePath); // Chemin de l'image
                showAlert(AlertType.INFORMATION, "Succès", "Étudiant ajouté avec succès !");

            } else { // Mode Modification (étudiant existant)
                sql = "UPDATE Etudiants SET nom = ?, prenom = ?, date_naissance = ?, adresse = ?, telephone = ?, email = ?, classe_id = ?, image_path = ? WHERE id = ?";
                pstm = conn.prepareStatement(sql);
                // Définir les paramètres pour la mise à jour
                pstm.setString(1, nom);
                pstm.setString(2, prenom);
                pstm.setString(3, dateNaissance.toString());
                pstm.setString(4, adresse);
                pstm.setString(5, telephone);
                pstm.setString(6, email);
                pstm.setInt(7, selectedClass.getId());
                pstm.setString(8, selectedImagePath);
                pstm.setInt(9, etudiantToModify.getId()); // ID de l'étudiant à modifier
                showAlert(AlertType.INFORMATION, "Succès", "Étudiant modifié avec succès !");
            }

            pstm.executeUpdate();
            loadEtudiants(); // Recharger les données dans la TableView pour refléter les changements
            clearFields(); // Vider les champs après l'opération réussie

        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Erreur Base de Données", "Erreur lors de l'enregistrement de l'étudiant : " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(null, pstm, conn);
        }
    }

    @FXML
    private void handleSupprimer(ActionEvent event) {
        Etudiant selectedEtudiant = tbletudiant.getSelectionModel().getSelectedItem();
        if (selectedEtudiant == null) {
            showAlert(AlertType.WARNING, "Attention", "Veuillez sélectionner un étudiant à supprimer.");
            return;
        }

        // Demande de confirmation avant suppression
        Alert confirmAlert = new Alert(AlertType.CONFIRMATION, "Voulez-vous vraiment supprimer l'étudiant " + selectedEtudiant.getNom() + " " + selectedEtudiant.getPrenom() + " ?", ButtonType.YES, ButtonType.NO);
        confirmAlert.setTitle("Confirmation de Suppression");
        confirmAlert.setHeaderText(null);
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                Connection conn = null;
                PreparedStatement pstm = null;
                try {
                    conn = db.getConnection();
                    String sql = "DELETE FROM Etudiants WHERE id = ?";
                    pstm = conn.prepareStatement(sql);
                    pstm.setInt(1, selectedEtudiant.getId());
                    pstm.executeUpdate();
                    showAlert(AlertType.INFORMATION, "Succès", "Étudiant supprimé avec succès !");
                    loadEtudiants(); // Recharger les données après suppression
                    clearFields(); // Vider les champs après suppression
                } catch (SQLException e) {
                    showAlert(AlertType.ERROR, "Erreur Base de Données", "Erreur lors de la suppression de l'étudiant : " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    closeResources(null, pstm, conn);
                }
            }
        });
    }

    @FXML
    private void handleRecherche() { // Le type d'événement ActionEvent n'est pas nécessaire si l'action est déclenchée par un champ de texte
        String searchText = txtrecherche.getText().toLowerCase();
        ObservableList<Etudiant> filteredList = FXCollections.observableArrayList();

        if (searchText.isEmpty()) {
            tbletudiant.setItems(etudiants); // Afficher toute la liste si le champ de recherche est vide
            return;
        }

        for (Etudiant etudiant : etudiants) {
            // Recherche par nom, prénom, email, ou nom de classe
            if (etudiant.getNom().toLowerCase().contains(searchText) ||
                    etudiant.getPrenom().toLowerCase().contains(searchText) ||
                    (etudiant.getEmail() != null && etudiant.getEmail().toLowerCase().contains(searchText)) ||
                    (etudiant.getClasseNom() != null && etudiant.getClasseNom().toLowerCase().contains(searchText))) {
                filteredList.add(etudiant);
            }
        }
        tbletudiant.setItems(filteredList);
    }

    @FXML
    private void handleImageButton(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");
        // Filtre les types de fichiers image
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );
        // Affiche la boîte de dialogue et attend la sélection d'un fichier
        File selectedFile = fileChooser.showOpenDialog(new Stage()); // Utilise une nouvelle Stage comme parent
        if (selectedFile != null) {
            selectedImagePath = selectedFile.getAbsolutePath(); // Stocke le chemin absolu du fichier
            // Affiche l'image sélectionnée dans l'ImageView
            imageViewEtudiant.setImage(new Image(selectedFile.toURI().toString()));
        }
    }

    private void clearFields() {
        txtnom.clear();
        txtprenom.clear();
        txtdatenaissance.setValue(null); // Réinitialise le DatePicker
        txtadresse.clear();
        txttelephone.clear();
        txtemail.clear();
        comboxeclasse.getSelectionModel().clearSelection(); // Désélectionne l'élément de la ComboBox
        imageViewEtudiant.setImage(null); // Efface l'image affichée
        selectedImagePath = null; // Réinitialise le chemin de l'image sélectionnée
        tbletudiant.getSelectionModel().clearSelection(); // Désélectionne l'étudiant dans la TableView
    }

    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null); // Pas d'en-tête spécifique pour cette alerte
        alert.setContentText(content);
        alert.showAndWait(); // Attend que l'utilisateur ferme l'alerte
    }

    // Méthode utilitaire pour fermer les ressources JDBC (ResultSet, PreparedStatement)
    private void closeResources(ResultSet rs, PreparedStatement pstm, Connection conn) {
        try {
            if (rs != null) rs.close();
            if (pstm != null) pstm.close();
            // Important : Ne pas fermer la connexion ici si DB.getConnection() gère un singleton
            // carcela fermerait la connexion globale pour toute l'application, ce qui empêcherait
            // d'autres opérations BD. La classe DB elle-même devrait gérer la fermeture de sa connexion
            // en fin d'application (par exemple, avec un ShutdownHook).
            // Si db.getConnection() renvoie une nouvelle connexion à chaque appel, alors conn.close() serait nécessaire ici.
            // Pour la simplicité de DB comme singleton, nous laissons la connexion ouverte.
            // if(conn != null) conn.close(); // Commentez ceci si DB est un singleton qui gère sa connexion
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void RETOUR(ActionEvent e) throws IOException {
        Outils.load(e,"page etudiant","/com/example/projet_semestre6/AccueilController.fxml");
    }
}