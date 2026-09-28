import AppServices.DBService;
import AppServices.DBServiceHelper;
import AppServices.FileService;
import AppServices.FileServiceHelper;
import AppServices.ServiceException;
import org.omg.CORBA.ORB;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Client CORBA en Java, 100% terminal (pas d'interface graphique).
 * Memes commandes que le client C++ (Client.cpp) : n'importe lequel des
 * deux peut parler a un serveur DBService et/ou FileService, quelle que
 * soit la langue de ce serveur.
 *
 * Voir docs/COMMANDS.md pour tous les exemples d'appel.
 */
public class Client {

    private static String readFirstLine(String path) {
        try (BufferedReader r = new BufferedReader(new FileReader(path))) {
            String line = r.readLine();
            return line == null ? "" : line;
        } catch (IOException e) {
            return "";
        }
    }

    private static org.omg.CORBA.Object resolveRef(ORB orb, String iorArg) {
        String iorStr = iorArg;
        if (!iorArg.startsWith("IOR:") && !iorArg.startsWith("corbaloc:")) {
            iorStr = readFirstLine(iorArg);
            if (iorStr.isEmpty()) {
                System.err.println("Impossible de lire une IOR dans le fichier : " + iorArg);
                System.exit(1);
            }
        }
        return orb.string_to_object(iorStr);
    }

    private static void printRows(String[][] rows) {
        for (String[] row : rows) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < row.length; j++) {
                if (j > 0) sb.append(" | ");
                sb.append(row[j]);
            }
            System.out.println(sb);
        }
    }

    private static void printUsage() {
        System.out.println(
            "Usage:\n" +
            "  client db   <ior> insert <table> <c1,c2,...> <v1,v2,...>\n" +
            "  client db   <ior> update <table> <c1,c2,...> <v1,v2,...> <whereCol> <whereVal>\n" +
            "  client db   <ior> delete <table> <whereCol> <whereVal>\n" +
            "  client db   <ior> list   <table>\n" +
            "  client db   <ior> select <table> <whereCol> <whereVal>\n" +
            "  client file <ior> create <filename>\n" +
            "  client file <ior> write  <filename> <content> <append:0|1>\n" +
            "  client file <ior> read   <filename>\n" +
            "  client file <ior> delete <filename>\n" +
            "  client sync export-to-file   <dbIor> <fileIor> <table> <outputFilename> [delimiter=;]\n" +
            "  client sync import-from-file <dbIor> <fileIor> <inputFilename> <table> <c1,c2,...> [delimiter=;]"
        );
    }

    public static void main(String[] args) {
        try {
            ORB orb = ORB.init(args, null);

            if (args.length < 2) { printUsage(); return; }
            String mode = args[0];

            switch (mode) {
                case "db": {
                    if (args.length < 3) { printUsage(); return; }
                    DBService db = DBServiceHelper.narrow(resolveRef(orb, args[1]));
                    String op = args[2];
                    switch (op) {
                        case "insert":
                            if (args.length < 6) { printUsage(); return; }
                            boolean ok1 = db.insert(args[3], args[4].split(",", -1), args[5].split(",", -1));
                            System.out.println(ok1 ? "OK" : "echec");
                            break;
                        case "update":
                            if (args.length < 8) { printUsage(); return; }
                            boolean ok2 = db.update(args[3], args[4].split(",", -1), args[5].split(",", -1),
                                                     args[6], args[7]);
                            System.out.println(ok2 ? "OK" : "echec");
                            break;
                        case "delete":
                            if (args.length < 6) { printUsage(); return; }
                            boolean ok3 = db.remove(args[3], args[4], args[5]);
                            System.out.println(ok3 ? "OK" : "echec");
                            break;
                        case "list":
                            if (args.length < 4) { printUsage(); return; }
                            printRows(db.list(args[3]));
                            break;
                        case "select":
                            if (args.length < 6) { printUsage(); return; }
                            printRows(db.select(args[3], args[4], args[5]));
                            break;
                        default:
                            printUsage();
                    }
                    break;
                }
                case "file": {
                    if (args.length < 3) { printUsage(); return; }
                    FileService fs = FileServiceHelper.narrow(resolveRef(orb, args[1]));
                    String op = args[2];
                    switch (op) {
                        case "create":
                            if (args.length < 4) { printUsage(); return; }
                            System.out.println(fs.create(args[3]) ? "OK" : "echec");
                            break;
                        case "write":
                            if (args.length < 6) { printUsage(); return; }
                            boolean append = "1".equals(args[5]);
                            System.out.println(fs.write(args[3], args[4], append) ? "OK" : "echec");
                            break;
                        case "read":
                            if (args.length < 4) { printUsage(); return; }
                            System.out.println(fs.read(args[3]));
                            break;
                        case "delete":
                            if (args.length < 4) { printUsage(); return; }
                            System.out.println(fs.remove(args[3]) ? "OK" : "echec");
                            break;
                        default:
                            printUsage();
                    }
                    break;
                }
                case "sync": {
                    if (args.length < 2) { printUsage(); return; }
                    String syncOp = args[1];
                    if ("export-to-file".equals(syncOp) && args.length >= 6) {
                        DBService db = DBServiceHelper.narrow(resolveRef(orb, args[2]));
                        FileService fs = FileServiceHelper.narrow(resolveRef(orb, args[3]));
                        String table = args[4];
                        String outFile = args[5];
                        String delim = args.length >= 7 ? args[6] : ";";

                        String[][] rows = db.list(table);
                        StringBuilder content = new StringBuilder();
                        for (String[] row : rows) {
                            for (int j = 0; j < row.length; j++) {
                                if (j > 0) content.append(delim);
                                content.append(row[j]);
                            }
                            content.append("\n");
                        }
                        boolean ok = fs.write(outFile, content.toString(), false);
                        System.out.println("Export " + (ok ? "reussi" : "echoue") + " : " + rows.length
                                + " ligne(s) (dont l'en-tete) envoyees au FileService distant, ecrites dans '"
                                + outFile + "'.");

                    } else if ("import-from-file".equals(syncOp) && args.length >= 7) {
                        DBService db = DBServiceHelper.narrow(resolveRef(orb, args[2]));
                        FileService fs = FileServiceHelper.narrow(resolveRef(orb, args[3]));
                        String inFile = args[4];
                        String table = args[5];
                        String[] columns = args[6].split(",", -1);
                        String delim = args.length >= 8 ? args[7] : ";";

                        String[][] rows = fs.readAsRows(inFile, delim);
                        boolean ok = db.createTableAndInsertRows(table, columns, rows);
                        System.out.println("Import " + (ok ? "reussi" : "echoue") + " : " + rows.length
                                + " ligne(s) lues via le FileService distant dans '" + inFile
                                + "', inserees dans la table '" + table + "' via le DBService distant.");
                    } else {
                        printUsage();
                    }
                    break;
                }
                default:
                    printUsage();
            }
        } catch (ServiceException ex) {
            System.err.println("Erreur de service distante : " + ex.message);
            System.exit(1);
        } catch (org.omg.CORBA.SystemException ex) {
            System.err.println("Erreur CORBA : " + ex);
            System.exit(1);
        }
    }
}
