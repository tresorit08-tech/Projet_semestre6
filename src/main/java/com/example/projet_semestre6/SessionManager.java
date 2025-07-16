package com.example.projet_semestre6;

/**
 * Gère la session de l'utilisateur connecté.
 * Cette classe singleton stocke le rôle de l'utilisateur et son ID d'étudiant (si applicable)
 * pour qu'ils soient accessibles globalement dans l'application après la connexion.
 */
public class SessionManager {
    private static SessionManager instance;

    private String currentUserRole; // Peut être "admin" ou "etudiant"
    private int currentEtudiantId;  // ID de l'étudiant si l'utilisateur est un étudiant, sinon -1

    // Constructeur privé pour le pattern Singleton
    private SessionManager() {
        // Initialisation par défaut
        this.currentUserRole = null;
        this.currentEtudiantId = -1; // -1 pour indiquer qu'aucun ID étudiant n'est défini
    }

    /**
     * Retourne l'instance unique de SessionManager (Singleton).
     * @return L'instance de SessionManager.
     */
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    // --- Getters ---
    public String getCurrentUserRole() {
        return currentUserRole;
    }

    public int getCurrentEtudiantId() {
        return currentEtudiantId;
    }

    /**
     * Vérifie si l'utilisateur connecté est un administrateur.
     * @return true si le rôle est "admin", false sinon.
     */
    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(currentUserRole);
    }

    /**
     * Vérifie si l'utilisateur connecté est un étudiant.
     * @return true si le rôle est "etudiant", false sinon.
     */
    public boolean isEtudiant() {
        return "etudiant".equalsIgnoreCase(currentUserRole);
    }

    // --- Setters pour définir l'état de la session après connexion ---

    /**
     * Définit les informations de l'utilisateur connecté.
     * @param role Le rôle de l'utilisateur ("admin" ou "etudiant").
     * @param etudiantId L'ID de l'étudiant si le rôle est "etudiant". Ignoré si le rôle est "admin".
     */
    public void setLoggedInUser(String role, int etudiantId) {
        this.currentUserRole = role;
        if ("etudiant".equalsIgnoreCase(role)) {
            this.currentEtudiantId = etudiantId;
        } else {
            this.currentEtudiantId = -1; // Réinitialise si ce n'est pas un étudiant
        }
        System.out.println("SessionManager: Utilisateur connecté - Rôle: " + currentUserRole + ", Etudiant ID: " + currentEtudiantId);
    }

    /**
     * Réinitialise la session (déconnexion).
     */
    public void logout() {
        this.currentUserRole = null;
        this.currentEtudiantId = -1;
        System.out.println("SessionManager: Utilisateur déconnecté.");
    }
}
