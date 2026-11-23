package ma.ensah.pfe.services;

import ma.ensah.pfe.models.*;
import java.util.ArrayList;
import java.util.List;

public class VerificationService {
    
   ValidateurContraintes validateur;
    
    public VerificationService() {
        this.validateur = new ValidateurContraintes();
    }
    
    public List<String> verifierConstraint(List<Soutenance> soutenances) 
    {
       List<String> violations = new ArrayList<>();
        
       for (int i = 0; i < soutenances.size(); i++) {
           Soutenance s1 = soutenances.get(i);
            
         // verifier s'il manque qq chose
            if(!s1.estComplete()) {
               violations.add("Soutenance incomplete (prof ou salle manquant) pour " + s1.getEtudiant().getNom());
            } 
            else {
               if(!validateur.verifierQuotaInformatique(s1)) {
                  violations.add("Erreur: quota prof informatique non respecte pour " + s1.getEtudiant().getNom());
               }
                
               if(!validateur.verifierProfAnglais(s1)) {
                   violations.add("Attention: prof anglais manquant pour PFE de " + s1.getEtudiant().getNom());
               }
            }

           // verification des conflits avec les autres soutenances
           for(int j = i + 1; j < soutenances.size(); j++) {
              Soutenance s2 = soutenances.get(j);
                
              if (validateur.salleConstraint(s1, s2)) {
                   violations.add("Conflit de salle: " + s1.getEtudiant().getNom() + " et " + s2.getEtudiant().getNom());
              }
                
               if(validateur.professeurConstraint(s1, s2)) {
                   violations.add("Conflit horaire prof entre " + s1.getEtudiant().getNom() + " et " + s2.getEtudiant().getNom());
               }
                
               Professeur profFatigue = validateur.profReposConstraint(s1, s2);
               if(profFatigue != null) {
                   violations.add("Repos insuffisant pour " + profFatigue.getNom() + " (entre " + s1.getEtudiant().getNom() + " et " + s2.getEtudiant().getNom() + ")");
               }
           }
       }
        
       return violations;
    }
}
