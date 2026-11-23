package ma.ensah.pfe.services;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigService {
    private static ConfigService instance;
    private Properties properties;

    private ConfigService() {
        properties = new Properties();
        // Valeurs par défaut (seront écrasées par l'onglet Configuration d'Excel)
        properties.setProperty("jury.prioriser.equite", "true");
        properties.setProperty("jury.specialite.requise", "info");
        properties.setProperty("quota.max.etudiants.prof", "4");
        properties.setProperty("quota.min.etudiants.prof", "3");
        properties.setProperty("excel.sheet.etudiants", "Etudiants");
        properties.setProperty("excel.sheet.profs", "Professeurs");
        properties.setProperty("excel.sheet.salles", "Salles");
        properties.setProperty("ui.default.jours", "3");
        properties.setProperty("soutenance.duree", "1");
        properties.setProperty("jury.taille", "3");
    }

    public static ConfigService getInstance() {
        if (instance == null) {
            instance = new ConfigService();
        }
        return instance;
    }
    
    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    public int getInt(String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value != null) {
            try {
                return Integer.parseInt(value.trim());
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public String getString(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue).trim();
    }
}
