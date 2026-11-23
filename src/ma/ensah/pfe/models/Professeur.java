package ma.ensah.pfe.models;

import java.util.Objects;

public class Professeur {
    private String nom;
    private String prenom;
    private String discipline; // Informatique, Mathématique, Anglais
    private int chargeActuelle; // Nombre de soutenances où il est impliqué

    public Professeur(String nom, String prenom, String discipline) {
        this.nom = nom;
        this.prenom = prenom;
        this.discipline = discipline;
        this.chargeActuelle = 0;
    }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getDiscipline() { return discipline; }
    public void setDiscipline(String discipline) { this.discipline = discipline; }

    public int getChargeActuelle() { return chargeActuelle; }
    public void incrementerCharge() { this.chargeActuelle++; }
    public void decrementerCharge() { this.chargeActuelle--; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Professeur that = (Professeur) o;
        return Objects.equals(nom, that.nom) && Objects.equals(prenom, that.prenom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nom, prenom);
    }

    @Override
    public String toString() {
        return nom + " " + prenom + " (" + discipline + ")";
    }
}
