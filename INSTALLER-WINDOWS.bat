@echo off
chcp 65001 >nul
setlocal
cd /d "%~dp0"
title Rule Keeper - Installation en 1 clic

echo ==========================================
echo   Rule Keeper - installation automatique
echo ==========================================
echo.

where java >nul 2>nul
if errorlevel 1 (
  echo [ERREUR] Java n'est pas installe. Installe Java 17 ^(Temurin 17 sur adoptium.net^) puis relance ce fichier.
  pause
  exit /b 1
)

set MC=%APPDATA%\.minecraft
set FORGE_VER=1.20.1-47.3.0
set GRADLE_VER=8.1.1
set GRADLE_DIR=%~dp0.gradle-dist

REM --- 1. Forge (installe seulement s'il manque) ---
if not exist "%MC%\versions\1.20.1-forge-47.3.0" (
  echo [1/3] Installation de Forge %FORGE_VER%...
  echo       ^(Si ca echoue : lance une fois Minecraft Java 1.20.1 normal, puis relance ce fichier^)
  powershell -NoProfile -Command "[Net.ServicePointManager]::SecurityProtocol='Tls12'; Invoke-WebRequest https://maven.minecraftforge.net/net/minecraftforge/forge/%FORGE_VER%/forge-%FORGE_VER%-installer.jar -OutFile forge-installer.jar"
  java -jar forge-installer.jar --installClient
  del forge-installer.jar >nul 2>nul
) else (
  echo [1/3] Forge deja installe.
)

REM --- 2. Compilation du mod ---
if not exist "%GRADLE_DIR%\gradle-%GRADLE_VER%\bin\gradle.bat" (
  echo [2/3] Telechargement de Gradle...
  powershell -NoProfile -Command "[Net.ServicePointManager]::SecurityProtocol='Tls12'; Invoke-WebRequest https://services.gradle.org/distributions/gradle-%GRADLE_VER%-bin.zip -OutFile gradle.zip; Expand-Archive gradle.zip -DestinationPath '%GRADLE_DIR%' -Force; Remove-Item gradle.zip"
)
echo [2/3] Compilation du mod ^(la premiere fois, ca peut prendre 5 a 10 minutes^)...
call "%GRADLE_DIR%\gradle-%GRADLE_VER%\bin\gradle.bat" build --no-daemon
if errorlevel 1 (
  echo.
  echo [ERREUR] La compilation a echoue. Fais une capture de ce texte et envoie-la.
  pause
  exit /b 1
)

REM --- 3. Copie dans le dossier mods ---
echo [3/3] Copie du mod dans Minecraft...
if not exist "%MC%\mods" mkdir "%MC%\mods"
copy /y "build\libs\rulekeeper-1.20.1-1.0.0.jar" "%MC%\mods\" >nul

echo.
echo ==========================================
echo   Termine ! Dans le lanceur Minecraft,
echo   choisis le profil "forge" et lance le jeu.
echo   Dans ton monde : /regles menu
echo ==========================================
pause
