package com.example.projet_semestre6;

public class Matiere {
    private int id;
    private String nomMatiere;
    private double coefficient; // Utiliser double pour DECIMAL(3,1)

    // Constructeur complet
    public Matiere(int id, String nomMatiere, double coefficient) {
        this.id = id;
        this.nomMatiere = nomMatiere;
        this.coefficient = coefficient;
    }

    // Constructeur pour une nouvelle matière (ID auto-incrémenté)
    public Matiere(String nomMatiere, double coefficient) {
        this.nomMatiere = nomMatiere;
        this.coefficient = coefficient;
    }

    // Constructeur par défaut (nécessaire pour JavaFX TableView)
    public Matiere() {}

    // Getters (doivent correspondre aux PropertyValueFactory du contrôleur)
    public int getId() { return id; }
    public String getNomMatiere() { return nomMatiere; }
    public double getCoefficient() { return coefficient; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setNomMatiere(String nomMatiere) { this.nomMatiere = nomMatiere; }
    public void setCoefficient(double coefficient) { this.coefficient = coefficient; }

    @Override
    public String toString() {
        return nomMatiere + " (Coef: " + coefficient + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Matiere matiere = (Matiere) obj;
        return id == matiere.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}