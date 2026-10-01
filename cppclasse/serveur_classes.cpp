#include "classes.hh"
#include <mysql/mysql.h>
#include <cstring>
#include <cstdlib>
#include <string>
#include <iostream>
#include <fstream>

class GestionClassesImpl : public POA_Classes::GestionClasses
{
private:
    MYSQL *connexion_;

    std::string echapper(const char *texte)
    {
        size_t taille = strlen(texte);
        std::string sortie(2 * taille + 1, '\0');
        unsigned long longueur = mysql_real_escape_string(connexion_, &sortie[0], texte, taille);
        sortie.resize(longueur);
        return sortie;
    }

    Classes::ListeNoms *lireNoms(const std::string &requete, const char *messageSiVide)
    {
        mysql_query(connexion_, requete.c_str());
        MYSQL_RES *resultat = mysql_store_result(connexion_);

        unsigned long nombre = mysql_num_rows(resultat);
        if (nombre == 0 && messageSiVide != nullptr)
        {
            Classes::ErreurClasse erreur;
            erreur.message = messageSiVide;
            throw erreur;
        }

        Classes::ListeNoms *liste = new Classes::ListeNoms;
        liste->length(nombre);
        for (unsigned long i = 0; i < nombre; ++i)
        {
            MYSQL_ROW rangee = mysql_fetch_row(resultat);
            (*liste)[i] = static_cast<const char *>(rangee[0]);
        }
        return liste;
    }

public:
    explicit GestionClassesImpl(MYSQL *connexion)
    {
        connexion_ = connexion;
    }

    virtual Classes::ListeNoms *listerClasses()
    {
        return lireNoms("SELECT DISTINCT nom_classe FROM classe ORDER BY nom_classe", nullptr);
    }

    virtual Classes::ListeNoms *listerEtudiants(const char *nomClasse)
    {
        std::string requete = "SELECT nom_etudiant FROM classe WHERE nom_classe = '" + echapper(nomClasse) + "' ORDER BY nom_etudiant";
        return lireNoms(requete, "Classe introuvable");
    }
};

int main(int argc, char **argv)
{
    MYSQL *connexion = mysql_init(nullptr);
    if (mysql_real_connect(connexion, "localhost", "classes_user", "classes_pass", "classes_db", 0, "/opt/lampp/var/mysql/mysql.sock", 0) == nullptr)
    {
        std::cerr << "Connexion MySQL impossible : " << mysql_error(connexion) << std::endl;
        return 1;
    }
    CORBA::ORB_var orb = CORBA::ORB_init(argc, argv);
    CORBA::Object_var objetPoa = orb->resolve_initial_references("RootPOA");
    PortableServer::POA_var poa = PortableServer::POA::_narrow(objetPoa);
    GestionClassesImpl *servant = new GestionClassesImpl(connexion);
    PortableServer::ObjectId_var identifiant = poa->activate_object(servant);
    CORBA::Object_var reference = servant->_this();
    CORBA::String_var ior = orb->object_to_string(reference);
    std::ofstream fichierIor("classes.ior");
    fichierIor << static_cast<const char *>(ior);
    fichierIor.close();
    PortableServer::POAManager_var gestionnaire = poa->the_POAManager();
    gestionnaire->activate();
    std::cout << "Serveur pret" << std::endl;
    orb->run();
    return 0;
}
