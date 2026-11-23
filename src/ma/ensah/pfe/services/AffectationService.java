package ma.ensah.pfe.services;

import ma.ensah.pfe.models.Etudiant;
import ma.ensah.pfe.models.Professeur;
import java.util.List;

public class AffectationService {

    // On distribue les etudiants un par un
    public void affecterAutomatiquement(List<Etudiant> etudiants, List<Professeur> profs) {
        if(profs.size() == 0 || etudiants.size() == 0) {
            System.out.println("Impossible de faire l'affectation, donnees manquantes.");
            return;
        }

        int indexProf = 0; // le premier prof
        
        for (int i = 0; i < etudiants.size(); i++) {
            Etudiant e = etudiants.get(i);
            Professeur p = profs.get(indexProf);
            
            e.setEncadrant(p);
            
            // on passe au prof suivant pour le prochain etudiant
            indexProf++;
            
            // si on a donné un etudiant au tout dernier prof de la liste, on recommence au premier !
            if (indexProf >= profs.size()) {
                indexProf = 0;
            }
        }
    }
}
