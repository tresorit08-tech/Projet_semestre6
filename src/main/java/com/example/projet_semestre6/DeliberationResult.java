package com.example.projet_semestre6;

// Cette classe sert de modèle de données simple pour représenter une ligne de résultat
// dans la TableView de délibération. Elle contient les informations agrégées
// (nom de la matière, moyenne calculée, coefficient de la matière, et mention).
public class DeliberationResult {
    private String matiereNom;       // Le nom de la matière (ex: "Mathematiques")
    private double moyenneMatiere;   // La moyenne pondérée calculée pour cette matière
    private double coefficientMatiere; // Le coefficient global de la matière (issu de la table Matieres)
    private String mention;          // La mention attribuée (ex: "Admis", "Très Bien", "Refusé")

    /**
     * Constructeur pour créer un objet DeliberationResult.
     * Les noms des paramètres correspondent aux propriétés de la classe.
     * @param matiereNom Le nom de la matière.
     * @param moyenneMatiere La moyenne pondérée de la matière pour l'étudiant.
     * @param coefficientMatiere Le coefficient global de la matière.
     * @param mention La mention obtenue pour cette matière.
     */
    public DeliberationResult(String matiereNom, double moyenneMatiere, double coefficientMatiere, String mention) {
        this.matiereNom = matiereNom;
        this.moyenneMatiere = moyenneMatiere;
        this.coefficientMatiere = coefficientMatiere;
        this.mention = mention;
    }

    /**
     * Getter pour le nom de la matière.
     * ESSENTIEL pour PropertyValueFactory afin d'afficher la valeur dans la colonne "MATIÈRE".
     * Le nom de la méthode doit être getMatiereNom() pour correspondre à "matiereNom" dans FXML.
     * @return Le nom de la matière.
     */
    public String getMatiereNom() {
        return matiereNom;
    }

    /**
     * Getter pour la moyenne de la matière.
     * ESSENTIEL pour PropertyValueFactory afin d'afficher la valeur dans la colonne "MOYENNE".
     * Le nom de la méthode doit être getMoyenneMatiere() pour correspondre à "moyenneMatiere" dans FXML.
     * @return La moyenne pondérée de la matière.
     */
    public double getMoyenneMatiere() {
        return moyenneMatiere;
    }

    /**
     * Getter pour le coefficient de la matière.
     * ESSENTIEL pour PropertyValueFactory afin d'afficher la valeur dans la colonne "COEFF. MATIÈRE".
     * Le nom de la méthode doit être getCoefficientMatiere() pour correspondre à "coefficientMatiere" dans FXML.
     * @return Le coefficient global de la matière.
     */
    public double getCoefficientMatiere() {
        return coefficientMatiere;
    }

    /**
     * Getter pour la mention.
     * ESSENTIEL pour PropertyValueFactory afin d'afficher la valeur dans la colonne "MENTION".
     * Le nom de la méthode doit être getMention() pour correspondre à "mention" dans FXML.
     * @return La mention textuelle.
     */
    public String getMention() {
        return mention;
    }

    // --- Setters (méthodes optionnelles pour modifier les valeurs après la création de l'objet) ---
    // Ces setters ne sont pas directement utilisés par PropertyValueFactory pour l'affichage initial,
    // mais seraient nécessaires si la TableView était éditable ou si les propriétés devaient être modifiées.

    public void setMatiereNom(String matiereNom) {
        this.matiereNom = matiereNom;
    }

    public void setMoyenneMatiere(double moyenneMatiere) {
        this.moyenneMatiere = moyenneMatiere;
    }

    public void setCoefficientMatiere(double coefficientMatiere) {
        this.coefficientMatiere = coefficientMatiere;
    }

    public void setMention(String mention) {
        this.mention = mention;
    }

    /**
     * Méthode toString() pour fournir une représentation textuelle conviviale de l'objet.
     * Utile pour le débogage (par exemple, avec System.out.println).
     * @return Une chaîne de caractères décrivant l'objet DeliberationResult.
     */
    @Override
    public String toString() {
        return "Matiere: " + matiereNom + ", Moyenne: " + String.format("%.2f", moyenneMatiere) + ", Coefficient: " + coefficientMatiere + ", Mention: " + mention;
    }
}