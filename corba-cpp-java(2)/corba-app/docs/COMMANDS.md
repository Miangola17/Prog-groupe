# CORBA C++ <-> Java : guide des commandes

## 1. Principe (a lire avant le code)

```
                 AppServices.idl   (le contrat, aucune logique)
                  /                \
        omniidl (C++)              idlj (Java)
              |                        |
   DBServiceImpl.h / .cpp      FileServiceImpl.java
   (logique metier MySQL)      (logique metier fichiers)
              |                        |
        DBServer (C++)           FileServer (Java)      <- serveurs
              |                        |
      ecrit dbservice.ior      ecrit fileservice.ior
              \                        /
        Client C++  ou  Client Java   (memes commandes)  <- clients
```

- L'IDL declare 2 interfaces : `DBService` (implemente en C++, MySQL) et
  `FileService` (implemente en Java, fichiers).
- Chaque serveur cree son objet CORBA et ecrit sa reference (IOR) dans un
  fichier texte. Le client lit ce fichier pour se connecter.
- Un client C++ ou Java peut appeler n'importe quel serveur : seul l'IDL compte.
- Les fonctions `sync` orchestrent les DEUX serveurs depuis le client
  (base -> fichier, fichier -> table MySQL).
- Pas de Naming Service : l'echange de l'IOR par fichier suffit et evite
  un processus supplementaire.

## 2. Prerequis (Debian)

```bash
sudo apt install build-essential omniidl libomniorb4-dev \
                 default-libmysqlclient-dev mariadb-server openjdk-8-jdk
```

