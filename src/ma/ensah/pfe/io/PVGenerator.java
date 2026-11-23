package ma.ensah.pfe.io;

import ma.ensah.pfe.models.Etudiant;
import ma.ensah.pfe.models.Professeur;
import ma.ensah.pfe.models.Soutenance;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

// librairie Word d'Apache POI
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

public class PVGenerator {

    // On lit le fichier modele Word (.docx), on remplace les balises, et on sauvegarde 
    public void genererPVDocx(Soutenance soutenance, Etudiant etudiantCible, String cheminTemplate, String dossierDestination) {
        if(soutenance == null || etudiantCible == null) {
            System.out.println("Erreur: Soutenance vide, generation annulee.");
            return;
        }
        
        try {
            FileInputStream fileIn = new FileInputStream(cheminTemplate);
            XWPFDocument document = new XWPFDocument(fileIn);
            
            // 2. On traite les paragraphes normaux
            for (XWPFParagraph p : document.getParagraphs()) {
                remplacerTexteDansParagraphe(p, soutenance, etudiantCible);
            }
            
            // 2.Bis On traite les tableaux (car le fichier de l'administration contient souvent des tableaux !)
            for (org.apache.poi.xwpf.usermodel.XWPFTable tbl : document.getTables()) {
                for (org.apache.poi.xwpf.usermodel.XWPFTableRow row : tbl.getRows()) {
                    for (org.apache.poi.xwpf.usermodel.XWPFTableCell cell : row.getTableCells()) {
                        for (XWPFParagraph p : cell.getParagraphs()) {
                            remplacerTexteDansParagraphe(p, soutenance, etudiantCible);
                        }
                    }
                }
            }
            
            //  On cree le dossier de destination s'il n'existe pas
            File dossier = new File(dossierDestination);
            if(!dossier.exists()) {
                dossier.mkdirs(); // magie !
            }
            
            // On sauvegarde le nouveau fichier Word officiel
            String nomFichier = "PV_" + etudiantCible.getNom() + "_" + etudiantCible.getPrenom() + ".docx";
            File nouveauFichier = new File(dossier, nomFichier);
            
            FileOutputStream fileOut = new FileOutputStream(nouveauFichier);
            document.write(fileOut);
            
            // fermer fichs
            fileOut.close();
            document.close();
            fileIn.close();
            
            System.out.println("PV Word cree avec succes : " + nomFichier);
            
        } catch (Exception e) {
            System.out.println("probleme avec la generation du PV Word : " + e.getMessage());
        }
    }
    
    private void remplacerTexteDansParagraphe(XWPFParagraph p, Soutenance soutenance, Etudiant etudiantCible) {
        String texte = p.getText();
        if (texte == null || !texte.contains("$")) return;

        String nomComplet = etudiantCible.getNom() + " " + etudiantCible.getPrenom();
        
        String jury1 = soutenance.getJury().get(0).getNom() + " " + soutenance.getJury().get(0).getPrenom();
        String jury2 = soutenance.getJury().get(1).getNom() + " " + soutenance.getJury().get(1).getPrenom();
        String jury3 = soutenance.getJury().get(2).getNom() + " " + soutenance.getJury().get(2).getPrenom();
        
        String dateText = soutenance.getCreneau().getDate().toString();
        String heureText = soutenance.getCreneau().getHeureDebut().toString();
        String salleText = soutenance.getSalle().getNom();
        String nomEncadrant = jury1; // l'encadrant est toujours le premier
        String filiere = etudiantCible.getFiliere();

        String replacedText = texte.replace("$NOM_ETU", nomComplet)
                                   .replace("$FILIERE", filiere)
                                   .replace("$SALLE", salleText)
                                   .replace("$DATE", dateText)
                                   .replace("$HEURE", heureText)
                                   .replace("$ENCADRANT", nomEncadrant)
                                   .replace("$JURY_1", jury1)
                                   .replace("$JURY_2", jury2)
                                   .replace("$JURY_3", jury3);

        if (!replacedText.equals(texte)) {
            // On sauvegarde le style du premier morceau AVANT de le supprimer 
            String fontFamily = null;
            int fontSize = -1;
            boolean isBold = false;
            boolean isItalic = false;
            
            if (p.getRuns().size() > 0) {
                org.apache.poi.xwpf.usermodel.XWPFRun firstRun = p.getRuns().get(0);
                try { fontFamily = firstRun.getFontFamily(); } catch(Exception e) {}
                try { fontSize = firstRun.getFontSize(); } catch(Exception e) {}
                try { isBold = firstRun.isBold(); } catch(Exception e) {}
                try { isItalic = firstRun.isItalic(); } catch(Exception e) {}
            }
            
            // On supprime tous les anciens morceaux
            for (int k = p.getRuns().size() - 1; k >= 0; k--) {
                p.removeRun(k);
            }
            
            // On recrée un morceau propre avec le texte remplacé
            org.apache.poi.xwpf.usermodel.XWPFRun newRun = p.createRun();
            newRun.setText(replacedText, 0);
            
            if (fontFamily != null) {
                newRun.setFontFamily(fontFamily);
            }
            if (fontSize != -1) {
                newRun.setFontSize(fontSize);
            }
            newRun.setBold(isBold);
            newRun.setItalic(isItalic);
        }
    }
}
