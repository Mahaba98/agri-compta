# Agri Compta

Application de gestion comptable agricole pour suivre les campagnes, parcelles, cultures, recoltes, depenses, recettes, stocks, mouvements de stock et rapports.

## Structure

- `Mémoire/agri-compta-backend/` : API Spring Boot.
- `Mémoire/agri-compta-frontend/` : interface Angular.
- `Mémoire/memoire_taysir_agro_roles_actualises_20261021.docx` : memoire actualise.
- `Document/` : documents de conception et livrables precedents.

## Demarrage sous Windows

Prerequis : JDK 21, Node.js compatible avec Angular 22 et PostgreSQL demarre.
Creer la base `agri_compta` et adapter les parametres PostgreSQL du backend a la machine.

Backend, dans un terminal PowerShell :

```powershell
cd "Mémoire/agri-compta-backend"
.\gradlew.bat bootRun
```

Frontend, dans un second terminal a la racine du depot :

```powershell
cd "Mémoire/agri-compta-frontend"
npm.cmd ci --include=dev
npm.cmd start
```

`npm.cmd` evite le blocage de `npm.ps1` par la politique PowerShell.
L'installation des dependances est necessaire au premier lancement et apres modification du fichier de verrouillage.
Arreter un ancien serveur avant de reinstaller les dependances, pour eviter les fichiers verrouilles sous Windows.

- Interface : http://localhost:4200
- API : http://localhost:8080
- Arret des serveurs : `Ctrl+C` dans chaque terminal.

## Verification

Dans le backend :

```powershell
.\gradlew.bat test
```

Dans le frontend :

```powershell
npm.cmd test -- --watch=false
npm.cmd run build
```

La politique des roles et les routes d'annulation et d'historique sont decrites dans
`Mémoire/agri-compta-backend/docs/roles-et-autorisations.md`.
