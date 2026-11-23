package ma.ensah.pfe.services;

import ma.ensah.pfe.models.*;
import java.util.ArrayList;
import java.util.List;

public class PlanningService {
    
    // on prepare le validateur pour apres
    ValidateurContraintes validateur = new ValidateurContraintes();

    public List<Soutenance> genererPlanning(List<Etudiant> etudiants, List<Professeur> profs, List<Salle> salles, List<Creneau> creneaux) 
    {
       List<Soutenance> planning = new ArrayList<>();
       List<Etudiant> etudiantsTraites = new ArrayList<>();
        
       // boucle sur tout  
        for (Etudiant e : etudiants) {
            if(etudiantsTraites.contains(e)) continue; // Deja planifie avec son binome
            
            etudiantsTraites.add(e);
            if(e.getBinome() != null) etudiantsTraites.add(e.getBinome());
            
           // nouvelle soutenance (l'encadrant est ajouté automatiquement grace au modele)
            Soutenance s = new Soutenance(e);
            
           // 1.On cherche une place ideal (Le prof est libre + repos)
            boolean placeTrouvee = chercherPlace(s, e, creneaux, salles, planning, true);
            
            // 2.on n'a pas trouvé de place avec repos on cherche place libre 
            if(!placeTrouvee) {
                chercherPlace(s, e, creneaux, salles, planning, false);
            }
            
            // logique d'ajout des membres du jury
            // Au lieu de prendre au hasard. trie les profs par leur nombre de soutenances
            List<Professeur> profsMelanges = new java.util.ArrayList<>(profs);
            java.util.Collections.shuffle(profsMelanges); // on mélange d'abord pour éviter l'ordre alphabétique en cas d'égalité
            profsMelanges.sort((p1, p2) -> {
                int charge1 = compterSoutenances(p1, planning);
                int charge2 = compterSoutenances(p2, planning);
                return Integer.compare(charge1, charge2);
            });
            
            // Si PFE en anglais on cherche un prof d'anglais libre
            boolean anglaisTrouve = false;
            if(e.isPfeEnAnglais()) {
                // Passe 1 : Avec Repos
                for(int j=0; j<profsMelanges.size(); j++) {
                    Professeur p = profsMelanges.get(j);
                    boolean isAnglais = p.getDiscipline() != null && p.getDiscipline().toLowerCase().contains("anglais");
                    if(isAnglais && profEstLibre(p, s.getCreneau(), planning) && profAAssezDeRepos(p, s.getCreneau(), planning)) {
                        s.ajouterMembreJury(p);
                        anglaisTrouve = true;
                        break;
                    }
                }
                // Sans Repos
                if(!anglaisTrouve) {
                    for(int j=0; j<profsMelanges.size(); j++) {
                        Professeur p = profsMelanges.get(j);
                        boolean isAnglais = p.getDiscipline() != null && p.getDiscipline().toLowerCase().contains("anglais");
                        if(isAnglais && profEstLibre(p, s.getCreneau(), planning)) {
                            s.ajouterMembreJury(p);
                            break;
                        }
                    }
                }
            }
            
            // On boucle le reste des profs pour completer a 3 membres (AVEC REPOS)
            for(int j=0; j<profsMelanges.size(); j++) {
                Professeur p = profsMelanges.get(j);
                if(s.getJury().size() >= 3) break;
                
                if(!s.getJury().contains(p) && profEstLibre(p, s.getCreneau(), planning) && profAAssezDeRepos(p, s.getCreneau(), planning)) {
                    ConfigService config = ConfigService.getInstance();
                    String specRequise = config.getString("jury.specialite.requise", "info");
                    int minProfs = config.getInt("jury.minimum.profs.specialite", 2);
                    boolean prioriserEquite = config.getString("jury.prioriser.equite", "true").equalsIgnoreCase("true");
                    
                    if (!prioriserEquite) {
                        int nbSpecs = 0;
                        for (Professeur membre : s.getJury()) {
                            if (membre.getDiscipline() != null && membre.getDiscipline().toLowerCase().contains(specRequise)) nbSpecs++;
                        }
                        boolean isSpec = p.getDiscipline() != null && p.getDiscipline().toLowerCase().contains(specRequise);
                        if (nbSpecs < minProfs && !isSpec) continue;
                    }
                    
                    s.ajouterMembreJury(p);
                }
            }
            
            //(SANS REPOS)
            for(int j=0; j<profsMelanges.size(); j++) {
                Professeur p = profsMelanges.get(j);
                if(s.getJury().size() >= 3) break;
                
                if(!s.getJury().contains(p) && profEstLibre(p, s.getCreneau(), planning)) {
                    ConfigService config = ConfigService.getInstance();
                    String specRequise = config.getString("jury.specialite.requise", "info");
                    int minProfs = config.getInt("jury.minimum.profs.specialite", 2);
                    boolean prioriserEquite = config.getString("jury.prioriser.equite", "true").equalsIgnoreCase("true");
                    
                    if (!prioriserEquite) {
                        int nbSpecs = 0;
                        for (Professeur membre : s.getJury()) {
                            if (membre.getDiscipline() != null && membre.getDiscipline().toLowerCase().contains(specRequise)) nbSpecs++;
                        }
                        boolean isSpec = p.getDiscipline() != null && p.getDiscipline().toLowerCase().contains(specRequise);
                        if (nbSpecs < minProfs && !isSpec) continue;
                    }
                    
                    s.ajouterMembreJury(p);
                }
            }
            
            planning.add(s);
        }
        
       return planning;
    }
    
