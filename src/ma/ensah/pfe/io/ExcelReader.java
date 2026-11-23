package ma.ensah.pfe.io;

import ma.ensah.pfe.models.Etudiant;
import ma.ensah.pfe.models.Professeur;
import ma.ensah.pfe.models.Salle;
import ma.ensah.pfe.models.Creneau;
import ma.ensah.pfe.services.ConfigService;

import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelReader {
    private DataFormatter formatter = new DataFormatter();

    private List<Professeur> profs = new ArrayList<>();
    private List<Etudiant> etudiants = new ArrayList<>();
    private List<Salle> salles = new ArrayList<>();
    private List<Creneau> creneaux = new ArrayList<>();

    public void chargerFichierGlobal(String cheminFichier) {
        profs.clear();
        etudiants.clear();
        salles.clear();
        creneaux.clear();

        try {
            FileInputStream file = new FileInputStream(new File(cheminFichier));
            Workbook workbook = new XSSFWorkbook(file);
            ConfigService config = ConfigService.getInstance();

            // 0. Lire la Configuration (Prioritaire car elle redéfinit les paramètres)
            Sheet sheetConfig = workbook.getSheet("Configuration");
            if (sheetConfig != null) lireConfiguration(sheetConfig);

            // 1. Lire les Étudiants
            Sheet sheetEtu = workbook.getSheet(config.getString("excel.sheet.etudiants", "Etudiants"));
            if (sheetEtu != null) lireEtudiants(sheetEtu);

            // 2. Lire les Professeurs
            Sheet sheetProf = workbook.getSheet(config.getString("excel.sheet.profs", "Professeurs"));
            if (sheetProf != null) lireProfs(sheetProf);

            // 3. Lire les Salles
            Sheet sheetSalles = workbook.getSheet(config.getString("excel.sheet.salles", "Salles"));
            if (sheetSalles != null) lireSalles(sheetSalles);

            

            workbook.close();
            file.close();
        } catch (Exception e) {
            System.out.println("Erreur (ExcelReader Global) : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void lireConfiguration(Sheet sheet) {
        ConfigService config = ConfigService.getInstance();
        System.out.println("\n--- Lecture de l'onglet Configuration ---");
        int count = 0;
        
        for (Row row : sheet) {
            if (row.getCell(0) == null || row.getCell(1) == null) continue;
            
            String key = formatter.formatCellValue(row.getCell(0)).trim();
            String value = formatter.formatCellValue(row.getCell(1)).trim();
            
            if (key.isEmpty() || value.isEmpty()) continue;
            
            String lowerKey = key.toLowerCase();
            boolean reconnu = false;
            
            if (lowerKey.contains("jours")) {
                config.setProperty("ui.default.jours", value);
                reconnu = true;
            } else if (lowerKey.contains("profs") && lowerKey.contains("jury")) {
                config.setProperty("jury.taille", value);
                reconnu = true;
            } else if (lowerKey.contains("duree") || lowerKey.contains("durée")) {
                config.setProperty("soutenance.duree", value);
                reconnu = true;
            } else if (lowerKey.contains("equite") || lowerKey.contains("équité")) {
                config.setProperty("jury.prioriser.equite", value.equalsIgnoreCase("oui") || value.equalsIgnoreCase("true") ? "true" : "false");
                reconnu = true;
            } else if (lowerKey.contains("specialite") || lowerKey.contains("spécialité")) {
                config.setProperty("jury.specialite.requise", value);
                reconnu = true;
            } else if (lowerKey.contains("maximum") && lowerKey.contains("etudiant")) {
                config.setProperty("quota.max.etudiants.prof", value);
                reconnu = true;
            }
            
            if (reconnu) {
                System.out.println(" * " + key + " => " + value);
                count++;
            }
        }
        System.out.println("-> " + count + " paramètre(s) chargé(s) depuis Excel.\n");
    }

    private void lireProfs(Sheet sheet) {
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;
            String nom = formatter.formatCellValue(row.getCell(0));
            String prenom = formatter.formatCellValue(row.getCell(1));
            String discipline = formatter.formatCellValue(row.getCell(2));
            
            if (!nom.isEmpty() && !nom.equalsIgnoreCase("Nom") && !nom.equalsIgnoreCase("Name")) { 
                profs.add(new Professeur(nom, prenom, discipline));
            }
        }
    }

    private void lireEtudiants(Sheet sheet) {
        java.util.Map<String, String> mapBinomes = new java.util.HashMap<>();
        
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;
            String cne = formatter.formatCellValue(row.getCell(0));
            String nom = formatter.formatCellValue(row.getCell(1));
            String prenom = formatter.formatCellValue(row.getCell(2));
            String email = formatter.formatCellValue(row.getCell(4)); // On prend l'Email Academique (Colonne E)
            String filiere = formatter.formatCellValue(row.getCell(5)); // Colonne F
            
            boolean pfeEnAnglais = formatter.formatCellValue(row.getCell(6)).equalsIgnoreCase("oui"); // Colonne G
            String cneBinome = formatter.formatCellValue(row.getCell(7)); // Colonne H : CNE du Binome
            
            if (!nom.isEmpty() && !nom.equalsIgnoreCase("Nom")) {
                Etudiant e = new Etudiant(cne, nom, prenom, email, filiere, pfeEnAnglais, null);
                etudiants.add(e);
                
                if (cneBinome != null && !cneBinome.isEmpty()) {
                    mapBinomes.put(cne, cneBinome);
                }
            }
        }
        
        // Deuxieme passe : Lier physiquement les objets Etudiant entre eux
        for (Etudiant e1 : etudiants) {
            String cneCible = mapBinomes.get(e1.getCne());
            if (cneCible != null && e1.getBinome() == null) {
                for (Etudiant e2 : etudiants) {
                    if (e2.getCne().equals(cneCible)) {
                        e1.setBinome(e2);
                        e2.setBinome(e1); // Lien bidirectionnel
                        break;
                    }
                }
            }
        }
    }

    private void lireSalles(Sheet sheet) {
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;
            String nom = formatter.formatCellValue(row.getCell(0));
            if (!nom.isEmpty() && !nom.equalsIgnoreCase("Nom")) {
                salles.add(new Salle(nom));
            }
        }
    }

    private void lireCreneaux(Sheet sheet) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;
            try {
                String dateStr = formatter.formatCellValue(row.getCell(0));
                String heureDebutStr = formatter.formatCellValue(row.getCell(1));
                String heureFinStr = formatter.formatCellValue(row.getCell(2));

                if (!dateStr.isEmpty()) {
                    LocalDate date = LocalDate.parse(dateStr, dateFormatter);
                    LocalTime debut = LocalTime.parse(heureDebutStr, timeFormatter);
                    LocalTime fin = LocalTime.parse(heureFinStr, timeFormatter);
                    creneaux.add(new Creneau(date, debut, fin));
                }
            } catch (Exception e) {
                System.out.println("Erreur parsing creneau ligne " + row.getRowNum());
            }
        }
    }

    public List<Professeur> getProfs() { return profs; }
    public List<Etudiant> getEtudiants() { return etudiants; }
    public List<Salle> getSalles() { return salles; }
    public List<Creneau> getCreneaux() { return creneaux; }
}
