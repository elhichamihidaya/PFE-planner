package ma.ensah.pfe.models;

public class Etudiant {
    private String cne;
    private String nom;
    private String prenom;
    private String email; 
    private String filiere; // GI, ID, TDIA
    private boolean pfeEnAnglais;
    private Professeur encadrant;
    private Etudiant binome; // Gestion des binomes

    public Etudiant(String cne, String nom, String prenom, String email, String filiere, boolean pfeEnAnglais, Professeur encadrant) {
        this.cne = cne;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.filiere = filiere;
        this.pfeEnAnglais = pfeEnAnglais;
        this.encadrant = encadrant;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCne() { return cne; }
    public void setCne(String cne) { this.cne = cne; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getFiliere() { return filiere; }
    public void setFiliere(String filiere) { this.filiere = filiere; }

    public boolean isPfeEnAnglais() { return pfeEnAnglais; }
    public void setPfeEnAnglais(boolean pfeEnAnglais) { this.pfeEnAnglais = pfeEnAnglais; }

    public Professeur getEncadrant() { return encadrant; }
    public void setEncadrant(Professeur encadrant) { this.encadrant = encadrant; }

    public Etudiant getBinome() { return binome; }
    public void setBinome(Etudiant binome) { this.binome = binome; }

    @Override
    public String toString() {
        return nom + " " + prenom + " (" + filiere + ")";
    }
}