IMPORTANT : le JDK 8 est obligatoire pour la partie Java. Le support CORBA
(et l'outil `idlj`) a ete retire du JDK a partir de la version 11. Si
plusieurs JDK sont installes :

```bash
sudo update-alternatives --config java
sudo update-alternatives --config javac
export PATH=/usr/lib/jvm/java-8-openjdk-amd64/bin:$PATH   # pour idlj
```

Si `idlj` est introuvable (cas du Temurin 8, qui ne le fournit pas), pas de
probleme : le dossier `java/generated/` contient deja les souches generees, et
`./build.sh` les utilise automatiquement. Il faut simplement que `java` et
`javac` soient en version 8 (`java -version` doit afficher 1.8.x), aussi bien
pour compiler que pour lancer FileServer et Client.

## 3. Preparer MySQL / MariaDB

```bash
sudo mysql -e "CREATE DATABASE corbadb;
CREATE USER 'corbauser'@'localhost' IDENTIFIED BY 'corbapass';
GRANT ALL ON corbadb.* TO 'corbauser'@'localhost'; FLUSH PRIVILEGES;
USE corbadb; CREATE TABLE personnes (nom VARCHAR(100), age VARCHAR(10));"
```

## 4. Compilation

```bash
cd cpp  && make            # genere les souches via omniidl, compile dbserver + client
cd ../java && ./build.sh   # genere les souches via idlj, compile tout (JDK 8)
```

Si l'IDL change : `make` et `./build.sh` regenerent tout automatiquement.

## 5. Demarrer les serveurs (un terminal chacun)

Serveur C++ (MySQL) :
```bash
cd cpp
./dbserver --host 127.0.0.1 --user corbauser --password corbapass --db corbadb \
           --ior ../dbservice.ior
```
Options : `--dbport 3306`, `--ior <fichier>`.

Serveur Java (fichiers) :
```bash
cd java
java -cp build FileServer --ior ../fileservice.ior
```

Si le client n'arrive pas a se connecter (COMM_FAILURE), l'ORB annonce
peut-etre une mauvaise adresse IP dans l'IOR. Forcer le loopback :
```bash
./dbserver ... -ORBendPoint giop:tcp:127.0.0.1:
java -cp build FileServer --ior ../fileservice.ior -ORBServerHost 127.0.0.1
```

On peut lancer un seul serveur, ou les deux. Les clients peuvent etre lances
depuis n'importe quel dossier ; `<ior>` est un chemin de fichier ou une
chaine `IOR:...` litterale.

## 6. Commandes clients

Le client C++ (`cpp/client`) et le client Java (`java -cp java/build Client`)
ont EXACTEMENT la meme syntaxe. Ci-dessous `CLIENT` = l'un ou l'autre.

```bash
alias CLIENTCPP='./cpp/client'
alias CLIENTJAVA='java -cp java/build Client'
```

### 6.1 Base de donnees (DBService, serveur C++)

```bash
CLIENT db dbservice.ior insert personnes nom,age Alice,30
CLIENT db dbservice.ior update personnes age 31 nom Alice     # SET age=31 WHERE nom=Alice
CLIENT db dbservice.ior delete personnes nom Alice
CLIENT db dbservice.ior list   personnes
CLIENT db dbservice.ior select personnes nom Alice
```
La premiere ligne affichee par `list` / `select` contient les noms de colonnes.

### 6.2 Fichiers (FileService, serveur Java)

```bash
CLIENT file fileservice.ior create /tmp/a.txt
CLIENT file fileservice.ior write  /tmp/a.txt "bonjour" 0     # 0 = ecraser, 1 = ajouter a la fin
CLIENT file fileservice.ior read   /tmp/a.txt
CLIENT file fileservice.ior delete /tmp/a.txt
```
Les chemins sont ceux de la machine ou tourne le serveur Java.

### 6.3 Synchronisation (le client parle aux DEUX serveurs)

Base -> fichier (contenu d'une table ecrit dans un fichier cote serveur Java) :
```bash
CLIENT sync export-to-file dbservice.ior fileservice.ior personnes /tmp/export.csv ";"
```

Fichier -> table MySQL (une ligne par enregistrement, champs separes par le
delimiteur, SANS ligne d'en-tete ; la table est creee si besoin, colonnes en TEXT) :
```bash
printf 'Paris;75000\nLyon;69000\n' > /tmp/villes.csv
CLIENT sync import-from-file dbservice.ior fileservice.ior /tmp/villes.csv villes ville,code_postal ";"
```
Le delimiteur est optionnel (";" par defaut). Exemple "liste de noms -> table" :
un fichier avec un nom par ligne + `import-from-file ... noms nom`.

## 7. Matrice de test croisee

| Client | Serveur | Commande |
|--------|---------|----------|
| Java   | C++ (DB)   | `java -cp java/build Client db dbservice.ior list personnes` |
| C++    | Java (fichier) | `./cpp/client file fileservice.ior read /tmp/a.txt` |
| C++    | C++ (DB)   | `./cpp/client db dbservice.ior list personnes` |
| Java   | Java (fichier) | `java -cp java/build Client file fileservice.ior read /tmp/a.txt` |

## 8. Ajouter une nouvelle fonction (meme workflow a chaque fois)

1. `idl/AppServices.idl` : declarer l'operation dans l'interface.
2. C++ : declarer dans `DBServiceImpl.h`, ecrire la logique dans `DBServiceImpl.cpp`.
   Java : ajouter la methode dans `FileServiceImpl.java`.
3. `make` (cpp) et `./build.sh` (java) regenerent les souches.
4. Client : ajouter une branche de commande dans `Client.cpp` et `Client.java`.
Les serveurs n'ont pas besoin d'autre modification.

## 9. Notes techniques

- `delete` est un mot reserve en C++/Java : l'operation IDL s'appelle `remove`
  (la commande terminal reste `delete`).
- Les noms de tables/colonnes sont limites a `[A-Za-z0-9_]` (anti-injection SQL) ;
  les valeurs sont echappees avec `mysql_real_escape_string`.
- Piege omniORB corrige dans le code : assigner un `char*` non-const a une
  sequence de chaines TRANSFERE la propriete du buffer (double free). Toujours
  passer par un `const char*` (voir `runSelectQuery`).
- Erreurs distantes : renvoyees par l'exception IDL `ServiceException`.
