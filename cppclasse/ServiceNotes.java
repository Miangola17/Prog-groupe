import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ServiceNotes {

    private final ServiceClasses serviceClasses;
    private final Map<String, Double> notesLocales;

    public ServiceNotes(ServiceClasses serviceClasses, Map<String, Double> notesLocales) {
        this.serviceClasses = serviceClasses;
        this.notesLocales = notesLocales;
    }

    public ResultatClasse construireResultat(String nomClasse) throws Classes.ErreurClasse {
        String[] noms = serviceClasses.listerEtudiants(nomClasse);
        List<Object[]> lignes = new ArrayList<>();
        double somme = 0;
        int nombre = 0;
        for (String nom : noms) {
            Double note = notesLocales.get(nom);
            if (note == null) {
                lignes.add(new Object[] { nom, "introuvable" });
            } else {
                lignes.add(new Object[] { nom, note });
                somme += note;
                nombre++;
            }
        }
        return new ResultatClasse(lignes, somme, nombre);
    }
}
