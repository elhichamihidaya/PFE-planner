package ma.ensah.pfe.io;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import ma.ensah.pfe.models.Soutenance;
import ma.ensah.pfe.models.Professeur;
import java.io.FileOutputStream;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class PdfExportService {

    private Map<String, BaseColor> profColors = new HashMap<>();
    
    // Generer une couleur pastel unique basee sur le nom
    private BaseColor getColorForProf(String nomProf) {
        if (nomProf == null || nomProf.isEmpty()) return BaseColor.WHITE;
        if (profColors.containsKey(nomProf)) {
            return profColors.get(nomProf);
        }
        
        // Hasher le nom pour generer RGB
        int hash = nomProf.hashCode();
        int r = (hash & 0xFF0000) >> 16;
        int g = (hash & 0x00FF00) >> 8;
        int b = hash & 0x0000FF;
        
        // Rendre la couleur claire (pastel) pour que le texte noir soit lisible
        r = (r + 255) / 2;
        g = (g + 255) / 2;
        b = (b + 255) / 2;
        
        // Quelques couleurs forcees pour coller à l'image si possible
        if (nomProf.contains("BADI")) { r=46; g=204; b=113; } // Vert
        if (nomProf.contains("BOUJRAF")) { r=241; g=196; b=15; } // Jaune
        if (nomProf.contains("BOUHAFER")) { r=238; g=130; b=238; } // Violet
        if (nomProf.contains("ADDAM")) { r=189; g=195; b=199; } // Gris
        
        BaseColor color = new BaseColor(r, g, b);
        profColors.put(nomProf, color);
        return color;
    }

    public void exporterPlanningPDF(List<Soutenance> planning, String filePath) {
        Document document = new Document(PageSize.A4.rotate(), 10, 10, 20, 20); // Paysage
        
        try {
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();
            
            // TITRES
            Font fontTitre = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
            Font fontSousTitre = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
            
            Paragraph p1 = new Paragraph("Ecole Nationale des Sciences Appliquées - Al Hoceima", fontTitre);
            p1.setAlignment(Element.ALIGN_CENTER);
            document.add(p1);
            
            Paragraph p2 = new Paragraph("Département Mathématiques et Informatique", fontTitre);
            p2.setAlignment(Element.ALIGN_CENTER);
            document.add(p2);
            
            Paragraph p3 = new Paragraph("Planning des soutenances des Projets de Fin d'Etude\n(Première Session)\nAnnée Universitaire 2025/2026", fontSousTitre);
            p3.setAlignment(Element.ALIGN_CENTER);
            p3.setSpacingAfter(15);
            document.add(p3);
            
            // TABLEAU
            PdfPTable table = new PdfPTable(10);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.5f, 4f, 4f, 4f, 3f, 2f, 2f, 4f, 4f, 2f});
            
            // En-tetes
            String[] headers = {"ID", "Encadrant", "Membre de jury 1", "Membre de jury 2", "Date", "Heure", "Salle", "Nom d'étudiant", "Prénom d'étudiant", "Filière"};
            Font fontHeader = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);
            
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, fontHeader));
                cell.setBackgroundColor(BaseColor.BLACK);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setPadding(5);
                table.addCell(cell);
            }
            
            Font fontCell = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL);
            BaseColor colJaune = new BaseColor(255, 255, 0);
            BaseColor colBleuClair = new BaseColor(173, 216, 230);
            BaseColor colGris = new BaseColor(220, 220, 220);
            
            int idCounter = 1;
            
            for (Soutenance s : planning) {
                // Determine row span based on binome
                int rowSpan = s.getEtudiant().getBinome() != null ? 2 : 1;
                
                // --- ID ---
                PdfPCell cellId = new PdfPCell(new Phrase(String.valueOf(idCounter++), fontCell));
                cellId.setRowspan(rowSpan);
                table.addCell(cellId);
                
                // --- Encadrant ---
                String nomEncadrant = s.getEtudiant().getEncadrant() != null ? s.getEtudiant().getEncadrant().getNom() + " " + s.getEtudiant().getEncadrant().getPrenom() : "";
                PdfPCell cellEncadrant = new PdfPCell(new Phrase(nomEncadrant, fontCell));
                cellEncadrant.setBackgroundColor(getColorForProf(nomEncadrant));
                cellEncadrant.setRowspan(rowSpan);
                table.addCell(cellEncadrant);
                
                // On isole les membres du jury qui ne sont pas l'encadrant principal
                java.util.List<Professeur> membresJury = new java.util.ArrayList<>();
                for (Professeur p : s.getJury()) {
                    if (s.getEtudiant().getEncadrant() == null || !p.equals(s.getEtudiant().getEncadrant())) {
                        membresJury.add(p);
                    }
                }
                
                // --- Jury 1 ---
                String jury1 = membresJury.size() > 0 ? membresJury.get(0).getNom() + " " + membresJury.get(0).getPrenom() : "";
                PdfPCell cellJ1 = new PdfPCell(new Phrase(jury1, fontCell));
                cellJ1.setBackgroundColor(getColorForProf(jury1));
                cellJ1.setRowspan(rowSpan);
                table.addCell(cellJ1);
                
                // --- Jury 2 ---
                String jury2 = membresJury.size() > 1 ? membresJury.get(1).getNom() + " " + membresJury.get(1).getPrenom() : "";
                PdfPCell cellJ2 = new PdfPCell(new Phrase(jury2, fontCell));
                cellJ2.setBackgroundColor(getColorForProf(jury2));
                cellJ2.setRowspan(rowSpan);
                table.addCell(cellJ2);
                
                // --- Date ---
                String dateStr = s.getCreneau() != null ? s.getCreneau().getDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
                PdfPCell cellDate = new PdfPCell(new Phrase(dateStr, fontCell));
                cellDate.setBackgroundColor(colJaune);
                cellDate.setRowspan(rowSpan);
                table.addCell(cellDate);
                
                // --- Heure ---
                String heureStr = s.getCreneau() != null ? s.getCreneau().getHeureDebut().toString() : "";
                PdfPCell cellHeure = new PdfPCell(new Phrase(heureStr, fontCell));
                cellHeure.setBackgroundColor(colJaune);
                cellHeure.setRowspan(rowSpan);
                table.addCell(cellHeure);
                
                // --- Salle ---
                String nomSalle = s.getSalle() != null ? s.getSalle().getNom() : "";
                PdfPCell cellSalle = new PdfPCell(new Phrase(nomSalle, fontCell));
                cellSalle.setBackgroundColor(colGris);
                cellSalle.setRowspan(rowSpan);
                table.addCell(cellSalle);
                
                // --- Etudiant 1 ---
                PdfPCell cellNom1 = new PdfPCell(new Phrase(s.getEtudiant().getNom(), fontCell));
                cellNom1.setBackgroundColor(colBleuClair);
                table.addCell(cellNom1);
                
                PdfPCell cellPrenom1 = new PdfPCell(new Phrase(s.getEtudiant().getPrenom(), fontCell));
                cellPrenom1.setBackgroundColor(colBleuClair);
                table.addCell(cellPrenom1);
                
                PdfPCell cellFil1 = new PdfPCell(new Phrase(s.getEtudiant().getFiliere(), fontCell));
                cellFil1.setBackgroundColor(colBleuClair);
                table.addCell(cellFil1);
                
                // --- Binome (si existe) ---
                if (rowSpan == 2) {
                    PdfPCell cellNom2 = new PdfPCell(new Phrase(s.getEtudiant().getBinome().getNom(), fontCell));
                    cellNom2.setBackgroundColor(colBleuClair);
                    table.addCell(cellNom2);
                    
                    PdfPCell cellPrenom2 = new PdfPCell(new Phrase(s.getEtudiant().getBinome().getPrenom(), fontCell));
                    cellPrenom2.setBackgroundColor(colBleuClair);
                    table.addCell(cellPrenom2);
                    
                    PdfPCell cellFil2 = new PdfPCell(new Phrase(s.getEtudiant().getBinome().getFiliere(), fontCell));
                    cellFil2.setBackgroundColor(colBleuClair);
                    table.addCell(cellFil2);
                }
            }
            
            document.add(table);
            document.close();
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors de la generation du PDF : " + e.getMessage());
        }
    }

    public void exporterAffectationsPDF(List<Professeur> profs, List<ma.ensah.pfe.models.Etudiant> etudiants, String filePath) {
        Document document = new Document(PageSize.A4.rotate(), 10, 10, 20, 20); // Paysage
        
        try {
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();
            
            // TITRES
            Font fontTitre = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
            Font fontSousTitre = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
            
            Paragraph p1 = new Paragraph("Ecole Nationale des Sciences Appliquées - Al Hoceima", fontTitre);
            p1.setAlignment(Element.ALIGN_CENTER);
            document.add(p1);
            
            Paragraph p2 = new Paragraph("Département Mathématiques et Informatique", fontTitre);
            p2.setAlignment(Element.ALIGN_CENTER);
            document.add(p2);
            
            Paragraph p3 = new Paragraph("Affectation des encadrants de Projet de Fin d'Etude\nAnnée Universitaire 2025/2026", fontSousTitre);
            p3.setAlignment(Element.ALIGN_CENTER);
            p3.setSpacingAfter(10);
            document.add(p3);
            
            Font fontCell = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL);
            Font fontCellBold = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD);
            
            BaseColor colFiliereID = new BaseColor(255, 255, 153); // Jaune (Yellow)
            BaseColor colFiliereTDIA = new BaseColor(144, 238, 144); // Vert clair (Light Green)
            BaseColor colFiliereGI = new BaseColor(255, 182, 193); // Rose (Pink)
            
            // LEGENDE (Notes pour les couleurs)
            PdfPTable legendTable = new PdfPTable(3);
            legendTable.setWidthPercentage(40);
            legendTable.setHorizontalAlignment(Element.ALIGN_CENTER);
            legendTable.setSpacingAfter(15);
            
            PdfPCell cellLegendID = new PdfPCell(new Phrase("Filière ID", fontCell));
            cellLegendID.setBackgroundColor(colFiliereID);
            cellLegendID.setHorizontalAlignment(Element.ALIGN_CENTER);
            legendTable.addCell(cellLegendID);
            
            PdfPCell cellLegendTDIA = new PdfPCell(new Phrase("Filière TDIA", fontCell));
            cellLegendTDIA.setBackgroundColor(colFiliereTDIA);
            cellLegendTDIA.setHorizontalAlignment(Element.ALIGN_CENTER);
            legendTable.addCell(cellLegendTDIA);
            
            PdfPCell cellLegendGI = new PdfPCell(new Phrase("Filière GI", fontCell));
            cellLegendGI.setBackgroundColor(colFiliereGI);
            cellLegendGI.setHorizontalAlignment(Element.ALIGN_CENTER);
            legendTable.addCell(cellLegendGI);
            
            document.add(legendTable);
            
            // TABLEAU (10 colonnes)
            PdfPTable table = new PdfPTable(10);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2f, 2f, 2f, 2f, 2f, 2f, 2f, 2f, 2f, 2f});
            
            Font fontHeader = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.BLACK);
            BaseColor colorHeaderBlue = new BaseColor(0, 191, 255); // Bleu clair
            
            // Top Headers (Encadrant, Etudiants encadrés)
            PdfPCell cellHeaderEnc = new PdfPCell(new Phrase("Encadrant", fontHeader));
            cellHeaderEnc.setColspan(2);
            cellHeaderEnc.setBackgroundColor(colorHeaderBlue);
            cellHeaderEnc.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cellHeaderEnc);
            
            PdfPCell cellHeaderEtu = new PdfPCell(new Phrase("Etudiants encadrés", fontHeader));
            cellHeaderEtu.setColspan(8);
            cellHeaderEtu.setBackgroundColor(colorHeaderBlue);
            cellHeaderEtu.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cellHeaderEtu);
            
            // Sub Headers
            String[] subHeaders = {"Nom", "Prénom", "Etudiant 1", "Etudiant 2", "Etudiant 3", "Etudiant 4"};
            table.addCell(new PdfPCell(new Phrase("Nom", fontHeader)));
            table.addCell(new PdfPCell(new Phrase("Prénom", fontHeader)));
            for(int i=1; i<=4; i++) {
                PdfPCell subCell = new PdfPCell(new Phrase("Etudiant " + i, fontHeader));
                subCell.setColspan(2);
                subCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(subCell);
            }
            
            for (Professeur prof : profs) {
                // Find students for this prof
                java.util.List<ma.ensah.pfe.models.Etudiant> etudiantsDuProf = new java.util.ArrayList<>();
                for (ma.ensah.pfe.models.Etudiant e : etudiants) {
                    if (e.getEncadrant() != null && e.getEncadrant().equals(prof)) {
                        etudiantsDuProf.add(e);
                    }
                }
                
                table.addCell(new PdfPCell(new Phrase(prof.getNom(), fontCellBold)));
                table.addCell(new PdfPCell(new Phrase(prof.getPrenom(), fontCellBold)));
                
                for(int i=0; i<4; i++) {
                    if (i < etudiantsDuProf.size()) {
                        ma.ensah.pfe.models.Etudiant e = etudiantsDuProf.get(i);
                        BaseColor bgColor = BaseColor.WHITE;
                        if ("ID".equalsIgnoreCase(e.getFiliere())) bgColor = colFiliereID;
                        else if ("TDIA".equalsIgnoreCase(e.getFiliere())) bgColor = colFiliereTDIA;
                        else if ("GI".equalsIgnoreCase(e.getFiliere())) bgColor = colFiliereGI;
                        
                        PdfPCell cNom = new PdfPCell(new Phrase(e.getNom(), fontCell));
                        cNom.setBackgroundColor(bgColor);
                        table.addCell(cNom);
                        
                        PdfPCell cPrenom = new PdfPCell(new Phrase(e.getPrenom(), fontCell));
                        cPrenom.setBackgroundColor(bgColor);
                        table.addCell(cPrenom);
                    } else {
                        table.addCell(new PdfPCell(new Phrase("")));
                        table.addCell(new PdfPCell(new Phrase("")));
                    }
                }
            }

            
            document.add(table);
            document.close();
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors de la generation du PDF Affectations : " + e.getMessage());
        }
    }
}
