package ma.ensah.pfe.ui;

import javax.swing.*;
import java.awt.*;

public class PanneauCartes extends JPanel {
    
    private JLabel lblTotalSoutenances;
    private JLabel lblTotalProfs;
    private JLabel lblSalles;
    
    public PanneauCartes() {
        setLayout(new GridLayout(1, 3, 20, 0)); // 1 ligne, 3 colonnes avec espace
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        lblTotalSoutenances = new JLabel("0", SwingConstants.CENTER);
        lblTotalSoutenances.setFont(new Font("Arial", Font.BOLD, 24));
        
        lblTotalProfs = new JLabel("0", SwingConstants.CENTER);
        lblTotalProfs.setFont(new Font("Arial", Font.BOLD, 24));
        
        lblSalles = new JLabel("0", SwingConstants.CENTER);
        lblSalles.setFont(new Font("Arial", Font.BOLD, 24));
        
        add(creerCarte("Soutenances Prévues", lblTotalSoutenances));
        add(creerCarte("Professeurs Chargés", lblTotalProfs));
        add(creerCarte("Salles Utilisées", lblSalles));
    }
    
    // petite fct pour pas repeter le meme code 3 fois
    private JPanel creerCarte(String titre, JLabel labelValeur) {
        JPanel carte = new JPanel(new BorderLayout());
        carte.setBackground(Color.WHITE);
        carte.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2));
        
        JLabel lblTitre = new JLabel(titre, SwingConstants.CENTER);
        lblTitre.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        
        carte.add(lblTitre, BorderLayout.NORTH);
        carte.add(labelValeur, BorderLayout.CENTER);
        
        return carte;
    }
    
    public void majStatistiques(int nbSoutenances, int nbProfs, int nbSalles) {
        lblTotalSoutenances.setText(String.valueOf(nbSoutenances));
        lblTotalProfs.setText(String.valueOf(nbProfs));
        lblSalles.setText(String.valueOf(nbSalles));
    }
}
