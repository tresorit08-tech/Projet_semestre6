package com.example.projet_semestre6;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

import java.io.IOException;

public class AccueilController {
    @FXML
    private Button btndeconnexion;

    @FXML
    private Button btnetudiant;

    @FXML
    private Button btnenseignant;

    @FXML
    private Button btnclasse;

    @FXML
    private Button btnmatiere;

    @FXML
    private Button btnenseignantmatiere;

    @FXML
    private Button btncour;

    @FXML
    private Button btnnote;

    @FXML
    private Button btnabsence;

    @FXML
    private Button btndeliberation;


    @FXML
    public void deconnexion(ActionEvent e) throws IOException {
        Outils.load(e,"page etudiant","/com/example/projet_semestre6/hello-view.fxml");
    }
    @FXML
    public void etudiant(ActionEvent e) throws IOException {
        Outils.load(e,"page etudiant","/com/example/projet_semestre6/EtudiantController.fxml");
    }
    @FXML
    public void enseignant(ActionEvent e) throws IOException {
        Outils.load(e,"page etudiant","/com/example/projet_semestre6/EnseignantController.fxml");
    }
    @FXML
    public void classe(ActionEvent e) throws IOException {
        Outils.load(e,"page etudiant","/com/example/projet_semestre6/ClasseController.fxml");
    }
    @FXML
    public void matiere(ActionEvent e) throws IOException {
        Outils.load(e,"page etudiant","/com/example/projet_semestre6/MatiereController.fxml");
    }
    @FXML
    public void enseignantmatiere(ActionEvent e) throws IOException {
        Outils.load(e,"page etudiant","/com/example/projet_semestre6/EnseignantMatiereController.fxml");
    }
    @FXML
    public void cours(ActionEvent e) throws IOException {
        Outils.load(e,"page etudiant","/com/example/projet_semestre6/CoursController.fxml");
    }
    @FXML
    public void notes(ActionEvent e) throws IOException {
        Outils.load(e,"page etudiant","/com/example/projet_semestre6/NotesController.fxml");
    }
    @FXML
    public void absences(ActionEvent e) throws IOException {
        Outils.load(e,"page etudiant","/com/example/projet_semestre6/AbsencesController.fxml");
    }
    @FXML
    public void deliberation(ActionEvent e) throws IOException {
        Outils.load(e,"page etudiant", "/com/example/projet_semestre6/Deliberation.fxml");
    }

}
