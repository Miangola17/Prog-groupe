import java.util.List;

public class ResultatClasse {

    public final List<Object[]> lignes;
    public final double somme;
    public final int nombre;

    public ResultatClasse(List<Object[]> lignes, double somme, int nombre) {
        this.lignes = lignes;
        this.somme = somme;
        this.nombre = nombre;
    }

    public double moyenne() {
        return nombre == 0 ? 0 : somme / nombre;
    }
}
