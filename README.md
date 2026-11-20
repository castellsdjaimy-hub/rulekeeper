# Rule Keeper (Forge 1.20.1)

Mod serveur : un admin définit des règles, le mod les fait respecter.

## Commandes
- `/regles` : affiche les règles actives (tout le monde)
- `/regles menu` : ouvre le menu cliquable (tout le monde le voit, seuls les admins peuvent cliquer pour activer / désactiver). Côté serveur uniquement, les clients n'ont pas besoin du mod.
- `/regles activer <regle>` / `/regles desactiver <regle>` (admin)
- `/regles ajouter <texte>` : règle libre, affichée seulement (admin)
- `/regles retirer <numero>` (admin)
- `/regles exempter <joueur>` / `/regles retablir <joueur>` (admin)
- `/regles ops <true|false>` : les opérateurs sont-ils exemptés ? (défaut : oui)

## Règles intégrées
pas_jeter, pas_casser, pas_poser, pas_ramasser, pas_pvp, pas_chat

Les réglages sont sauvegardés dans `config/rulekeeper.json`.

## Installation en 1 clic
Prérequis : Java 17 installé (Temurin 17 sur adoptium.net) et Minecraft Java lancé au moins une fois.
- Windows : double-clic sur `INSTALLER-WINDOWS.bat`
- Mac / Linux : `bash installer-mac-linux.sh`

Le script installe Forge 1.20.1 si besoin, compile le mod et le copie dans le dossier `mods`.
Ensuite, lance le profil "forge" dans le lanceur Minecraft.

## Compiler à la main
Il faut Java 17 et le wrapper Gradle de Forge MDK 1.20.1 (47.3.0) :
1. Télécharge le MDK 1.20.1 sur files.minecraftforge.net et copie `gradlew`, `gradlew.bat` et le dossier `gradle/` dans ce projet
2. Lance `./gradlew build`
3. Le mod est dans `build/libs/rulekeeper-1.20.1-1.0.0.jar` : mets-le dans le dossier `mods/` du serveur (et des clients si tu veux)
