// DBServer.cpp
//
// Serveur CORBA en C++. Il expose un objet DBService (implemente par
// DBServiceImpl, qui parle a MySQL) et publie sa reference CORBA (IOR)
// dans un fichier texte que n'importe quel client (C++ OU Java) pourra
// lire pour s'y connecter.
//
// Usage :
//   ./dbserver --host 127.0.0.1 --user root --password secret --db test
//              [--dbport 3306] [--ior dbservice.ior]
//
// On peut aussi passer des options omniORB standard, par exemple pour
// fixer un port TCP fixe (au lieu d'un port ephemere) :
//   ./dbserver ... -ORBendPoint giop:tcp::9999

#include "AppServices.h"
#include "DBServiceImpl.h"
#include <fstream>
#include <iostream>
#include <cstring>
#include <cstdlib>

static std::string getOpt(int argc, char** argv, const std::string& name, const std::string& def) {
    for (int i = 1; i < argc - 1; ++i) {
        if (name == argv[i]) return argv[i + 1];
    }
    return def;
}

int main(int argc, char** argv) {
    try {
        // IMPORTANT : on laisse d'abord l'ORB reconnaitre et retirer ses
        // propres options (-ORBxxx) de argc/argv. Ce qui reste ensuite
        // est a nous (--host, --user, ...).
        CORBA::ORB_var orb = CORBA::ORB_init(argc, argv);

        std::string host     = getOpt(argc, argv, "--host", "127.0.0.1");
        std::string user     = getOpt(argc, argv, "--user", "root");
        std::string password = getOpt(argc, argv, "--password", "");
        std::string db       = getOpt(argc, argv, "--db", "test");
        unsigned int dbport  = static_cast<unsigned int>(std::atoi(getOpt(argc, argv, "--dbport", "3306").c_str()));
        std::string iorFile  = getOpt(argc, argv, "--ior", "dbservice.ior");

        CORBA::Object_var poaObj = orb->resolve_initial_references("RootPOA");
        PortableServer::POA_var poa = PortableServer::POA::_narrow(poaObj);
        PortableServer::POAManager_var pman = poa->the_POAManager();

        std::cout << "Connexion a MySQL (" << user << "@" << host << ":" << dbport
                  << "/" << db << ") ..." << std::endl;
        DBServiceImpl* servant = new DBServiceImpl(host, user, password, db, dbport);

        PortableServer::ObjectId_var oid = poa->activate_object(servant);

        AppServices::DBService_var dbRef = servant->_this();
        CORBA::String_var iorStr = orb->object_to_string(dbRef);

        std::ofstream out(iorFile.c_str());
        out << iorStr << std::endl;
        out.close();

        std::cout << "=== Serveur DBService (C++ / MySQL) demarre ===" << std::endl;
        std::cout << "IOR ecrite dans : " << iorFile << std::endl;
        std::cout << "IOR : " << iorStr << std::endl;

        pman->activate();
        servant->_remove_ref();

        orb->run();

        orb->destroy();
    } catch (const AppServices::ServiceException& ex) {
        std::cerr << "Erreur de service : " << ex.message << std::endl;
        return 1;
    } catch (const CORBA::Exception& ex) {
        std::cerr << "Erreur CORBA : " << ex._name() << std::endl;
        return 1;
    }
    return 0;
}
