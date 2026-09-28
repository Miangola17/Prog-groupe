#include "DBServiceImpl.h"
#include <cctype>
#include <cstring>
#include <vector>
#include <sstream>

// -------------------------------------------------------------------
// Construction / connexion
// -------------------------------------------------------------------

DBServiceImpl::DBServiceImpl(const std::string& host,
                              const std::string& user,
                              const std::string& password,
                              const std::string& database,
                              unsigned int port)
    : conn_(NULL)
{
    conn_ = mysql_init(NULL);
    if (!conn_) {
        throw AppServices::ServiceException("mysql_init a echoue (memoire insuffisante)");
    }

    if (!mysql_real_connect(conn_, host.c_str(), user.c_str(), password.c_str(),
                             database.c_str(), port, NULL, 0)) {
        std::string msg = std::string("Connexion MySQL impossible : ") + mysql_error(conn_);
        mysql_close(conn_);
        conn_ = NULL;
        throw AppServices::ServiceException(msg.c_str());
    }
}

DBServiceImpl::~DBServiceImpl() {
    if (conn_) {
        mysql_close(conn_);
    }
}

// -------------------------------------------------------------------
// Utilitaires prives
// -------------------------------------------------------------------

// Un nom de table/colonne ne doit contenir que des lettres, chiffres et
// underscores. C'est une protection simple contre l'injection SQL sur
// les IDENTIFIANTS (les valeurs, elles, passent par escapeValue()).
void DBServiceImpl::checkIdentifier(const std::string& name, const char* what) {
    if (name.empty()) {
        std::string msg = std::string(what) + " est vide";
        throw AppServices::ServiceException(msg.c_str());
    }
    for (size_t i = 0; i < name.size(); ++i) {
        char c = name[i];
        if (!std::isalnum(static_cast<unsigned char>(c)) && c != '_') {
            std::string msg = std::string(what) + " contient un caractere interdit : " + name;
            throw AppServices::ServiceException(msg.c_str());
        }
    }
}

std::string DBServiceImpl::escapeValue(const std::string& value) {
    std::vector<char> buffer(value.size() * 2 + 1);
    unsigned long len = mysql_real_escape_string(conn_, &buffer[0], value.c_str(), value.size());
    return std::string(&buffer[0], len);
}

void DBServiceImpl::raiseServiceException(const std::string& context) {
    std::string msg = context + " : " + mysql_error(conn_);
    throw AppServices::ServiceException(msg.c_str());
}

// Execute un SELECT et transforme le resultat en RowSeq CORBA.
// La toute premiere ligne renvoyee contient les NOMS DE COLONNES
// (pratique pour l'affichage cote client) ; les lignes suivantes sont
// les donnees.
::AppServices::RowSeq* DBServiceImpl::runSelectQuery(const std::string& query) {
    if (mysql_query(conn_, query.c_str()) != 0) {
        raiseServiceException("Erreur lors du SELECT");
    }

    MYSQL_RES* res = mysql_store_result(conn_);
    if (!res) {
        raiseServiceException("Impossible de recuperer le resultat du SELECT");
    }

    unsigned int numFields = mysql_num_fields(res);
    MYSQL_FIELD* fields = mysql_fetch_fields(res);
    my_ulonglong numRows = mysql_num_rows(res);

    ::AppServices::RowSeq* result = new ::AppServices::RowSeq();
    result->length(static_cast<CORBA::ULong>(numRows) + 1);

    // Ligne 0 = en-tetes
    // ATTENTION (piege omniORB) : fields[i].name est un char* NON const.
    // Une affectation depuis un char* non-const est interpretee par
    // omniORB comme un TRANSFERT DE PROPRIETE du buffer (il le liberera
    // lui-meme plus tard), au lieu d'une copie. Or ce buffer appartient a
    // MySQL et sera libere par mysql_free_result() -> double free / crash.
    // On force donc le passage par un const char* pour obtenir une copie.
    ::AppServices::StringSeq header;
    header.length(numFields);
    for (unsigned int i = 0; i < numFields; ++i) {
        const char* name = fields[i].name;
        header[i] = name;
    }
    (*result)[0] = header;

    // Lignes suivantes = donnees (NULL -> chaine vide)
    CORBA::ULong idx = 1;
    MYSQL_ROW row;
    while ((row = mysql_fetch_row(res)) != NULL) {
        ::AppServices::StringSeq r;
        r.length(numFields);
        for (unsigned int i = 0; i < numFields; ++i) {
            const char* val = row[i] ? row[i] : "";
            r[i] = val;
        }
        (*result)[idx++] = r;
    }

    mysql_free_result(res);
    return result;
}

// -------------------------------------------------------------------
// Operations IDL
// -------------------------------------------------------------------

