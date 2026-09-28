import AppServices.FileService;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.*;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import java.io.FileWriter;
import java.io.PrintWriter;

/**
 * Serveur CORBA en Java. Il expose un objet FileService (implemente par
 * FileServiceImpl, qui parle au systeme de fichiers local) et publie sa
 * reference CORBA (IOR) dans un fichier texte que n'importe quel client
 * (Java OU C++) pourra lire pour s'y connecter.
 *
 * Usage :
 *   java FileServer [--ior fileservice.ior] [-ORBInitialPort N] [-ORBInitialHost H]
 */
public class FileServer {

    private static String getOpt(String[] args, String name, String def) {
        for (int i = 0; i < args.length - 1; i++) {
            if (name.equals(args[i])) return args[i + 1];
        }
        return def;
    }

    public static void main(String[] args) {
        try {
            // L'ORB reconnait et retire ses propres options (-ORBxxx) de args.
            ORB orb = ORB.init(args, null);

            String iorFile = getOpt(args, "--ior", "fileservice.ior");

            POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootpoa.the_POAManager().activate();

            FileServiceImpl servant = new FileServiceImpl();
            org.omg.CORBA.Object ref = rootpoa.servant_to_reference(servant);
            FileService fileRef = AppServices.FileServiceHelper.narrow(ref);

            String iorStr = orb.object_to_string(fileRef);

            try (PrintWriter out = new PrintWriter(new FileWriter(iorFile))) {
                out.println(iorStr);
            }

            System.out.println("=== Serveur FileService (Java) demarre ===");
            System.out.println("IOR ecrite dans : " + iorFile);
            System.out.println("IOR : " + iorStr);

            orb.run();
        } catch (Exception e) {
            System.err.println("Erreur serveur : " + e);
            e.printStackTrace();
            System.exit(1);
        }
    }
}
