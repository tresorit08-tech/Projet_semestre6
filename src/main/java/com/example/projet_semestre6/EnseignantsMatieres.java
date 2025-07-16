package com.example.projet_semestre6;

public class EnseignantsMatieres {
    private int enseignantId;
    private String enseignantNomComplet; // Pour l'affichage dans la TableView
    private int matiereId;
    private String matiereNom;          // Pour l'affichage dans la TableView

    // Constructeur complet pour les données récupérées de la DB (avec noms pour l'affichage)
    public EnseignantsMatieres(int enseignantId, String enseignantNomComplet, int matiereId, String matiereNom) {
        this.enseignantId = enseignantId;
        this.enseignantNomComplet = enseignantNomComplet;
        this.matiereId = matiereId;
        this.matiereNom = matiereNom;
    }

    // Constructeur pour une nouvelle affectation (IDs seulement)
    public EnseignantsMatieres(int enseignantId, int matiereId) {
        this.enseignantId = enseignantId;
        this.matiereId = matiereId;
    }

    // Constructeur par défaut (nécessaire pour JavaFX si d'autres constructeurs sont présents)
    public EnseignantsMatieres() {}

    // Getters
    public int getEnseignantId() { return enseignantId; }
    public String getEnseignantNomComplet() { return enseignantNomComplet; }
    public int getMatiereId() { return matiereId; }
    public String getMatiereNom() { return matiereNom; }

    // Setters
    public void setEnseignantId(int enseignantId) { this.enseignantId = enseignantId; }
    public void setEnseignantNomComplet(String enseignantNomComplet) { this.enseignantNomComplet = enseignantNomComplet; }
    public void setMatiereId(int matiereId) { this.matiereId = matiereId; }
    public void setMatiereNom(String matiereNom) { this.matiereNom = matiereNom; }

    @Override
    public String toString() {
        return "Affectation: " + enseignantNomComplet + " - " + matiereNom;
    }

    // Important pour la comparaison et la suppression dans les listes
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        EnseignantsMatieres that = (EnseignantsMatieres) obj;
        return enseignantId == that.enseignantId && matiereId == that.matiereId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(enseignantId) + Integer.hashCode(matiereId);
    }
}