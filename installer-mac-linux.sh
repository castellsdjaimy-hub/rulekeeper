#!/usr/bin/env bash
# Rule Keeper - installation automatique (Mac / Linux)
set -e
cd "$(dirname "$0")"

if ! command -v java >/dev/null 2>&1; then
  echo "[ERREUR] Java n'est pas installé. Installe Java 17 (Temurin 17 sur adoptium.net) puis relance."
  exit 1
fi

if [ "$(uname)" = "Darwin" ]; then
  MC="$HOME/Library/Application Support/minecraft"
else
  MC="$HOME/.minecraft"
fi
FORGE_VER="1.20.1-47.3.0"
GRADLE_VER="8.1.1"
GRADLE_DIR="$PWD/.gradle-dist"

# 1. Forge
if [ ! -d "$MC/versions/1.20.1-forge-47.3.0" ]; then
  echo "[1/3] Installation de Forge $FORGE_VER..."
  echo "      (Si ça échoue : lance une fois Minecraft Java 1.20.1 normal, puis relance ce script)"
  curl -L -o forge-installer.jar "https://maven.minecraftforge.net/net/minecraftforge/forge/$FORGE_VER/forge-$FORGE_VER-installer.jar"
  java -jar forge-installer.jar --installClient
  rm -f forge-installer.jar
else
  echo "[1/3] Forge déjà installé."
fi

# 2. Compilation
if [ ! -x "$GRADLE_DIR/gradle-$GRADLE_VER/bin/gradle" ]; then
  echo "[2/3] Téléchargement de Gradle..."
  mkdir -p "$GRADLE_DIR"
  curl -L -o gradle.zip "https://services.gradle.org/distributions/gradle-$GRADLE_VER-bin.zip"
  unzip -q -o gradle.zip -d "$GRADLE_DIR"
  rm -f gradle.zip
fi
echo "[2/3] Compilation du mod (la première fois, ça peut prendre 5 à 10 minutes)..."
"$GRADLE_DIR/gradle-$GRADLE_VER/bin/gradle" build --no-daemon

# 3. Copie dans mods
echo "[3/3] Copie du mod dans Minecraft..."
mkdir -p "$MC/mods"
cp -f build/libs/rulekeeper-1.20.1-1.0.0.jar "$MC/mods/"

echo
echo "Terminé ! Dans le lanceur Minecraft, choisis le profil \"forge\" et lance le jeu."
echo "Dans ton monde : /regles menu"
