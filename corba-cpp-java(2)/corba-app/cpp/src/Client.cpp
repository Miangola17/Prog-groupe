// Client.cpp
//
// Client CORBA en C++, 100% terminal (pas d'interface graphique).
// Il peut appeler :
//   - un DBService   (peu importe qu'il soit implemente en C++ ou en Java)
//   - un FileService (peu importe qu'il soit implemente en C++ ou en Java)
// et meme orchestrer les DEUX en meme temps (mode "sync") pour copier des
// donnees de la base vers un fichier, ou d'un fichier vers la base.
//
// Voir docs/COMMANDS.md pour tous les exemples d'appel.

#include "AppServices.h"
#include <iostream>
#include <fstream>
#include <sstream>
#include <vector>
#include <string>
#include <cstdlib>

// Lit la premiere ligne non vide d'un fichier (utilise pour lire une IOR
// ecrite par un serveur).
static std::string readFirstLine(const std::string& path) {
    std::ifstream in(path.c_str());
    std::string line;
    std::getline(in, line);
    return line;
}

// L'argument peut etre soit une IOR litterale (IOR:... ou corbaloc:...),
// soit un chemin vers un fichier qui la contient.
static CORBA::Object_var resolveRef(CORBA::ORB_var& orb, const std::string& iorArg) {
    std::string iorStr = iorArg;
    if (iorArg.rfind("IOR:", 0) != 0 && iorArg.rfind("corbaloc:", 0) != 0) {
        iorStr = readFirstLine(iorArg);
        if (iorStr.empty()) {
            std::cerr << "Impossible de lire une IOR dans le fichier : " << iorArg << std::endl;
            std::exit(1);
        }
    }
    return orb->string_to_object(iorStr.c_str());
}

static std::vector<std::string> splitCsv(const std::string& s) {
    std::vector<std::string> out;
    std::istringstream ss(s);
    std::string item;
    while (std::getline(ss, item, ',')) out.push_back(item);
    return out;
}

static AppServices::StringSeq toStringSeq(const std::vector<std::string>& v) {
    AppServices::StringSeq seq;
    seq.length(static_cast<CORBA::ULong>(v.size()));
    for (size_t i = 0; i < v.size(); ++i) seq[i] = v[i].c_str();
    return seq;
}

static void printRows(AppServices::RowSeq_var& rows) {
    for (CORBA::ULong i = 0; i < rows->length(); ++i) {
        AppServices::StringSeq& row = rows[i];
        for (CORBA::ULong j = 0; j < row.length(); ++j) {
            if (j) std::cout << " | ";
            std::cout << (const char*) row[j];
        }
        std::cout << std::endl;
    }
}

static void printUsage() {
    std::cout <<
    "Usage:\n"
    "  client db   <ior> insert <table> <c1,c2,...> <v1,v2,...>\n"
    "  client db   <ior> update <table> <c1,c2,...> <v1,v2,...> <whereCol> <whereVal>\n"
    "  client db   <ior> delete <table> <whereCol> <whereVal>\n"
    "  client db   <ior> list   <table>\n"
    "  client db   <ior> select <table> <whereCol> <whereVal>\n"
    "  client file <ior> create <filename>\n"
    "  client file <ior> write  <filename> <content> <append:0|1>\n"
    "  client file <ior> read   <filename>\n"
    "  client file <ior> delete <filename>\n"
    "  client sync export-to-file   <dbIor> <fileIor> <table> <outputFilename> [delimiter=;]\n"
    "  client sync import-from-file <dbIor> <fileIor> <inputFilename> <table> <c1,c2,...> [delimiter=;]\n";
}

