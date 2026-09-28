#!/bin/bash
# Compile le cote Java.
# - Si idlj est disponible (JDK 8 d'OpenJDK), les souches CORBA sont
#   regenerees depuis l'IDL.
# - Sinon, on utilise les souches DEJA generees fournies dans generated/
#   (utile avec Temurin 8, qui ne fournit pas idlj).
# Dans les deux cas il faut un JDK 8 pour javac et pour l'execution
# (le support CORBA a ete retire du JDK a partir de la version 11).
set -e
cd "$(dirname "$0")"

JDK8=/usr/lib/jvm/java-8-openjdk-amd64
if [ -x "$JDK8/bin/idlj" ]; then
    export PATH="$JDK8/bin:$PATH"
fi

IDL=../idl/AppServices.idl
GEN_DIR=generated
BUILD_DIR=build

if command -v idlj >/dev/null 2>&1; then
    echo ">> Generation des souches Java depuis l'IDL (idlj)..."
    rm -rf "$GEN_DIR"
    idlj -fall -td "$GEN_DIR" "$IDL"
elif [ -d "$GEN_DIR/AppServices" ]; then
    echo ">> idlj introuvable : utilisation des souches deja generees dans $GEN_DIR/"
else
    echo "idlj introuvable et pas de dossier $GEN_DIR/ : installer openjdk-8-jdk"
    exit 1
fi

echo ">> Compilation (javac)..."
mkdir -p "$BUILD_DIR"
javac -nowarn -d "$BUILD_DIR" $(find "$GEN_DIR" src -name "*.java")

echo ">> OK. Classes compilees dans $BUILD_DIR/"
