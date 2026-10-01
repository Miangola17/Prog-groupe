import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Font;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

public class ClientNotes {

    public static void main(String[] args) throws Exception {
        Map<String, Double> notesLocales = lireNotesLocales();

        Properties proprietes = new Properties();
        proprietes.setProperty("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
        proprietes.setProperty("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");
        org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args, proprietes);

        String ior = new String(Files.readAllBytes(Paths.get("classes.ior")), StandardCharsets.UTF_8).trim();
        org.omg.CORBA.Object objet = orb.string_to_object(ior);
        Classes.GestionClasses gestion = Classes.GestionClassesHelper.narrow(objet);

        ServiceClasses serviceClasses = new ServiceClasses(gestion);
        ServiceNotes serviceNotes = new ServiceNotes(serviceClasses, notesLocales);

        String[] nomsClasses = serviceClasses.listerClasses();

        JTabbedPane onglets = new JTabbedPane();
        double sommeGenerale = 0;
        int nombreGeneral = 0;

        for (String nomClasse : nomsClasses) {
            ResultatClasse resultat = serviceNotes.construireResultat(nomClasse);
            onglets.addTab(nomClasse, new PanelClasse(resultat));
            sommeGenerale += resultat.somme;
            nombreGeneral += resultat.nombre;
        }

        double moyenneGenerale = nombreGeneral == 0 ? 0 : sommeGenerale / nombreGeneral;

        SwingUtilities.invokeLater(() -> afficher(onglets, moyenneGenerale));
    }

    private static Map<String, Double> lireNotesLocales() throws Exception {
        Map<String, Double> notes = new HashMap<>();
        List<String> lignes = Files.readAllLines(Paths.get("notes.txt"), StandardCharsets.UTF_8);
        for (String ligne : lignes) {
            String propre = ligne.trim();
            if (propre.isEmpty()) {
                continue;
            }
            String[] parties = propre.split(";");
            notes.put(parties[0], Double.parseDouble(parties[1]));
        }
        return notes;
    }

    private static void afficher(JTabbedPane onglets, double moyenneGenerale) {
        JLabel etiquetteGenerale = new JLabel(
                String.format(Locale.ROOT, "Moyenne generale de toutes les classes : %.2f", moyenneGenerale));
        etiquetteGenerale.setFont(etiquetteGenerale.getFont().deriveFont(Font.BOLD, 14f));
        etiquetteGenerale.setBorder(new EmptyBorder(10, 10, 10, 10));

        JFrame fenetre = new JFrame("Notes des classes");
        fenetre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fenetre.setLayout(new BorderLayout());
        fenetre.add(onglets, BorderLayout.CENTER);
        fenetre.add(etiquetteGenerale, BorderLayout.SOUTH);
        fenetre.setSize(450, 350);
        fenetre.setLocationRelativeTo(null);
        fenetre.setVisible(true);
    }
}