int main(int argc, char** argv) {
    try {
        CORBA::ORB_var orb = CORBA::ORB_init(argc, argv);

        if (argc < 3) { printUsage(); return 1; }
        std::string mode = argv[1];

        if (mode == "db") {
            if (argc < 4) { printUsage(); return 1; }
            CORBA::Object_var obj = resolveRef(orb, argv[2]);
            AppServices::DBService_var db = AppServices::DBService::_narrow(obj);
            if (CORBA::is_nil(db)) { std::cerr << "Reference DBService invalide\n"; return 1; }

            std::string op = argv[3];
            if (op == "insert" && argc >= 7) {
                AppServices::StringSeq cols = toStringSeq(splitCsv(argv[5]));
                AppServices::StringSeq vals = toStringSeq(splitCsv(argv[6]));
                CORBA::Boolean ok = db->insert(argv[4], cols, vals);
                std::cout << (ok ? "OK" : "echec") << std::endl;
            } else if (op == "update" && argc >= 9) {
                AppServices::StringSeq cols = toStringSeq(splitCsv(argv[5]));
                AppServices::StringSeq vals = toStringSeq(splitCsv(argv[6]));
                CORBA::Boolean ok = db->update(argv[4], cols, vals, argv[7], argv[8]);
                std::cout << (ok ? "OK" : "echec") << std::endl;
            } else if (op == "delete" && argc >= 7) {
                CORBA::Boolean ok = db->remove(argv[4], argv[5], argv[6]);
                std::cout << (ok ? "OK" : "echec") << std::endl;
            } else if (op == "list" && argc >= 5) {
                AppServices::RowSeq_var rows = db->list(argv[4]);
                printRows(rows);
            } else if (op == "select" && argc >= 7) {
                AppServices::RowSeq_var rows = db->select(argv[4], argv[5], argv[6]);
                printRows(rows);
            } else {
                printUsage(); return 1;
            }

        } else if (mode == "file") {
            if (argc < 4) { printUsage(); return 1; }
            CORBA::Object_var obj = resolveRef(orb, argv[2]);
            AppServices::FileService_var fs = AppServices::FileService::_narrow(obj);
            if (CORBA::is_nil(fs)) { std::cerr << "Reference FileService invalide\n"; return 1; }

            std::string op = argv[3];
            if (op == "create" && argc >= 5) {
                CORBA::Boolean ok = fs->create(argv[4]);
                std::cout << (ok ? "OK" : "echec") << std::endl;
            } else if (op == "write" && argc >= 7) {
                CORBA::Boolean append = (std::string(argv[6]) == "1");
                CORBA::Boolean ok = fs->write(argv[4], argv[5], append);
                std::cout << (ok ? "OK" : "echec") << std::endl;
            } else if (op == "read" && argc >= 5) {
                CORBA::String_var content = fs->read(argv[4]);
                std::cout << (const char*) content << std::endl;
            } else if (op == "delete" && argc >= 5) {
                CORBA::Boolean ok = fs->remove(argv[4]);
                std::cout << (ok ? "OK" : "echec") << std::endl;
            } else {
                printUsage(); return 1;
            }

        } else if (mode == "sync") {
            if (argc < 3) { printUsage(); return 1; }
            std::string syncOp = argv[2];

            if (syncOp == "export-to-file" && argc >= 7) {
                CORBA::Object_var dbObj   = resolveRef(orb, argv[3]);
                CORBA::Object_var fileObj = resolveRef(orb, argv[4]);
                AppServices::DBService_var   db = AppServices::DBService::_narrow(dbObj);
                AppServices::FileService_var fs = AppServices::FileService::_narrow(fileObj);
                std::string table   = argv[5];
                std::string outFile = argv[6];
                std::string delim   = (argc >= 8) ? argv[7] : ";";

                AppServices::RowSeq_var rows = db->list(table.c_str());
                std::ostringstream content;
                for (CORBA::ULong i = 0; i < rows->length(); ++i) {
                    AppServices::StringSeq& row = rows[i];
                    for (CORBA::ULong j = 0; j < row.length(); ++j) {
                        if (j) content << delim;
                        content << (const char*) row[j];
                    }
                    content << "\n";
                }
                CORBA::Boolean ok = fs->write(outFile.c_str(), content.str().c_str(), false);
                std::cout << "Export " << (ok ? "reussi" : "echoue") << " : " << rows->length()
                          << " ligne(s) (dont l'en-tete) envoyees au FileService distant, "
                          << "ecrites dans '" << outFile << "'." << std::endl;

            } else if (syncOp == "import-from-file" && argc >= 8) {
                CORBA::Object_var dbObj   = resolveRef(orb, argv[3]);
                CORBA::Object_var fileObj = resolveRef(orb, argv[4]);
                AppServices::DBService_var   db = AppServices::DBService::_narrow(dbObj);
                AppServices::FileService_var fs = AppServices::FileService::_narrow(fileObj);
                std::string inFile = argv[5];
                std::string table  = argv[6];
                std::vector<std::string> colVec = splitCsv(argv[7]);
                std::string delim  = (argc >= 9) ? argv[8] : ";";

                AppServices::StringSeq cols = toStringSeq(colVec);
                AppServices::RowSeq_var rows = fs->readAsRows(inFile.c_str(), delim.c_str());
                CORBA::Boolean ok = db->createTableAndInsertRows(table.c_str(), cols, rows.in());
                std::cout << "Import " << (ok ? "reussi" : "echoue") << " : " << rows->length()
                          << " ligne(s) lues via le FileService distant dans '" << inFile
                          << "', inserees dans la table '" << table
                          << "' via le DBService distant." << std::endl;
            } else {
                printUsage(); return 1;
            }

        } else {
            printUsage(); return 1;
        }

    } catch (const AppServices::ServiceException& ex) {
        std::cerr << "Erreur de service distante : " << (const char*) ex.message << std::endl;
        return 1;
    } catch (const CORBA::Exception& ex) {
        std::cerr << "Erreur CORBA : " << ex._name() << std::endl;
        return 1;
    }
    return 0;
}
