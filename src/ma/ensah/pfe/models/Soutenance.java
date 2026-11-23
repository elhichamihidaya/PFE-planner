package ma.ensah.pfe.models;

import java.util.ArrayList;
import java.util.List;

public class Soutenance {
    private Etudiant etudiant;
    private Etudiant binome;
    private Salle salle;
    private Creneau creneau;
    private List<Professeur> jury;

    public Soutenance(Etudiant etudiant) {
        this.etudiant = etudiant;
        this.binome = etudiant.getBinome();
        this.jury = new ArrayList<>();
        // Le premier membre du jury doit toujours etre lencadrant
        if(etudiant != null && etudiant.getEncadrant() != null) {
            ajouterMembreJury(etudiant.getEncadrant());
        }
        // Si le binome a un encadrant different on lajoute aussi au jury (co-encadrement)
        if(binome != null && binome.getEncadrant() != null) {
            ajouterMembreJury(binome.getEncadrant());
        }
    }

    public Etudiant getEtudiant() { return etudiant; }
    public Etudiant getBinome() { return binome; }
    public void setEtudiant(Etudiant etudiant) { this.etudiant = etudiant; }

    public Salle getSalle() { return salle; }
    public void setSalle(Salle salle) { this.salle = salle; }

    public Creneau getCreneau() { return creneau; }
    public void setCreneau(Creneau creneau) { this.creneau = creneau; }

    public List<Professeur> getJury() { return jury; }
    
    public boolean ajouterMembreJury(Professeur p) {
        if (!jury.contains(p) && jury.size() < 3) {
            jury.add(p);
            p.incrementerCharge();
            return true;
        }
        return false;
    }

    public void retirerMembreJury(Professeur p) {
        // On ne retire pas l'encadrant qui est à l'index 0
        if (jury.size() > 1 && jury.contains(p) && !p.equals(etudiant.getEncadrant())) {
            jury.remove(p);
            p.decrementerCharge();
        }
    }

    public boolean estComplete() {
        return salle != null && creneau != null && jury.size() == 3;
    }

    @Override
    public String toString() {
        return "Soutenance de " + etudiant.getNom() + " - " + 
               (creneau != null ? creneau.toString() : "Non planifiée");
    }
}
