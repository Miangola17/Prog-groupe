public class ServiceClasses {

    private final Classes.GestionClasses gestion;

    public ServiceClasses(Classes.GestionClasses gestion) {
        this.gestion = gestion;
    }

    public String[] listerClasses() throws Classes.ErreurClasse {
        return gestion.listerClasses();
    }

    public String[] listerEtudiants(String nomClasse) throws Classes.ErreurClasse {
        return gestion.listerEtudiants(nomClasse);
    }
}
