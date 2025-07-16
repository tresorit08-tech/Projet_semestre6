package com.example.projet_semestre6;

import java.time.LocalDate;
import java.time.LocalTime; // Important pour les heures

public class Cours {
    private int id;
    private int matiereId;
    private String matiereNom; // Pour l'affichage
    private int enseignantId;
    private String enseignantNomComplet; // Pour l'affichage
    private int classeId;
    private String classeNom; // Pour l'affichage
    private LocalDate jourSemaine; // La date du cours
    private LocalTime heureDebut; // Heure de début
    private LocalTime heureFin;   // Heure de fin
    private String salle;

    // Constructeur complet (pour la lecture depuis la base de données)
    public Cours(int id, int matiereId, String matiereNom, int enseignantId, String enseignantNomComplet,
                 int classeId, String classeNom, LocalDate jourSemaine, LocalTime heureDebut, LocalTime heureFin, String salle) {
        this.id = id;
        this.matiereId = matiereId;
        this.matiereNom = matiereNom;
        this.enseignantId = enseignantId;
        this.enseignantNomComplet = enseignantNomComplet;
        this.classeId = classeId;
        this.classeNom = classeNom;
        this.jourSemaine = jourSemaine;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.salle = salle;
    }

    // Constructeur pour un nouveau cours (sans ID, car auto-incrémenté)
    public Cours(int matiereId, int enseignantId, int classeId, LocalDate jourSemaine,
                 LocalTime heureDebut, LocalTime heureFin, String salle) {
        this.matiereId = matiereId;
        this.enseignantId = enseignantId;
        this.classeId = classeId;
        this.jourSemaine = jourSemaine;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.salle = salle;
    }

    // Constructeur par défaut (nécessaire pour JavaFX TableView et FXMLLoader)
    public Cours() {}

    // Getters
    public int getId() { return id; }
    public int getMatiereId() { return matiereId; }
    public String getMatiereNom() { return matiereNom; }
    public int getEnseignantId() { return enseignantId; }
    public String getEnseignantNomComplet() { return enseignantNomComplet; }
    public int getClasseId() { return classeId; }
    public String getClasseNom() { return classeNom; }
    public LocalDate getJourSemaine() { return jourSemaine; }
    public LocalTime getHeureDebut() { return heureDebut; }
    public LocalTime getHeureFin() { return heureFin; }
    public String getSalle() { return salle; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setMatiereId(int matiereId) { this.matiereId = matiereId; }
    public void setMatiereNom(String matiereNom) { this.matiereNom = matiereNom; }
    public void setEnseignantId(int enseignantId) { this.enseignantId = enseignantId; }
    public void setEnseignantNomComplet(String enseignantNomComplet) { this.enseignantNomComplet = enseignantNomComplet; }
    public void setClasseId(int classeId) { this.classeId = classeId; }
    public void setClasseNom(String classeNom) { this.classeNom = classeNom; }
    public void setJourSemaine(LocalDate jourSemaine) { this.jourSemaine = jourSemaine; }
    public void setHeureDebut(LocalTime heureDebut) { this.heureDebut = heureDebut; }
    public void setHeureFin(LocalTime heureFin) { this.heureFin = heureFin; }
    public void setSalle(String salle) { this.salle = salle; }

    @Override
    public String toString() {
        return "Cours{" +
                "id=" + id +
                ", matiere='" + matiereNom + '\'' +
                ", enseignant='" + enseignantNomComplet + '\'' +
                ", classe='" + classeNom + '\'' +
                ", jourSemaine=" + jourSemaine +
                ", heureDebut=" + heureDebut +
                ", heureFin=" + heureFin +
                ", salle='" + salle + '\'' +
                '}';
    }

    // Important pour la sélection dans la TableView
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Cours cours = (Cours) obj;
        return id == cours.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}