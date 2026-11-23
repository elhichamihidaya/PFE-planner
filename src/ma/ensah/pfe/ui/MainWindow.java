package ma.ensah.pfe.ui;

import ma.ensah.pfe.io.*;
import ma.ensah.pfe.models.*;
import ma.ensah.pfe.services.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class MainWindow extends JFrame {
    
    private JButton btnCharger;
    private JButton btnDates; // Nouveau bouton
    private JButton btnPlanning;
    private JButton btnPV;
    private JButton btnPDF; // Bouton PDF
    private JButton btnAffectationsPDF; // Nouveau bouton
    
    private JTable tableauPlanning;
    private DefaultTableModel modeleTableau;
    private JTextArea consoleArea;
    
    // Les nouveaux panneaux recuperes des idees de l'equipe
    private PanneauCartes panneauCartes;
    private PanneauAlertes panneauAlertes;
    
    private List<Professeur> lesProfs = new ArrayList<>();
    private List<Etudiant> lesEtudiants = new ArrayList<>();
    private List<Soutenance> lePlanningFinal = new ArrayList<>();
    private List<Salle> lesSalles = new ArrayList<>();
    private List<Creneau> lesCreneaux = new ArrayList<>();
    private String lastExcelPath = null;

    public MainWindow() {
        setTitle("PFE Planner - Dashboard");
        setSize(1100, 700); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 
        
        // initSallesEtCreneaux(); // Supprimé pour respecter OCP (Open/Closed Principle)
        construireInterface();
        redirigerConsole();
    }
    
    private void construireInterface() {
        setLayout(new BorderLayout());
        
        // Titre + Cartes Statistiques
        JPanel panelHaut = new JPanel(new BorderLayout());
        
        JPanel panelTitre = new JPanel();
        panelTitre.setBackground(new Color(41, 128, 185)); 
        JLabel labelTitre = new JLabel("Planificateur PFE 2026");
        labelTitre.setForeground(Color.WHITE);
        labelTitre.setFont(new Font("Arial", Font.BOLD, 22));
        panelTitre.add(labelTitre);
        
        panneauCartes = new PanneauCartes(); 
        
        panelHaut.add(panelTitre, BorderLayout.NORTH);
        panelHaut.add(panneauCartes, BorderLayout.CENTER);
        add(panelHaut, BorderLayout.NORTH);
        
        //Boutons
        JPanel panelGauche = new JPanel(new GridLayout(8, 1, 10, 15)); // 8 lignes maintenant
        panelGauche.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        
        btnCharger = new JButton("1. Charger Excel (Sauf Creneaux)");
        btnDates = new JButton("2. Configurer Dates");
        btnPlanning = new JButton("3. Generer Planning");
        btnPV = new JButton("4. Generer PV Word");
        btnPDF = new JButton("5. Exporter PDF (Coloré)");
        btnAffectationsPDF = new JButton("6. Exporter Affectations PDF");
        JButton btnDashboard = new JButton("7. Dashboard Détaillé");
        btnDashboard.setBackground(new Color(46, 204, 113)); 
        btnDashboard.setForeground(Color.WHITE);
        JButton btnQuitter = new JButton("Quitter");
        
        panelGauche.add(btnCharger);
        panelGauche.add(btnDates);
        panelGauche.add(btnPlanning);
        panelGauche.add(btnPV);
        panelGauche.add(btnPDF);
        panelGauche.add(btnAffectationsPDF);
        panelGauche.add(btnDashboard);
        panelGauche.add(btnQuitter);
        
        add(panelGauche, BorderLayout.WEST);
        
        // centre (Tableau + Console)
        JPanel panelCentre = new JPanel(new GridLayout(2, 1)); 
        
        String[] colonnes = {"Etudiant", "Filiere", "Salle", "Date/Heure", "Jury"};
        // Modele de tableau editable
        modeleTableau = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true; // Rendre le planning modifiable par l'utilisateur
            }
        }; 
        tableauPlanning = new JTable(modeleTableau);
        JScrollPane scrollTableau = new JScrollPane(tableauPlanning);
        scrollTableau.setBorder(BorderFactory.createTitledBorder("Planning Officiel"));
        
        consoleArea = new JTextArea();
        consoleArea.setEditable(false);
        consoleArea.setBackground(Color.BLACK);
        consoleArea.setForeground(Color.GREEN); 
        JScrollPane scrollConsole = new JScrollPane(consoleArea);
        scrollConsole.setBorder(BorderFactory.createTitledBorder("Logs d'execution"));
        
        panelCentre.add(scrollTableau);
        panelCentre.add(scrollConsole);
        add(panelCentre, BorderLayout.CENTER);
        
        //Alertes
        panneauAlertes = new PanneauAlertes();
        add(panneauAlertes, BorderLayout.EAST);
        
        //LES ACTIONS
        
        btnCharger.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String pathToLoad = null;
                
                if (lastExcelPath != null) {
                    int choix = JOptionPane.showConfirmDialog(MainWindow.this, 
                        "Voulez-vous réactualiser le fichier existant ?\n(Sélectionnez 'Non' pour choisir un nouveau fichier)", 
                        "Actualisation rapide", JOptionPane.YES_NO_CANCEL_OPTION);
                    
                    if (choix == JOptionPane.CANCEL_OPTION) return;
                    if (choix == JOptionPane.YES_OPTION) {
                        pathToLoad = lastExcelPath;
                    }
                }
                
                if (pathToLoad == null) {
                    JFileChooser chooser = new JFileChooser();
                    System.out.println("Selectionnez le fichier Excel GLOBAL (Etudiants, Profs, Salles, Creneaux)...");
                    chooser.setMultiSelectionEnabled(false); 
                    if(chooser.showOpenDialog(MainWindow.this) == JFileChooser.APPROVE_OPTION) {
                        pathToLoad = chooser.getSelectedFile().getAbsolutePath();
                        lastExcelPath = pathToLoad;
                    } else {
                        return; // L'utilisateur a annulé
                    }
                }
                
                // Chargement effectif du fichier
                ExcelReader lecteur = new ExcelReader();
                lecteur.chargerFichierGlobal(pathToLoad);
                
                lesProfs = lecteur.getProfs();
                lesEtudiants = lecteur.getEtudiants();
                lesSalles = lecteur.getSalles();
                lesCreneaux = lecteur.getCreneaux();
                
                // melange aleatoire etudiants
                java.util.Collections.shuffle(lesEtudiants);
                
                System.out.println("-> Base de données actualisée depuis : " + pathToLoad);
                System.out.println(lesEtudiants.size() + " Etudiants, " + lesProfs.size() + " Profs, " + lesSalles.size() + " Salles, " + lesCreneaux.size() + " Creneaux.");
                
                // affectation automatique des encadrants
                if (lesProfs.size() > 0 && lesEtudiants.size() > 0) {
                    System.out.println("Distribution automatique des encadrants en cours...");
                    AffectationService affectation = new AffectationService();
                    affectation.affecterAutomatiquement(lesEtudiants, lesProfs);
                    System.out.println("-> Succes : Chaque etudiant a recu un encadrant officiel !");
                }
                
                // On met a jour les cartes
                panneauCartes.majStatistiques(lesEtudiants.size(), lesProfs.size(), lesSalles.size());
            }
        });
        
        //  BOUTON : CONFIGURATION DATES
        btnDates.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JPanel panel = new JPanel(new GridLayout(6, 2, 5, 10));
                
                JTextField txtDate = new JTextField("23/06/2026");
                JTextField txtJours = new JTextField(ma.ensah.pfe.services.ConfigService.getInstance().getString("ui.default.jours", "4"));
                
                String[] modes = {"Journée Complète", "Matin Seulement", "Après-midi Seulement"};
                JComboBox<String> comboMode = new JComboBox<>(modes);
                
                JTextField txtMatinDebut = new JTextField("09:00");
                JTextField txtMatinFin = new JTextField("12:00");
                JTextField txtApremDebut = new JTextField("14:00");
                JTextField txtApremFin = new JTextField("18:00");
                
                panel.add(new JLabel("Date de début (JJ/MM/AAAA) :"));
                panel.add(txtDate);
                panel.add(new JLabel("Nombre de jours :"));
                panel.add(txtJours);
                panel.add(new JLabel("Mode (Raccourcis) :"));
                panel.add(comboMode);
                
                panel.add(new JLabel("1ère moitié (Matin) :"));
                JPanel panelMatin = new JPanel(new GridLayout(1, 3));
                panelMatin.add(txtMatinDebut); panelMatin.add(new JLabel(" à ", SwingConstants.CENTER)); panelMatin.add(txtMatinFin);
                panel.add(panelMatin);
                
                panel.add(new JLabel("2ème moitié (Après-midi) :"));
                JPanel panelAprem = new JPanel(new GridLayout(1, 3));
                panelAprem.add(txtApremDebut); panelAprem.add(new JLabel(" à ", SwingConstants.CENTER)); panelAprem.add(txtApremFin);
                panel.add(panelAprem);
                
                comboMode.addActionListener(ev -> {
                    String mode = (String) comboMode.getSelectedItem();
                    if ("Matin Seulement".equals(mode)) {
                        txtMatinDebut.setEnabled(true); txtMatinFin.setEnabled(true);
                        txtApremDebut.setEnabled(false); txtApremFin.setEnabled(false);
                    } else if ("Après-midi Seulement".equals(mode)) {
                        txtMatinDebut.setEnabled(false); txtMatinFin.setEnabled(false);
                        txtApremDebut.setEnabled(true); txtApremFin.setEnabled(true);
                    } else {
                        txtMatinDebut.setEnabled(true); txtMatinFin.setEnabled(true);
                        txtApremDebut.setEnabled(true); txtApremFin.setEnabled(true);
                    }
                });

                int result = JOptionPane.showConfirmDialog(MainWindow.this, panel, "Assistant de Configuration des Créneaux", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (result == JOptionPane.OK_OPTION) {
                    try {
                        java.time.format.DateTimeFormatter dateFormatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
                        java.time.format.DateTimeFormatter timeFormatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm");
                        
                        LocalDate dateDebut = LocalDate.parse(txtDate.getText().trim(), dateFormatter);
                        int nbJours = Integer.parseInt(txtJours.getText().trim());
                        
                        lesCreneaux.clear();
                        String mode = (String) comboMode.getSelectedItem();
                        
                        for (int i = 0; i < nbJours; i++) {
                            LocalDate dateCourante = dateDebut.plusDays(i);
                            
                            // Generation Matin (decoupage par heure)
                            if (!"Après-midi Seulement".equals(mode)) {
                                LocalTime tMatin = LocalTime.parse(txtMatinDebut.getText().trim(), timeFormatter);
                                LocalTime endMatin = LocalTime.parse(txtMatinFin.getText().trim(), timeFormatter);
                                while (tMatin.isBefore(endMatin)) {
                                    LocalTime nextT = tMatin.plusHours(1);
                                    if(nextT.isAfter(endMatin)) nextT = endMatin;
                                    lesCreneaux.add(new Creneau(dateCourante, tMatin, nextT));
                                    tMatin = nextT;
                                }
                            }
                            
                            // decoupage par heure
                            if (!"Matin Seulement".equals(mode)) {
                                LocalTime tAprem = LocalTime.parse(txtApremDebut.getText().trim(), timeFormatter);
                                LocalTime endAprem = LocalTime.parse(txtApremFin.getText().trim(), timeFormatter);
                                while (tAprem.isBefore(endAprem)) {
                                    LocalTime nextT = tAprem.plusHours(1);
                                    if(nextT.isAfter(endAprem)) nextT = endAprem;
                                    lesCreneaux.add(new Creneau(dateCourante, tAprem, nextT));
                                    tAprem = nextT;
                                }
                            }
                        }
                        
                        System.out.println("-> " + lesCreneaux.size() + " Créneaux générés avec succès !");
                        JOptionPane.showMessageDialog(MainWindow.this, "Configuration terminée !\n" + lesCreneaux.size() + " créneaux horaires générés.", "Succès", JOptionPane.INFORMATION_MESSAGE);
                        
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(MainWindow.this, "Erreur de format. Vérifiez les dates (JJ/MM/AAAA) et les heures (HH:mm).", "Erreur", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        
        btnPlanning.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(lesEtudiants.size() == 0 || lesProfs.size() == 0 || lesSalles.size() == 0 || lesCreneaux.size() == 0) {
                    JOptionPane.showMessageDialog(MainWindow.this, "Erreur: Manque de donnees (Fichier Excel incomplet) !");
                    return;
                }
                
                // Vérification stricte des ressources
                int nbBinomes = 0;
                for (Etudiant etu : lesEtudiants) {
                    if (etu.getBinome() != null) nbBinomes++;
                }
                int nbSoutenancesReelles = lesEtudiants.size() - (nbBinomes / 2);
                int capaciteMax = lesSalles.size() * lesCreneaux.size();
                
                if (nbSoutenancesReelles > capaciteMax) {
                    JOptionPane.showMessageDialog(MainWindow.this, "IMPOSSIBLE : Vous avez " + nbSoutenancesReelles + " soutenances à planifier mais seulement " + capaciteMax + " places (Salles x Créneaux).\nVeuillez ajouter des salles ou des créneaux dans le fichier Excel.", "Erreur Critique", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                System.out.println("Lancement de l'algorithme...");
                PlanningService moteur = new PlanningService();
                lePlanningFinal = moteur.genererPlanning(lesEtudiants, lesProfs, lesSalles, lesCreneaux);
                
                
                VerificationService verif = new VerificationService();
                List<String> erreurs = verif.verifierConstraint(lePlanningFinal);
                
                // on met a jour le panneau de droite 
                panneauAlertes.mettreAJourAlertes(erreurs);
                
                if(erreurs.size() > 0) {
                    System.out.println("Attention : Anomalies detectees, voir panneau de droite.");
                } else {
                    System.out.println("Succes : Zero conflit !");
                }
                
                // Triage chronologique (par date et heure)
                java.util.Collections.sort(lePlanningFinal, new java.util.Comparator<Soutenance>() {
                    public int compare(Soutenance s1, Soutenance s2) {
                        if (s1.getCreneau() == null && s2.getCreneau() == null) return 0;
                        if (s1.getCreneau() == null) return 1;
                        if (s2.getCreneau() == null) return -1;
                        
                        int dateCmp = s1.getCreneau().getDate().compareTo(s2.getCreneau().getDate());
                        if (dateCmp != 0) return dateCmp;
                        return s1.getCreneau().getHeureDebut().compareTo(s2.getCreneau().getHeureDebut());
                    }
                });
                
                // Affichage dans le tableau
                modeleTableau.setRowCount(0); 
                for(int i=0; i<lePlanningFinal.size(); i++) {
                    Soutenance s = lePlanningFinal.get(i);
                    
                    String etudiant = s.getEtudiant().getNom() + " " + s.getEtudiant().getPrenom();
                    if(s.getBinome() != null) {
                        etudiant += " & " + s.getBinome().getNom() + " " + s.getBinome().getPrenom();
                    }
                    
                    String filiere = s.getEtudiant().getFiliere();
                    String salle = (s.getSalle() != null) ? s.getSalle().getNom() : "?";
                    String date = (s.getCreneau() != null) ? s.getCreneau().toString() : "?";
                    
                    String juryText = "";
                    for(int j=0; j<s.getJury().size(); j++) {
                        juryText += s.getJury().get(j).getNom() + " ";
                    }
                    
                    Object[] ligne = {etudiant, filiere, salle, date, juryText};
                    modeleTableau.addRow(ligne);
                }
            }
        });
        
        btnPV.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(lePlanningFinal.size() == 0) return;
                
                PVGenerator gen = new PVGenerator();
                System.out.println("Creation des PVs Word...");
                for(int i=0; i<lePlanningFinal.size(); i++) {
                    ma.ensah.pfe.models.Soutenance s = lePlanningFinal.get(i);
                    gen.genererPVDocx(s, s.getEtudiant(), "template_pfe.docx", "PV_Soutenances");
                    
                    if(s.getEtudiant().getBinome() != null) {
                        gen.genererPVDocx(s, s.getEtudiant().getBinome(), "template_pfe.docx", "PV_Soutenances");
                    }
                }
                System.out.println("PVs termines !");
            }
        });
        
        btnPDF.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(lePlanningFinal.size() == 0) {
                    JOptionPane.showMessageDialog(MainWindow.this, "Veuillez d'abord générer le planning !");
                    return;
                }
                
                System.out.println("Génération du Planning PDF Coloré en cours...");
                ma.ensah.pfe.io.PdfExportService pdfService = new ma.ensah.pfe.io.PdfExportService();
                String filePath = "Planning_Soutenances_Colore.pdf";
                pdfService.exporterPlanningPDF(lePlanningFinal, filePath);
                System.out.println("-> PDF généré avec succès : " + filePath);
                JOptionPane.showMessageDialog(MainWindow.this, "Planning PDF exporté avec succès !\nFichier: " + filePath, "Succès", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        btnAffectationsPDF.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(lesProfs.isEmpty() || lesEtudiants.isEmpty()) {
                    JOptionPane.showMessageDialog(MainWindow.this, "Veuillez d'abord charger le fichier Excel !");
                    return;
                }
                
                System.out.println("Génération du rapport d'Affectations PDF en cours...");
                ma.ensah.pfe.io.PdfExportService pdfService = new ma.ensah.pfe.io.PdfExportService();
                String filePath = "Affectations_Encadrants.pdf";
                pdfService.exporterAffectationsPDF(lesProfs, lesEtudiants, filePath);
                System.out.println("-> PDF des affectations généré avec succès : " + filePath);
                JOptionPane.showMessageDialog(MainWindow.this, "Rapport d'affectations exporté avec succès !\nFichier: " + filePath, "Succès", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        btnDashboard.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(lesProfs.isEmpty() || lesEtudiants.isEmpty()) {
                    JOptionPane.showMessageDialog(MainWindow.this, "Veuillez charger les fichiers Excel d'abord !", "Erreur", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                StatisticsService statService = new StatisticsService();
                String rapport = statService.genererRapportComplet(lesEtudiants, lesProfs, lePlanningFinal);
                
                // On crée une jolie fenêtre pour afficher le rapport textuel
                JTextArea zoneTexte = new JTextArea(rapport);
                zoneTexte.setEditable(false);
                zoneTexte.setFont(new Font("Monospaced", Font.PLAIN, 14));
                zoneTexte.setBackground(new Color(245, 245, 245));
                zoneTexte.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                
                JScrollPane scrollStats = new JScrollPane(zoneTexte);
                scrollStats.setPreferredSize(new Dimension(600, 450));
                
                JOptionPane.showMessageDialog(MainWindow.this, scrollStats, "Tableau de Bord Détaillé", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        btnQuitter.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
    }
    
    private void redirigerConsole() {
        try {
            PrintStream printStream = new PrintStream(new OutputStream() {
                @Override
                public void write(int b) {
                    consoleArea.append(String.valueOf((char)b));
                    consoleArea.setCaretPosition(consoleArea.getDocument().getLength());
                }
                
                @Override
                public void write(byte[] b, int off, int len) {
                    try {
                        String s = new String(b, off, len, java.nio.charset.StandardCharsets.UTF_8);
                        consoleArea.append(s);
                        consoleArea.setCaretPosition(consoleArea.getDocument().getLength());
                    } catch (Exception e) {}
                }
            }, true, "UTF-8");
            
            System.setOut(printStream);
            System.setErr(printStream);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        MainWindow fenetre = new MainWindow();
        fenetre.setVisible(true);
    }
}
