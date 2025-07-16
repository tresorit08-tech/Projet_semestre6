package com.example.projet_semestre6;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Classe utilitaire pour générer un bulletin de notes au format PDF.
 * Utilise la bibliothèque iText.
 */
public class PdfGenerator {

    // Définition des polices pour le PDF
    private static Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD, BaseColor.BLACK);
    private static Font HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, BaseColor.DARK_GRAY);
    private static Font SUBHEADER_FONT = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.BLACK);
    private static Font NORMAL_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.BLACK);
    private static Font BOLD_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.BLACK);
    private static Font SMALL_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.GRAY);

    // Pour formater les nombres
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.00");

    /**
     * Génère le bulletin de notes d'un étudiant au format PDF.
     * @param bulletin L'objet BulletinEtudiant contenant toutes les données.
     * @param filePath Le chemin complet où le fichier PDF sera enregistré.
     * @throws DocumentException Si une erreur survient lors de la création du document PDF.
     * @throws IOException Si une erreur survient lors de l'écriture du fichier.
     */
    public static void generateBulletinPdf(BulletinEtudiant bulletin, String filePath) throws DocumentException, IOException {
        Document document = new Document(PageSize.A4, 30, 30, 30, 30); // Marges (gauche, droite, haut, bas)
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();

        // Ajouter l'en-tête du bulletin
        addHeader(document, bulletin);

        // Ajouter les détails par matière
        addMatiereDetails(document, bulletin.getDetailsMatieres());

        // Ajouter le résumé général
        addGeneralSummary(document, bulletin);

        // Ajouter les informations sur les absences
        addAbsenceDetails(document, bulletin);

        document.close();
        System.out.println("PDF généré avec succès à : " + filePath);
    }

    /**
     * Ajoute l'en-tête du bulletin (informations de l'établissement et de l'étudiant).
     * @param document Le document PDF.
     * @param bulletin L'objet BulletinEtudiant.
     * @throws DocumentException
     */
    private static void addHeader(Document document, BulletinEtudiant bulletin) throws DocumentException {
        Paragraph title = new Paragraph("BULLETIN DE NOTES", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph(" ")); // Ligne vide

        Paragraph estab = new Paragraph(bulletin.getNomEtablissement(), HEADER_FONT);
        estab.setAlignment(Element.ALIGN_CENTER);
        document.add(estab);
        document.add(new Paragraph("Année Scolaire : " + bulletin.getAnneeScolaire(), SUBHEADER_FONT));
        document.add(new Paragraph(" ")); // Ligne vide

        PdfPTable studentInfoTable = new PdfPTable(2);
        studentInfoTable.setWidthPercentage(100);
        studentInfoTable.setSpacingBefore(10f);
        studentInfoTable.setSpacingAfter(10f);
        studentInfoTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        studentInfoTable.addCell(new Phrase("Étudiant :", BOLD_FONT));
        studentInfoTable.addCell(new Phrase(bulletin.getNomEtudiant(), NORMAL_FONT));
        studentInfoTable.addCell(new Phrase("Classe :", BOLD_FONT));
        studentInfoTable.addCell(new Phrase(bulletin.getClasseEtudiant(), NORMAL_FONT));

        document.add(studentInfoTable);
        document.add(new Paragraph(" ")); // Ligne vide
    }

    /**
     * Ajoute le tableau des détails par matière.
     * @param document Le document PDF.
     * @param detailsMatieres La liste des détails par matière.
     * @throws DocumentException
     */
    private static void addMatiereDetails(Document document, List<BulletinDetailMatiere> detailsMatieres) throws DocumentException {
        document.add(new Paragraph("Détails des Matières", SUBHEADER_FONT));
        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(5); // 5 colonnes: Matière, Coeff, Notes, Moyenne, Mention
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);
        float[] columnWidths = {2f, 0.8f, 3f, 1f, 1.5f}; // Proportions des colonnes
        table.setWidths(columnWidths);

        // En-têtes du tableau
        addCell(table, "Matière", BOLD_FONT, BaseColor.LIGHT_GRAY, Element.ALIGN_CENTER);
        addCell(table, "Coeff.", BOLD_FONT, BaseColor.LIGHT_GRAY, Element.ALIGN_CENTER);
        addCell(table, "Notes Détaillées", BOLD_FONT, BaseColor.LIGHT_GRAY, Element.ALIGN_CENTER);
        addCell(table, "Moyenne", BOLD_FONT, BaseColor.LIGHT_GRAY, Element.ALIGN_CENTER);
        addCell(table, "Mention", BOLD_FONT, BaseColor.LIGHT_GRAY, Element.ALIGN_CENTER);

        // Lignes de données
        if (detailsMatieres.isEmpty()) {
            PdfPCell noDataCell = new PdfPCell(new Phrase("Aucune note enregistrée.", NORMAL_FONT));
            noDataCell.setColspan(5);
            noDataCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            noDataCell.setPadding(5);
            table.addCell(noDataCell);
        } else {
            for (BulletinDetailMatiere detail : detailsMatieres) {
                addCell(table, detail.getMatiereNom(), NORMAL_FONT, null, Element.ALIGN_LEFT);
                addCell(table, DECIMAL_FORMAT.format(detail.getMatiereCoefficient()), NORMAL_FONT, null, Element.ALIGN_CENTER);
                addCell(table, String.join("\n", detail.getNotesIndividuelles()), SMALL_FONT, null, Element.ALIGN_LEFT); // Notes sur plusieurs lignes
                addCell(table, DECIMAL_FORMAT.format(detail.getMoyenneMatiere()), NORMAL_FONT, null, Element.ALIGN_CENTER);
                addCell(table, detail.getMentionMatiere(), NORMAL_FONT, null, Element.ALIGN_CENTER);
            }
        }
        document.add(table);
    }

    /**
     * Ajoute le résumé général de l'étudiant (moyenne, mention, rang, appréciation).
     * @param document Le document PDF.
     * @param bulletin L'objet BulletinEtudiant.
     * @throws DocumentException
     */
    private static void addGeneralSummary(Document document, BulletinEtudiant bulletin) throws DocumentException {
        document.add(new Paragraph("Résumé Général", SUBHEADER_FONT));
        document.add(new Paragraph(" "));

        PdfPTable summaryTable = new PdfPTable(2);
        summaryTable.setWidthPercentage(100);
        summaryTable.setSpacingBefore(10f);
        summaryTable.setSpacingAfter(10f);
        summaryTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        summaryTable.addCell(new Phrase("Moyenne Générale :", BOLD_FONT));
        summaryTable.addCell(new Phrase(DECIMAL_FORMAT.format(bulletin.getMoyenneGenerale()), NORMAL_FONT));
        summaryTable.addCell(new Phrase("Mention Générale :", BOLD_FONT));
        summaryTable.addCell(new Phrase(bulletin.getMentionGenerale(), NORMAL_FONT));
        summaryTable.addCell(new Phrase("Rang dans la classe :", BOLD_FONT));
        summaryTable.addCell(new Phrase(String.valueOf(bulletin.getRang()), NORMAL_FONT));
        summaryTable.addCell(new Phrase("Appréciation Générale :", BOLD_FONT));
        summaryTable.addCell(new Phrase(bulletin.getAppreciationGenerale(), NORMAL_FONT)); // Appréciation

        document.add(summaryTable);
        document.add(new Paragraph(" "));
    }

    /**
     * Ajoute les informations sur les absences.
     * @param document Le document PDF.
     * @param bulletin L'objet BulletinEtudiant.
     * @throws DocumentException
     */
    private static void addAbsenceDetails(Document document, BulletinEtudiant bulletin) throws DocumentException {
        document.add(new Paragraph("Informations sur les Absences", SUBHEADER_FONT));
        document.add(new Paragraph(" "));

        PdfPTable absenceSummaryTable = new PdfPTable(2);
        absenceSummaryTable.setWidthPercentage(100);
        absenceSummaryTable.setSpacingBefore(10f);
        absenceSummaryTable.setSpacingAfter(10f);
        absenceSummaryTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        absenceSummaryTable.addCell(new Phrase("Total des absences :", BOLD_FONT));
        absenceSummaryTable.addCell(new Phrase(String.valueOf(bulletin.getTotalAbsences()), NORMAL_FONT));
        absenceSummaryTable.addCell(new Phrase("Absences justifiées :", BOLD_FONT));
        absenceSummaryTable.addCell(new Phrase(String.valueOf(bulletin.getJustifiedAbsences()), NORMAL_FONT));
        absenceSummaryTable.addCell(new Phrase("Absences injustifiées :", BOLD_FONT));
        absenceSummaryTable.addCell(new Phrase(String.valueOf(bulletin.getUnjustifiedAbsences()), NORMAL_FONT));

        document.add(absenceSummaryTable);
        document.add(new Paragraph(" "));

        document.add(new Paragraph("Détail des absences :", SUBHEADER_FONT));
        if (bulletin.getDetailedAbsences().isEmpty()) {
            document.add(new Paragraph("Aucune absence enregistrée.", NORMAL_FONT));
        } else {
            for (String detail : bulletin.getDetailedAbsences()) {
                document.add(new Paragraph("• " + detail, NORMAL_FONT));
            }
        }
        document.add(new Paragraph(" "));
    }

    /**
     * Méthode utilitaire pour ajouter une cellule à un tableau PDF.
     * @param table Le tableau PDF.
     * @param text Le texte de la cellule.
     * @param font La police à utiliser.
     * @param backgroundColor La couleur de fond de la cellule (peut être null).
     * @param alignment L'alignement du texte dans la cellule.
     */
    private static void addCell(PdfPTable table, String text, Font font, BaseColor backgroundColor, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        if (backgroundColor != null) {
            cell.setBackgroundColor(backgroundColor);
        }
        table.addCell(cell);
    }
}
