package ma.ensah.pfe.services;

import ma.ensah.pfe.models.*;
import java.time.Duration;

public class ValidateurContraintes {

    private static final long DURATION_MIN_ENTRE_SOUTENANCES = ConfigService.getInstance().getInt("repos.minimum.minutes", 60);

  // meme salle ??
    public boolean salleConstraint(Soutenance s1, Soutenance s2) {
       if(s1.getSalle() == null || s2.getSalle() == null) return false;
        
     boolean memeSalle = s1.getSalle().equals(s2.getSalle());
       boolean memeCreneau= s1.getCreneau().equals(s2.getCreneau());
        
      return memeSalle && memeCreneau;
    }

   // prof dans 2 salles ..
    public boolean professeurConstraint(Soutenance s1, Soutenance s2) {
        if (s1.getCreneau() == null || s2.getCreneau() == null) return false;
        
        if (!s1.getCreneau().equals(s2.getCreneau())) {
          return false; // pas le meme temps
        }
        
       for (int i = 0; i < s1.getJury().size(); i++) {
            Professeur p = s1.getJury().get(i);
          if (s2.getJury().contains(p)) {
              return true; 
          }
       }
     return false;
    }
    
    // verifie back to back 
    public Professeur profReposConstraint(Soutenance s1, Soutenance s2) 
    {
        if (s1.getCreneau() == null || s2.getCreneau() == null) return null;
        
        if (s1.getCreneau().getDate().equals(s2.getCreneau().getDate())) {
            
           long diffMin = Math.abs(Duration.between(s1.getCreneau().getHeureDebut(), s2.getCreneau().getHeureDebut()).toMinutes());
            
           if (diffMin > 0 && diffMin <= DURATION_MIN_ENTRE_SOUTENANCES) {
             // au moins reposConfig entre les deux 
              for(Professeur p : s1.getJury()) {
                 if(s2.getJury().contains(p)) {
                     return p; 
                 }
              }
           }
        }
       return null;
    }

    public boolean valideJury(Soutenance s) {
       return s.estComplete();
    }
    
    // quota configuré
    public boolean verifierQuotaInformatique(Soutenance s) {
       ConfigService config = ConfigService.getInstance();
       String specialiteRequise = config.getString("jury.specialite.requise", "info");
       int minProfs = config.getInt("jury.minimum.profs.specialite", 2);
       
       int nbSpecs = 0;
        for(int i=0; i<s.getJury().size(); i++){
            Professeur p = s.getJury().get(i);
            if(p.getDiscipline() != null && p.getDiscipline().toLowerCase().contains(specialiteRequise)) {
              nbSpecs++;
            }
        }
       return nbSpecs >= minProfs;
    }
    
    // pfe en anglais -> prof anglais
    public boolean verifierProfAnglais(Soutenance s) {
        if(s.getEtudiant().isPfeEnAnglais()) {
           boolean hasAnglais = false;
            for(int i=0; i<s.getJury().size(); i++){
                Professeur p = s.getJury().get(i);
                if(p.getDiscipline() != null && p.getDiscipline().toLowerCase().contains("anglais")) {
                    hasAnglais = true;
                }
            }
          return hasAnglais;
        }
       return true; // ok
    }

}
