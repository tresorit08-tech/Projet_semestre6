package com.example.projet_semestre6;

import java.time.LocalDate; // Importez LocalDate
import java.io.File; // Pour la gestion des fichiers images (chemin)

public class Etudiant {
    private int id; // Correspond à la colonne 'id' dans la DB
    private String nom;
    private String prenom;
    private LocalDate dateNaissance; // Changé de Date à LocalDate
    private String adresse;
    private String telephone;
    private String email;
    private int classeId; // ID de la classe (clé étrangère vers la table Classes)
    private String classeNom; // Nom de la classe pour l'affichage (ex"Seconde A")
    private String imagePath; // Chemin absolu ou relatif de l'image (stocké dans la DB)

    // Constructeur complet (avec ID)
    public Etudiant(int id, String nom, String prenom, LocalDate dateNaissance, String adresse, String telephone, String email, int classeId, String classeNom, String imagePath) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.dateNaissance = dateNaissance;
        this.adresse = adresse;
        this.telephone = telephone;
        this.email = email;
        this.classeId = classeId;
        this.classeNom = classeNom;
        this.imagePath = imagePath;
    }

    // Constructeur pour un nouvel étudiant (sans ID, car auto-incrémenté par la DB)
    public Etudiant(String nom, String prenom, LocalDate dateNaissance, String adresse, String telephone, String email, int classeId, String classeNom, String imagePath) {
        this.nom = nom;
        this.prenom = prenom;
        this.dateNaissance = dateNaissance;
        this.adresse = adresse;
        this.telephone = telephone;
        this.email = email;
        this.classeId = classeId;
        this.classeNom = classeNom;
        this.imagePath = imagePath;
    }

    // Constructeur vide (nécessaire pour JavaFX TableView et FXMLLoader dans certains cas)
    public Etudiant() {
    }

    // Getters et Setters (les noms des getters doivent correspondre aux "PropertyValueFactory" du contrôleur)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getClasseId() {
        return classeId;
    }

    public void setClasseId(int classeId) {
        this.classeId = classeId;
    }

    public String getClasseNom() { // Getter pour l'affichage du nom de la classe dans la TableView
        return classeNom;
    }

    public void setClasseNom(String classeNom) {
        this.classeNom = classeNom;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    // Méthode utilitaire pour obtenir un objet File à partir du chemin de l'image (utile pour ImageView)
    public File getImageFile() {
        return (imagePath != null && !imagePath.isEmpty()) ? new File(imagePath) : null;
    }

    // MÉTHODE TOSTRING CORRIGÉE POUR L'AFFICHAGE DANS LES COMBOBOX
    @Override
    public String toString() {
        // Retourne le nom et le prénom de l'étudiant pour un affichage convivial
        return nom + " " + prenom;
    }
}