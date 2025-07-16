package com.example.projet_semestre6;

import java.time.LocalDate;

public class Absence {
    private int id;
    private int etudiantId;
    private String etudiantNomComplet; // Pour l'affichage dans la TableView
    private int matiereId;
    private String matiereNom;         // Pour l'affichage dans la TableView
    private LocalDate dateAbsence;
    private String typeAbsence;        // Ex: "Justifiée", "Non Justifiée", "Retard"
    private String justification;      // Description ou chemin vers un justificatif

    // Constructeur complet (pour la lecture depuis la base de données)
    public Absence(int id, int etudiantId, String etudiantNomComplet, int matiereId, String matiereNom,
                   LocalDate dateAbsence, String typeAbsence, String justification) {
        this.id = id;
        this.etudiantId = etudiantId;
        this.etudiantNomComplet = etudiantNomComplet;
        this.matiereId = matiereId;
        this.matiereNom = matiereNom;
        this.dateAbsence = dateAbsence;
        this.typeAbsence = typeAbsence;
        this.justification = justification;
    }

    // Constructeur pour une nouvelle absence (sans ID, car auto-incrémenté)
    public Absence(int etudiantId, int matiereId, LocalDate dateAbsence,
                   String typeAbsence, String justification) {
        this.etudiantId = etudiantId;
        this.matiereId = matiereId;
        this.dateAbsence = dateAbsence;
        this.typeAbsence = typeAbsence;
        this.justification = justification;
    }

    // Constructeur par défaut (nécessaire pour JavaFX TableView et FXMLLoader)
    public Absence() {}

    // Getters (doivent correspondre aux PropertyValueFactory dans le contrôleur)
    public int getId() { return id; }
    public int getEtudiantId() { return etudiantId; }
    public String getEtudiantNomComplet() { return etudiantNomComplet; }
    public int getMatiereId() { return matiereId; }
    public String getMatiereNom() { return matiereNom; }
    public LocalDate getDateAbsence() { return dateAbsence; }
    public String getTypeAbsence() { return typeAbsence; }
    public String getJustification() { return justification; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setEtudiantId(int etudiantId) { this.etudiantId = etudiantId; }
    public void setEtudiantNomComplet(String etudiantNomComplet) { this.etudiantNomComplet = etudiantNomComplet; }
    public void setMatiereId(int matiereId) { this.matiereId = matiereId; }
    public void setMatiereNom(String matiereNom) { this.matiereNom = matiereNom; }
    public void setDateAbsence(LocalDate dateAbsence) { this.dateAbsence = dateAbsence; }
    public void setTypeAbsence(String typeAbsence) { this.typeAbsence = typeAbsence; }
    public void setJustification(String justification) { this.justification = justification; }

    @Override
    public String toString() {
        return "Absence{" +
                "id=" + id +
                ", etudiant='" + etudiantNomComplet + '\'' +
                ", matiere='" + matiereNom + '\'' +
                ", dateAbsence=" + dateAbsence +
                ", typeAbsence='" + typeAbsence + '\'' +
                ", justification='" + justification + '\'' +
                '}';
    }

    // Important pour la sélection dans la TableView
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Absence absence = (Absence) obj;
        return id == absence.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}