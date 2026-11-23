package ma.ensah.pfe.ui;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class PanneauAlertes extends JPanel {

    private JPanel contenuAlertes;

    public PanneauAlertes() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Alertes / Anomalies"));
        setPreferredSize(new Dimension(270, 0)); // fixe a droite
        
        // On cree le panel interieur qui va stocker la liste infinie
        contenuAlertes = new JPanel();
        contenuAlertes.setLayout(new BoxLayout(contenuAlertes, BoxLayout.Y_AXIS));
        contenuAlertes.setBackground(new Color(245, 245, 245)); // gris tres clair
        
        // ScrollPane pour scroller 
        JScrollPane scroll = new JScrollPane(contenuAlertes);
        scroll.setBorder(null); // pour eviter un double cadre moche
        scroll.getVerticalScrollBar().setUnitIncrement(16); // Pour que la molette de souris aille plus vite
        
        add(scroll, BorderLayout.CENTER);
    }
    
    // methode pour rafraichir quand on clique sur Generer Planning
    public void mettreAJourAlertes(List<String> erreurs) {
        contenuAlertes.removeAll(); // on vide tout
        
        if (erreurs.size() == 0) {
            JLabel lblOk = new JLabel("Tout est parfait !");
            lblOk.setForeground(new Color(0, 150, 0)); // vert
            lblOk.setFont(new Font("Arial", Font.BOLD, 14));
            contenuAlertes.add(lblOk);
        } else {
            // on fait une boucle simple pour afficher chaque erreur
            for (int i=0; i<erreurs.size(); i++) {
                String err = erreurs.get(i);
                
                JPanel pnlErr = new JPanel(new BorderLayout());
                pnlErr.setBackground(new Color(255, 220, 220)); 
                pnlErr.setBorder(BorderFactory.createLineBorder(Color.RED));
                
                // utiliser un JTextArea permet au texte long de revenir a la ligne
                JTextArea txtErr = new JTextArea("! " + err);
                txtErr.setWrapStyleWord(true);
                txtErr.setLineWrap(true);
                txtErr.setEditable(false);
                txtErr.setBackground(new Color(255, 220, 220));
                txtErr.setForeground(Color.RED);
                
                pnlErr.add(txtErr, BorderLayout.CENTER);
                pnlErr.setMaximumSize(new Dimension(300, 60)); 
                
                contenuAlertes.add(pnlErr);
                contenuAlertes.add(Box.createVerticalStrut(10)); 
            }
        }
        
        // on force l'interface a se redessiner
        contenuAlertes.revalidate();
        contenuAlertes.repaint();
    }
}
