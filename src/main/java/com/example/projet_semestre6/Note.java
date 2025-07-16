package com.example.projet_semestre6;

import java.time.LocalDate;

public class Note {
    private int id;
    private int etudiantId;
    private String etudiantNomComplet; // Nom complet de l'étudiant pour l'affichage
    private int matiereId;
    private String matiereNom;         // Nom de la matière pour l'affichage
    private int enseignantId;
    private String enseignantNomComplet; // Nom complet de l'enseignant pour l'affichage
    private LocalDate dateNote;
    private double valeurNote;
    private String typeEvaluation;    // Ex: "Examen", "Devoir", "Participation"
    private double coeffNote;         // Coefficient de la note

    // Constructeur complet pour les données lues depuis la base de données
    public Note(int id, int etudiantId, String etudiantNomComplet, int matiereId, String matiereNom,
                int enseignantId, String enseignantNomComplet, LocalDate dateNote,
                double valeurNote, String typeEvaluation, double coeffNote) {
        this.id = id;
        this.etudiantId = etudiantId;
        this.etudiantNomComplet = etudiantNomComplet;
        this.matiereId = matiereId;
        this.matiereNom = matiereNom;
        this.enseignantId = enseignantId;
        this.enseignantNomComplet = enseignantNomComplet;
        this.dateNote = dateNote;
        this.valeurNote = valeurNote;
        this.typeEvaluation = typeEvaluation;
        this.coeffNote = coeffNote;
    }

    // Constructeur pour une nouvelle note (sans ID, car auto-incrémenté)
    public Note(int etudiantId, int matiereId, int enseignantId, LocalDate dateNote,
                double valeurNote, String typeEvaluation, double coeffNote) {
        this.etudiantId = etudiantId;
        this.matiereId = matiereId;
        this.enseignantId = enseignantId;
        this.dateNote = dateNote;
        this.valeurNote = valeurNote;
        this.typeEvaluation = typeEvaluation;
        this.coeffNote = coeffNote;
    }

    // Constructeur par défaut (nécessaire pour JavaFX TableView et FXMLLoader)
    public Note() {}

    // Getters (doivent correspondre aux PropertyValueFactory dans le contrôleur)
    public int getId() { return id; }
    public int getEtudiantId() { return etudiantId; }
    public String getEtudiantNomComplet() { return etudiantNomComplet; }
    public int getMatiereId() { return matiereId; }
    public String getMatiereNom() { return matiereNom; }
    public int getEnseignantId() { return enseignantId; }
    public String getEnseignantNomComplet() { return enseignantNomComplet; }
    public LocalDate getDateNote() { return dateNote; }
    public double getValeurNote() { return valeurNote; }
    public String getTypeEvaluation() { return typeEvaluation; }
    public double getCoeffNote() { return coeffNote; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setEtudiantId(int etudiantId) { this.etudiantId = etudiantId; }
    public void setEtudiantNomComplet(String etudiantNomComplet) { this.etudiantNomComplet = etudiantNomComplet; }
    public void setMatiereId(int matiereId) { this.matiereId = matiereId; }
    public void setMatiereNom(String matiereNom) { this.matiereNom = matiereNom; }
    public void setEnseignantId(int enseignantId) { this.enseignantId = enseignantId; }
    public void setEnseignantNomComplet(String enseignantNomComplet) { this.enseignantNomComplet = enseignantNomComplet; }
    public void setDateNote(LocalDate dateNote) { this.dateNote = dateNote; }
    public void setValeurNote(double valeurNote) { this.valeurNote = valeurNote; }
    public void setTypeEvaluation(String typeEvaluation) { this.typeEvaluation = typeEvaluation; }
    public void setCoeffNote(double coeffNote) { this.coeffNote = coeffNote; }

    @Override
    public String toString() {
        return "Note{" +
                "id=" + id +
                ", etudiant='" + etudiantNomComplet + '\'' +
                ", matiere='" + matiereNom + '\'' +
                ", enseignant='" + enseignantNomComplet + '\'' +
                ", dateNote=" + dateNote +
                ", valeurNote=" + valeurNote +
                ", typeEvaluation='" + typeEvaluation + '\'' +
                ", coeffNote=" + coeffNote +
                '}';
    }

    // Important pour la sélection dans la TableView
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Note note = (Note) obj;
        return id == note.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}