import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Font;
import java.util.Locale;

public class PanelClasse extends JPanel {

    public PanelClasse(ResultatClasse resultat) {
        String[] colonnes = { "Nom", "Note" };
        DefaultTableModel modele = new DefaultTableModel(colonnes, 0);
        for (Object[] ligne : resultat.lignes) {
            modele.addRow(ligne);
        }

        JTable table = new JTable(modele);
        JScrollPane defilement = new JScrollPane(table);

        JLabel etiquetteMoyenne = new JLabel(
                String.format(Locale.ROOT, "Moyenne de la classe : %.2f", resultat.moyenne()));
        etiquetteMoyenne.setFont(etiquetteMoyenne.getFont().deriveFont(Font.BOLD, 13f));
        etiquetteMoyenne.setBorder(new EmptyBorder(8, 8, 8, 8));

        setLayout(new BorderLayout());
        add(defilement, BorderLayout.CENTER);
        add(etiquetteMoyenne, BorderLayout.SOUTH);
    }
}
