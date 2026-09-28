import AppServices.ServiceException;
import AppServices.FileServicePOA;

import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Logique metier cote Java : acces au systeme de fichiers.
 * Cette classe herite du squelette genere par idlj (FileServicePOA) a
 * partir de AppServices.idl et implemente chaque operation avec l'API
 * standard java.io / java.nio.
 */
public class FileServiceImpl extends FileServicePOA {

    @Override
    public boolean create(String filename) throws ServiceException {
        File f = new File(filename);
        if (f.exists()) {
            throw new ServiceException("Le fichier existe deja : " + filename);
        }
        try {
            return f.createNewFile();
        } catch (IOException e) {
            throw new ServiceException("Impossible de creer le fichier : " + e.getMessage());
        }
    }

    @Override
    public boolean write(String filename, String content, boolean append) throws ServiceException {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(filename, append))) {
            w.write(content);
            return true;
        } catch (IOException e) {
            throw new ServiceException("Impossible d'ecrire dans le fichier : " + e.getMessage());
        }
    }

    @Override
    public String read(String filename) throws ServiceException {
        File f = new File(filename);
        if (!f.exists()) {
            throw new ServiceException("Fichier introuvable : " + filename);
        }
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(filename));
            return new String(bytes);
        } catch (IOException e) {
            throw new ServiceException("Impossible de lire le fichier : " + e.getMessage());
        }
    }

    @Override
    public boolean remove(String filename) throws ServiceException {
        File f = new File(filename);
        if (!f.exists()) {
            throw new ServiceException("Fichier introuvable : " + filename);
        }
        return f.delete();
    }

    @Override
    public String[][] readAsRows(String filename, String delimiter) throws ServiceException {
        File f = new File(filename);
        if (!f.exists()) {
            throw new ServiceException("Fichier introuvable : " + filename);
        }
        try {
            List<String> lines = Files.readAllLines(Paths.get(filename));
            List<String[]> rows = new ArrayList<>();
            String sep = Pattern.quote(delimiter);
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                rows.add(line.split(sep, -1));
            }
            return rows.toArray(new String[0][]);
        } catch (IOException e) {
            throw new ServiceException("Impossible de lire le fichier : " + e.getMessage());
        }
    }
}