    // Fonction qui verifie si un prof est libre 
    private boolean profEstLibre(Professeur p, Creneau c, List<Soutenance> planningCourant) {
        if(c == null) return false;
        
        for(int i=0; i < planningCourant.size(); i++) {
            Soutenance soutenance = planningCourant.get(i);
            
            // si le prof est dans un jury a la meme heur il n'est pas libre 
            if(soutenance.getCreneau() != null && soutenance.getCreneau().equals(c)) {
                if(soutenance.getJury().contains(p)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    // Verifie si le prof a au moins 1h de repos par rapport au creneau c
    private boolean profAAssezDeRepos(Professeur p, Creneau c, List<Soutenance> planningCourant) {
        if(c == null || p == null) return true;
        for(int i=0; i < planningCourant.size(); i++) {
            Soutenance s = planningCourant.get(i);
            if(s.getCreneau() != null && s.getJury().contains(p)) {
                if(s.getCreneau().getDate().equals(c.getDate())) {
                    long diffMin = Math.abs(java.time.Duration.between(s.getCreneau().getHeureDebut(), c.getHeureDebut()).toMinutes());
                    int reposRequis = ConfigService.getInstance().getInt("repos.minimum.minutes", 60);
                    if(diffMin <= reposRequis) return false; // Il a une soutenance directement avant
                }
            }
        }
        return true;
    }

    private boolean chercherPlace(Soutenance s, Etudiant e, List<Creneau> creneaux, List<Salle> salles, List<Soutenance> planning, boolean exigerRepos) {
        // On melange les creneaux pour forcer l'etalement sur les 4,5 jours
        List<Creneau> creneauxMelanges = new java.util.ArrayList<>(creneaux);
        java.util.Collections.shuffle(creneauxMelanges);
        
        for(int j=0; j < creneauxMelanges.size(); j++) {
            Creneau c = creneauxMelanges.get(j);
            for(int k=0; k < salles.size(); k++) {
                Salle laSalle = salles.get(k);
                boolean estLibre = true;
                
                for(int l=0; l < planning.size(); l++) {
                    Soutenance dejaPlacee = planning.get(l);
                    if(dejaPlacee.getSalle() != null && dejaPlacee.getCreneau() != null) {
                        if(dejaPlacee.getSalle().equals(laSalle) && dejaPlacee.getCreneau().equals(c)) {
                            estLibre = false; break;
                        }
                    }
                }
                
                if (estLibre && e.getEncadrant() != null) {
                    if (!profEstLibre(e.getEncadrant(), c, planning)) estLibre = false;
                    else if(exigerRepos && !profAAssezDeRepos(e.getEncadrant(), c, planning)) estLibre = false;
                }
                
                if (estLibre && e.getBinome() != null && e.getBinome().getEncadrant() != null) {
                    if (!profEstLibre(e.getBinome().getEncadrant(), c, planning)) estLibre = false;
                    else if(exigerRepos && !profAAssezDeRepos(e.getBinome().getEncadrant(), c, planning)) estLibre = false;
                }
                
                if(estLibre) {
                    s.setCreneau(c);
                    s.setSalle(laSalle);
                    return true;
                }
            }
        }
        return false;
    }

    // Compte combien de soutenances a le prof a l'instant T
    private int compterSoutenances(Professeur p, List<Soutenance> planningCourant) {
        int count = 0;
        for (Soutenance s : planningCourant) {
            if (s.getJury().contains(p)) count++;
        }
        return count;
    }
}
