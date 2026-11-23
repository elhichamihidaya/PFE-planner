package ma.ensah.pfe.services;

import ma.ensah.pfe.models.Etudiant;
import ma.ensah.pfe.models.Professeur;
import java.util.List;

public class StatisticsService {
    
    // methode pour compter combien d'etudiants chaque prof encadre
    // on doit avoir entre 3 et 4 etudiants par prof normalement
    public void afficherStatistiquesEncadrement(List<Etudiant> listeEtu, List<Professeur> listeProf) {
        System.out.println("====== STATISTIQUES D'ENCADREMENT ======");
        
        int totalEtudiants = 0;
        int totalEncadrants = 0; // le nombre de profs qui encadrent 
        
        // on boucle sur les profs 
        for(int i=0; i < listeProf.size(); i++) {
            Professeur p = listeProf.get(i);
            int compteurEtu = 0;
            
            // on cherche tous les etudiants de ce prof
            for(int j=0; j < listeEtu.size(); j++) {
                Etudiant e = listeEtu.get(j);
                
                // des fois l'encadrant est nul si on a pas encore fait l'affectation
                if(e.getEncadrant() != null && e.getEncadrant().equals(p)) {
                    compteurEtu++;
                }
            }
            
            // on affiche que si le prof encadre quelqu'un
            if(compteurEtu > 0) {
                 totalEncadrants++;
                 totalEtudiants = totalEtudiants + compteurEtu;
                 
                 // regles metier (entre 3 et 4)
                 if(compteurEtu < 3) {
                     System.out.println("ATTENTION ! " + p.getNom() + " n'encadre que " + compteurEtu + " etudiant(s) -> (SOUS CHARGE)");
                 } else if(compteurEtu > 4) {
                     System.out.println("ALERTE ROUGE !! " + p.getNom() + " encadre " + compteurEtu + " etudiants -> (SURCHARGE)");
                 } else {
                     System.out.println("OK : " + p.getNom() + " -> " + compteurEtu + " etudiants");
                 }
            }
        }
        
        // on evite la division par zero
        if(totalEncadrants > 0) {
           double moyenne = (double) totalEtudiants / totalEncadrants;
           System.out.println("========================================");
           System.out.println("Moyenne generale d'encadrement : " + String.format("%.2f", moyenne) + " etudiants / prof");
           System.out.println("========================================");
        }
    }
    
    // Génère un rapport textuel complet pour le Dashboard UI
    public String genererRapportComplet(List<Etudiant> listeEtu, List<Professeur> listeProf, java.util.List<ma.ensah.pfe.models.Soutenance> planning) {
        StringBuilder sb = new StringBuilder();
        sb.append("====================================================\n");
        sb.append("       TABLEAU DE BORD - STATISTIQUES CLES          \n");
        sb.append("====================================================\n\n");
        
        // 1. Nombre d'étudiants encadrés par professeur
        sb.append("1. NOMBRE D'ÉTUDIANTS ENCADRÉS PAR PROFESSEUR :\n");
        sb.append("(Conformité: entre 3 et 4 étudiants par prof)\n");
        for(Professeur p : listeProf) {
            int countEtu = 0;
            for(Etudiant e : listeEtu) {
                if(e.getEncadrant() != null && e.getEncadrant().equals(p)) countEtu++;
            }
            if(countEtu > 0) {
                if(countEtu > 4) sb.append(" [ALERTE SURCHARGE] ");
                else if(countEtu < 3) sb.append(" [SOUS CHARGE]      ");
                else sb.append(" [OK]               ");
                sb.append(p.getNom() + " : " + countEtu + " étudiant(s)\n");
            }
        }
        
        sb.append("\n----------------------------------------------------\n");
        
        // 2. Nombre de soutenances par professeur (Jury)
        sb.append("2. NOMBRE TOTAL DE SOUTENANCES PAR PROFESSEUR :\n");
        sb.append("(Combien de fois le prof devra-t-il siéger dans un jury ?)\n");
        for(Professeur p : listeProf) {
            int countSout = 0;
            if (planning != null) {
                for(ma.ensah.pfe.models.Soutenance s : planning) {
                    if(s.getJury().contains(p)) countSout++;
                }
            }
            if(countSout > 0) {
                sb.append(" - Prof. " + p.getNom() + " participera à " + countSout + " soutenance(s)\n");
            }
        }
        
        sb.append("\n----------------------------------------------------\n");
        
        // 3. Nombre de soutenances par filière
        sb.append("3. VOLUME DE SOUTENANCES PAR FILIÈRE :\n");
        int countGI = 0, countID = 0, countTDIA = 0, countAutre = 0;
        if (planning != null) {
            for(ma.ensah.pfe.models.Soutenance s : planning) {
                String filiere = s.getEtudiant().getFiliere();
                if(filiere.equalsIgnoreCase("GI")) countGI++;
                else if(filiere.equalsIgnoreCase("ID")) countID++;
                else if(filiere.equalsIgnoreCase("TDIA")) countTDIA++;
                else countAutre++;
            }
        }
        sb.append(" -> Génie Informatique (GI) : " + countGI + " soutenances\n");
        sb.append(" -> Ingénierie des Données (ID) : " + countID + " soutenances\n");
        sb.append(" -> Technologie Digitale et IA (TDIA) : " + countTDIA + " soutenances\n");
        if(countAutre > 0) sb.append(" -> Autres : " + countAutre + " soutenances\n");
        
        sb.append("\n====================================================\n");
        return sb.toString();
    }
}