::CORBA::Boolean DBServiceImpl::insert(const char* table,
                                        const ::AppServices::StringSeq& columns,
                                        const ::AppServices::StringSeq& values) {
    checkIdentifier(table, "le nom de table");
    if (columns.length() != values.length()) {
        throw AppServices::ServiceException("insert: le nombre de colonnes et de valeurs doit etre identique");
    }
    for (CORBA::ULong i = 0; i < columns.length(); ++i) {
        checkIdentifier(std::string(columns[i]), "un nom de colonne");
    }

    std::ostringstream q;
    q << "INSERT INTO `" << table << "` (";
    for (CORBA::ULong i = 0; i < columns.length(); ++i) {
        if (i) q << ", ";
        q << "`" << std::string(columns[i]) << "`";
    }
    q << ") VALUES (";
    for (CORBA::ULong i = 0; i < values.length(); ++i) {
        if (i) q << ", ";
        q << "'" << escapeValue(std::string(values[i])) << "'";
    }
    q << ")";

    if (mysql_query(conn_, q.str().c_str()) != 0) {
        raiseServiceException("Erreur lors de l'INSERT");
    }
    return true;
}

::CORBA::Boolean DBServiceImpl::update(const char* table,
                                        const ::AppServices::StringSeq& setColumns,
                                        const ::AppServices::StringSeq& setValues,
                                        const char* whereColumn,
                                        const char* whereValue) {
    checkIdentifier(table, "le nom de table");
    checkIdentifier(whereColumn, "la colonne du WHERE");
    if (setColumns.length() != setValues.length()) {
        throw AppServices::ServiceException("update: le nombre de colonnes et de valeurs doit etre identique");
    }
    for (CORBA::ULong i = 0; i < setColumns.length(); ++i) {
        checkIdentifier(std::string(setColumns[i]), "un nom de colonne");
    }

    std::ostringstream q;
    q << "UPDATE `" << table << "` SET ";
    for (CORBA::ULong i = 0; i < setColumns.length(); ++i) {
        if (i) q << ", ";
        q << "`" << std::string(setColumns[i]) << "` = '" << escapeValue(std::string(setValues[i])) << "'";
    }
    q << " WHERE `" << whereColumn << "` = '" << escapeValue(whereValue) << "'";

    if (mysql_query(conn_, q.str().c_str()) != 0) {
        raiseServiceException("Erreur lors de l'UPDATE");
    }
    return true;
}

::CORBA::Boolean DBServiceImpl::remove(const char* table,
                                        const char* whereColumn,
                                        const char* whereValue) {
    checkIdentifier(table, "le nom de table");
    checkIdentifier(whereColumn, "la colonne du WHERE");

    std::ostringstream q;
    q << "DELETE FROM `" << table << "` WHERE `" << whereColumn << "` = '"
      << escapeValue(whereValue) << "'";

    if (mysql_query(conn_, q.str().c_str()) != 0) {
        raiseServiceException("Erreur lors du DELETE");
    }
    return true;
}

::AppServices::RowSeq* DBServiceImpl::list(const char* table) {
    checkIdentifier(table, "le nom de table");
    std::ostringstream q;
    q << "SELECT * FROM `" << table << "`";
    return runSelectQuery(q.str());
}

::AppServices::RowSeq* DBServiceImpl::select(const char* table,
                                              const char* whereColumn,
                                              const char* whereValue) {
    checkIdentifier(table, "le nom de table");
    checkIdentifier(whereColumn, "la colonne du WHERE");
    std::ostringstream q;
    q << "SELECT * FROM `" << table << "` WHERE `" << whereColumn << "` = '"
      << escapeValue(whereValue) << "'";
    return runSelectQuery(q.str());
}

::CORBA::Boolean DBServiceImpl::createTableAndInsertRows(const char* table,
                                                          const ::AppServices::StringSeq& columns,
                                                          const ::AppServices::RowSeq& rows) {
    checkIdentifier(table, "le nom de table");
    for (CORBA::ULong i = 0; i < columns.length(); ++i) {
        checkIdentifier(std::string(columns[i]), "un nom de colonne");
    }

    // 1) creation de la table si besoin (toutes les colonnes en TEXT,
    //    volontairement simple : ce n'est pas un outil de migration
    //    de schema, juste un moyen rapide d'importer un fichier).
    std::ostringstream create;
    create << "CREATE TABLE IF NOT EXISTS `" << table << "` (";
    for (CORBA::ULong i = 0; i < columns.length(); ++i) {
        if (i) create << ", ";
        create << "`" << std::string(columns[i]) << "` TEXT";
    }
    create << ")";
    if (mysql_query(conn_, create.str().c_str()) != 0) {
        raiseServiceException("Erreur lors du CREATE TABLE");
    }

    // 2) insertion de chaque ligne
    for (CORBA::ULong r = 0; r < rows.length(); ++r) {
        const ::AppServices::StringSeq& row = rows[r];
        if (row.length() != columns.length()) {
            throw AppServices::ServiceException("createTableAndInsertRows: une ligne n'a pas le bon nombre de colonnes");
        }
        std::ostringstream q;
        q << "INSERT INTO `" << table << "` (";
        for (CORBA::ULong i = 0; i < columns.length(); ++i) {
            if (i) q << ", ";
            q << "`" << std::string(columns[i]) << "`";
        }
        q << ") VALUES (";
        for (CORBA::ULong i = 0; i < row.length(); ++i) {
            if (i) q << ", ";
            q << "'" << escapeValue(std::string(row[i])) << "'";
        }
        q << ")";
        if (mysql_query(conn_, q.str().c_str()) != 0) {
            raiseServiceException("Erreur lors de l'INSERT (import fichier -> table)");
        }
    }

    return true;
}
