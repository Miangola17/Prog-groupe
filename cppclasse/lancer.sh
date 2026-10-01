#!/bin/bash
set -e
cd "$(dirname "$0")"

/opt/lampp/bin/mysql -u root -p < base.sql

omniidl -bcxx classes.idl
g++ -std=c++17 -o serveur_classes serveur_classes.cpp classesSK.cc $(pkg-config --cflags --libs omniORB4) $(mysql_config --cflags --libs)

mkdir -p src classes_java
java -cp "lib/*" org.jacorb.idl.parser -d src classes.idl
javac -cp "lib/*" -d classes_java src/Classes/*.java ResultatClasse.java ServiceClasses.java ServiceNotes.java PanelClasse.java ClientNotes.java

rm -f classes.ior
./serveur_classes -ORBendPoint giop:tcp:127.0.0.1:2809 &
PID_SERVEUR=$!
trap 'kill $PID_SERVEUR 2>/dev/null' EXIT

while [ ! -s classes.ior ]; do
    kill -0 $PID_SERVEUR 2>/dev/null || { echo "Le serveur s'est arrete"; exit 1; }
    sleep 0.2
done
sleep 0.3

java -cp "classes_java:lib/*" ClientNotes
