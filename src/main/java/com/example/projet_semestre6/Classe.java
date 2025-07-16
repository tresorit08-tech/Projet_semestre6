package com.example.projet_semestre6;

public class Classe {
    private int id;
    private String nomClasse;
    private String anneeScolaire; // Ex: "2024-2025"
    private String niveau; // Ex: "SECONDE", "PREMIERE", "TERMINALE"

    // Constructeur complet
    public Classe(int id, String nomClasse, String anneeScolaire, String niveau) {
        this.id = id;
        this.nomClasse = nomClasse;
        this.anneeScolaire = anneeScolaire;
        this.niveau = niveau;
    }

    // Constructeur pour un nouvel objet Classe (sans ID, car auto-incrémenté)
    public Classe(String nomClasse, String anneeScolaire, String niveau) {
        this.nomClasse = nomClasse;
        this.anneeScolaire = anneeScolaire;
        this.niveau = niveau;
    }

    // Constructeur par défaut (utile pour TableView et FXMLLoader)
    public Classe() {}

    // Constructeur utilisé spécifiquement pour la ComboBox et la récupération simple
    // J'ai corrigé ce constructeur pour qu'il initialise correctement les champs.
    public Classe(int id, String nomClasse) {
        this.id = id;
        this.nomClasse = nomClasse;
        // Les autres champs (anneeScolaire, niveau) ne sont pas initialisés ici car ce constructeur est minimal.
    }

    // Getters
    public int getId() { return id; }
    public String getNomClasse() { return nomClasse; }
    public String getAnneeScolaire() { return anneeScolaire; }
    public String getNiveau() { return niveau; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setNomClasse(String nomClasse) { this.nomClasse = nomClasse; }
    public void setAnneeScolaire(String anneeScolaire) { this.anneeScolaire = anneeScolaire; }
    public void setNiveau(String niveau) { this.niveau = niveau; }

    // Méthode toString pour un affichage convivial (par exemple dans une ComboBox)
    @Override
    public String toString() {
        return nomClasse; // Ceci est CRUCIAL pour que la ComboBox affiche le nom de la classe
    }

    // Méthodes equals et hashCode pour la comparaison des objets (important pour les collections JavaFX)
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Classe classe = (Classe) obj;
        return id == classe.id; // Compare les classes par leur ID
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}