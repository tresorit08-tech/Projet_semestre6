package com.example.projet_semestre6;

public class Enseignant {
        private int id;
        private String nom;
        private String prenom;
        private String specialite;
        private String telephone;
        private String email;

        // Constructeur complet
        public Enseignant(int id, String nom, String prenom, String specialite, String telephone, String email) {
            this.id = id;
            this.nom = nom;
            this.prenom = prenom;
            this.specialite = specialite;
            this.telephone = telephone;
            this.email = email;
        }

        // Constructeur pour un nouvel enseignant (ID auto-incrémenté)
        public Enseignant(String nom, String prenom, String specialite, String telephone, String email) {
            this.nom = nom;
            this.prenom = prenom;
            this.specialite = specialite;
            this.telephone = telephone;
            this.email = email;
        }

        // Constructeur par défaut (nécessaire pour JavaFX)
        public Enseignant() {}

        // Getters (doivent correspondre EXACTEMENT aux PropertyValueFactory du contrôleur)
        public int getId() { return id; }
        public String getNom() { return nom; }
        public String getPrenom() { return prenom; }
        public String getSpecialite() { return specialite; }
        public String getTelephone() { return telephone; }
        public String getEmail() { return email; }

        // Setters
        public void setId(int id) { this.id = id; }
        public void setNom(String nom) { this.nom = nom; }
        public void setPrenom(String prenom) { this.prenom = prenom; }
        public void setSpecialite(String specialite) { this.specialite = specialite; }
        public void setTelephone(String telephone) { this.telephone = telephone; }
        public void setEmail(String email) { this.email = email; }

        @Override
        public String toString() {
            return "Enseignant{" +
                    "id=" + id +
                    ", nom='" + nom + '\'' +
                    ", prenom='" + prenom + '\'' +
                    ", specialite='" + specialite + '\'' +
                    ", telephone='" + telephone + '\'' +
                    ", email='" + email + '\'' +
                    '}';
        }

        // Important pour la sélection dans la TableView et potentiellement d'autres collections
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Enseignant that = (Enseignant) obj;
            return id == that.id;
        }

        @Override
        public int hashCode() {
            return Integer.hashCode(id);
        }
}
