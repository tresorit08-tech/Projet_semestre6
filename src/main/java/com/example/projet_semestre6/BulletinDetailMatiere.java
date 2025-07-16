package com.example.projet_semestre6;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente le détail d'une matière spécifique dans le bulletin de notes d'un étudiant.
 * Contient les notes individuelles, la moyenne pondérée de la matière et la mention.
 */
public class BulletinDetailMatiere {
    private String matiereNom;          // Nom de la matière (ex: "Mathématiques")
    private double matiereCoefficient;  // Coefficient global de la matière (issu de la table Matieres)
    // Nous utiliserons une liste de chaînes pour les notes individuelles pour simplifier l'affichage.
    // Chaque chaîne pourrait être formatée comme "valeur/20 (type_evaluation - coeff_note)"
    private List<String> notesIndividuelles; // Liste des notes individuelles de l'étudiant pour cette matière
    private double moyenneMatiere;      // Moyenne pondérée calculée pour cette matière
    private String mentionMatiere;      // Mention obtenue pour cette matière (Admis, Très Bien, etc.)

    /**
     * Constructeur pour initialiser un BulletinDetailMatiere.
     * @param matiereNom Nom de la matière.
     * @param matiereCoefficient Coefficient global de la matière.
     * @param moyenneMatiere Moyenne pondérée calculée pour cette matière.
     * @param mentionMatiere Mention obtenue pour cette matière.
     */
    public BulletinDetailMatiere(String matiereNom, double matiereCoefficient, double moyenneMatiere, String mentionMatiere) {
        this.matiereNom = matiereNom;
        this.matiereCoefficient = matiereCoefficient;
        this.moyenneMatiere = moyenneMatiere;
        this.mentionMatiere = mentionMatiere;
        this.notesIndividuelles = new ArrayList<>(); // Initialise la liste vide, les notes seront ajoutées après
    }

    // --- Getters ---
    public String getMatiereNom() {
        return matiereNom;
    }

    public double getMatiereCoefficient() {
        return matiereCoefficient;
    }

    public List<String> getNotesIndividuelles() {
        return notesIndividuelles;
    }

    public double getMoyenneMatiere() {
        return moyenneMatiere;
    }

    public String getMentionMatiere() {
        return mentionMatiere;
    }

    // --- Setters (si nécessaire pour modifier les propriétés après construction) ---
    public void setMatiereNom(String matiereNom) {
        this.matiereNom = matiereNom;
    }

    public void setMatiereCoefficient(double matiereCoefficient) {
        this.matiereCoefficient = matiereCoefficient;
    }

    public void setNotesIndividuelles(List<String> notesIndividuelles) {
        this.notesIndividuelles = notesIndividuelles;
    }

    public void setMoyenneMatiere(double moyenneMatiere) {
        this.moyenneMatiere = moyenneMatiere;
    }

    public void setMentionMatiere(String mentionMatiere) {
        this.mentionMatiere = mentionMatiere;
    }

    /**
     * Ajoute une note individuelle formatée à la liste.
     * @param noteFormatted La note formatée (ex: "15.0/20 (DS1 - Coeff 1.0)").
     */
    public void addNoteIndividuelle(String noteFormatted) {
        this.notesIndividuelles.add(noteFormatted);
    }

    @Override
    public String toString() {
        return "Matiere: " + matiereNom + " (Coeff: " + matiereCoefficient + "), Moyenne: " + String.format("%.2f", moyenneMatiere) + ", Mention: " + mentionMatiere + ", Notes: " + notesIndividuelles;
    }
}