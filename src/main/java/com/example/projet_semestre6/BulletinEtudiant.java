package com.example.projet_semestre6;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Représente le bulletin de notes complet pour un étudiant.
 * Cette classe agrège toutes les informations nécessaires pour l'affichage du bulletin,
 * y compris les détails de l'étudiant, sa classe, les résultats par matière,
 * la moyenne générale, la mention générale, le rang, les informations d'absence et l'appréciation générale.
 */
public class BulletinEtudiant {
    // --- Informations d'En-tête (Détails de l'étudiant et de l'établissement) ---
    private String nomEtablissement;
    private String nomEtudiant;
    private String classeEtudiant;
    private String anneeScolaire;
    private int etudiantId;

    // --- Détails des Matières ---
    private List<BulletinDetailMatiere> detailsMatieres;

    // --- Résumé Général ---
    private double moyenneGenerale;
    private String mentionGenerale;
    private String appreciationGenerale; // NOUVEAU: Appréciation générale de l'étudiant
    private int rang;

    // --- Informations sur les Absences ---
    private int totalAbsences;        // Nombre total d'absences
    private int justifiedAbsences;    // Nombre d'absences justifiées
    private int unjustifiedAbsences; // Nombre d'absences injustifiées
    private List<String> detailedAbsences; // Liste des détails de chaque absence (ex: "Date: 2024-01-15, Type: Maladie, Justifié: Oui")

    /**
     * Constructeur pour initialiser un objet BulletinEtudiant.
     * @param nomEtablissement Nom de l'établissement.
     * @param nomEtudiant Nom complet de l'étudiant.
     * @param classeEtudiant Nom de la classe de l'étudiant.
     * @param anneeScolaire Année scolaire.
     * @param etudiantId ID de l'étudiant.
     */
    public BulletinEtudiant(String nomEtablissement, String nomEtudiant, String classeEtudiant, String anneeScolaire, int etudiantId) {
        this.nomEtablissement = nomEtablissement;
        this.nomEtudiant = nomEtudiant;
        this.classeEtudiant = classeEtudiant;
        this.anneeScolaire = anneeScolaire;
        this.etudiantId = etudiantId;
        this.detailsMatieres = new ArrayList<>();
        this.moyenneGenerale = 0.0;
        this.mentionGenerale = "N/A";
        this.appreciationGenerale = "N/A"; // Initialisation de l'appréciation
        this.rang = 0;
        // Initialisation des champs d'absences
        this.totalAbsences = 0;
        this.justifiedAbsences = 0;
        this.unjustifiedAbsences = 0;
        this.detailedAbsences = new ArrayList<>();
    }

    // --- Getters ---
    public String getNomEtablissement() {
        return nomEtablissement;
    }

    public String getNomEtudiant() {
        return nomEtudiant;
    }

    public String getClasseEtudiant() {
        return classeEtudiant;
    }

    public String getAnneeScolaire() {
        return anneeScolaire;
    }

    public int getEtudiantId() {
        return etudiantId;
    }

    public List<BulletinDetailMatiere> getDetailsMatieres() {
        return detailsMatieres;
    }

    public double getMoyenneGenerale() {
        return moyenneGenerale;
    }

    public String getMentionGenerale() {
        return mentionGenerale;
    }

    public String getAppreciationGenerale() { // NOUVEAU: Getter pour l'appréciation
        return appreciationGenerale;
    }

    public int getRang() {
        return rang;
    }

    public int getTotalAbsences() {
        return totalAbsences;
    }

    public int getJustifiedAbsences() {
        return justifiedAbsences;
    }

    public int getUnjustifiedAbsences() {
        return unjustifiedAbsences;
    }

    public List<String> getDetailedAbsences() {
        return detailedAbsences;
    }

    // --- Setters (pour mettre à jour les valeurs après les calculs) ---
    public void setNomEtablissement(String nomEtablissement) {
        this.nomEtablissement = nomEtablissement;
    }

    public void setNomEtudiant(String nomEtudiant) {
        this.nomEtudiant = nomEtudiant;
    }

    public void setClasseEtudiant(String classeEtudiant) {
        this.classeEtudiant = classeEtudiant;
    }

    public void setAnneeScolaire(String anneeScolaire) {
        this.anneeScolaire = anneeScolaire;
    }

    public void setMoyenneGenerale(double moyenneGenerale) {
        this.moyenneGenerale = moyenneGenerale;
    }

    public void setMentionGenerale(String mentionGenerale) {
        this.mentionGenerale = mentionGenerale;
    }

    public void setAppreciationGenerale(String appreciationGenerale) { // NOUVEAU: Setter pour l'appréciation
        this.appreciationGenerale = appreciationGenerale;
    }

    public void setRang(int rang) {
        this.rang = rang;
    }

    public void setTotalAbsences(int totalAbsences) {
        this.totalAbsences = totalAbsences;
    }

    public void setJustifiedAbsences(int justifiedAbsences) {
        this.justifiedAbsences = justifiedAbsences;
    }

    public void setUnjustifiedAbsences(int unjustifiedAbsences) {
        this.unjustifiedAbsences = unjustifiedAbsences;
    }

    public void setDetailedAbsences(List<String> detailedAbsences) {
        this.detailedAbsences = detailedAbsences;
    }

    public void addDetailMatiere(BulletinDetailMatiere detail) {
        this.detailsMatieres.add(detail);
    }

    @Override
    public String toString() {
        return "Bulletin pour " + nomEtudiant + " (" + classeEtudiant + ", " + anneeScolaire + ")" +
                "\nMoyenne Générale: " + String.format("%.2f", moyenneGenerale) + ", Mention: " + mentionGenerale + ", Appréciation: " + appreciationGenerale + ", Rang: " + rang +
                "\nAbsences: Total=" + totalAbsences + ", Justifiées=" + justifiedAbsences + ", Injustifiées=" + unjustifiedAbsences +
                "\nDétails des matières: " + detailsMatieres.size() + " matières.";
    }
}
