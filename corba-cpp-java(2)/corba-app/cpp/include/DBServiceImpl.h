#ifndef DBSERVICEIMPL_H
#define DBSERVICEIMPL_H

// Ce header declare la classe qui porte la LOGIQUE METIER cote C++.
// Elle herite de POA_AppServices::DBService (le squelette genere par
// omniidl a partir de AppServices.idl) et implemente chaque operation
// avec de vrais appels a la bibliotheque cliente MySQL (libmysqlclient).

#include "AppServices.h"
#include <mysql/mysql.h>
#include <string>

class DBServiceImpl : public POA_AppServices::DBService {
public:
    DBServiceImpl(const std::string& host,
                  const std::string& user,
                  const std::string& password,
                  const std::string& database,
                  unsigned int port);
    virtual ~DBServiceImpl();

    // --- operations definies dans l'IDL (module AppServices::DBService) ---
    virtual ::CORBA::Boolean insert(const char* table,
                                     const ::AppServices::StringSeq& columns,
                                     const ::AppServices::StringSeq& values);

    virtual ::CORBA::Boolean update(const char* table,
                                     const ::AppServices::StringSeq& setColumns,
                                     const ::AppServices::StringSeq& setValues,
                                     const char* whereColumn,
                                     const char* whereValue);

    virtual ::CORBA::Boolean remove(const char* table,
                                     const char* whereColumn,
                                     const char* whereValue);

    virtual ::AppServices::RowSeq* list(const char* table);

    virtual ::AppServices::RowSeq* select(const char* table,
                                           const char* whereColumn,
                                           const char* whereValue);

    virtual ::CORBA::Boolean createTableAndInsertRows(const char* table,
                                                        const ::AppServices::StringSeq& columns,
                                                        const ::AppServices::RowSeq& rows);

private:
    MYSQL* conn_;

    // Petits utilitaires internes (pas exposes par l'IDL)
    static void checkIdentifier(const std::string& name, const char* what);
    std::string escapeValue(const std::string& value);
    ::AppServices::RowSeq* runSelectQuery(const std::string& query);
    void raiseServiceException(const std::string& context);
};

#endif
