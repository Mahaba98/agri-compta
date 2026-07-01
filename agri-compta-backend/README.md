# Backend - Gestion comptable agricole

Backend Spring Boot de l'application de gestion comptable et de suivi des couts de production pour une exploitation agricole.

## Technologies

- Java 21
- Spring Boot
- Spring Web MVC
- Spring Data JPA
- PostgreSQL
- Validation Jakarta
- Lombok
- Gradle Wrapper

## Lancement

Creer une base PostgreSQL :

```sql
CREATE DATABASE agri_compta;
```

Configurer si besoin `src/main/resources/application.properties`, puis lancer :

```powershell
.\gradlew.bat bootRun
```

L'API demarre sur :

```text
http://localhost:8080
```

## Endpoints principaux

- `GET /api/campagnes`
- `GET /api/parcelles`
- `GET /api/cultures`
- `GET /api/depenses`
- `GET /api/recettes`
- `GET /api/produits-stock`
- `POST /api/mouvements-stock`
- `GET /api/dashboard`

## Commandes utiles

```powershell
.\gradlew.bat test
.\gradlew.bat build
.\gradlew.bat bootRun
```

Les anciens fichiers Maven ont ete archives dans `maven-backup`.

## Regles metier deja implementees

- Une depense est obligatoirement associee a une culture.
- Une recette est obligatoirement associee a une culture.
- Une culture est associee a une campagne et une parcelle.
- Le stock ne peut pas devenir negatif lors d'une sortie.
- Le tableau de bord calcule les depenses, recettes, benefices et couts par hectare.
