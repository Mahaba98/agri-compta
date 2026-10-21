# Roles et autorisations

Les controles sont appliques dans `ApiAuthenticationFilter` avec la politique
`ApiPermissions`. Toute ressource ou action non explicitement autorisee est refusee.
L'interface masque les commandes correspondantes et protege aussi l'ouverture des
formulaires. La securite effective reste celle du serveur.

| Module | AGRICULTEUR | COMPTABLE | ADMIN |
| --- | --- | --- | --- |
| Tableau de bord et rapports CSV | Lecture et export | Lecture et export | Lecture et export |
| Campagnes, parcelles, cultures | Creation, lecture, modification | Lecture | Lecture |
| Operations et recoltes | Creation, lecture, modification | Lecture | Lecture |
| Produits en stock | Creation, lecture, modification | Lecture | Lecture |
| Mouvements de stock | Creation et lecture | Lecture | Lecture |
| Depenses et recettes | Creation et lecture | Creation, lecture, correction et annulation | Lecture |
| Historique financier | Lecture | Lecture | Lecture |
| Categories de depenses | Lecture | Creation, lecture, modification | Creation, lecture, modification |
| Exploitations | Lecture de son exploitation | Lecture de son exploitation | Administration |
| Utilisateurs | Aucun acces administratif | Aucun acces administratif | Administration |

Les donnees metier sont bornees a l'exploitation affectee au compte, y compris
pour ADMIN. L'administrateur gere la liste generale des comptes et exploitations ;
cela ne constitue pas une bascule automatique vers leurs donnees metier.
Un utilisateur reste rattache a une seule exploitation dans cette version.

Les lectures par identifiant et les associations de saisie verifient egalement
l'exploitation. Une ressource etrangere retourne 404 pour ne pas divulguer son
existence. Les categories sont le referentiel commun existant.

## Corrections et annulations financieres

Les routes DELETE des depenses et recettes sont desactivees. Le comptable utilise :

```http
POST /api/depenses/{id}/annulation
Content-Type: application/json
Authorization: Bearer <jeton>

{"motif":"Double saisie"}
```

Les memes routes existent pour `/api/recettes`. Le motif doit contenir de 1 a 500
caracteres non vides. Une ecriture annulee est conservee, non modifiable et exclue
des totaux du tableau de bord, des rapports et du CSV. Son statut reste visible.
`GET /api/depenses/{id}/historique` ou `/api/recettes/{id}/historique` donne le journal.
Les creations, corrections et annulations enregistrent l'auteur serveur, la date,
les valeurs avant et apres ou le motif. L'operation et son historique partagent une
transaction. Les anciennes ecritures ne recoivent pas d'historique retroactif.

## Sessions et demarrage

Les comptes desactives, les exploitations inactives et les jetons dont le role,
l'adresse ou l'affectation ont change sont refuses. Reconnexion necessaire apres
ces changements. `APP_AUTH_SECRET` permet de fournir le secret de signature ; en
son absence un secret aleatoire est genere et les sessions expirent au redemarrage.

Les ajouts de schema sont pris en charge par le mode Hibernate `update` configure :
colonnes d'annulation dans les depenses et recettes, table `journal_financier`.
Aucune donnee existante n'est supprimee. Les suppressions des donnees agricoles et
des mouvements sont egalement fermees par la politique ; utiliser les statuts et
les ajustements prevus dans les parcours.

Les tests backend utilisent H2 en memoire, sans se connecter a PostgreSQL. Le
Gradle Wrapper reste en version 9.5.0. `gradle.properties` utilise l'encodage natif
pour les fichiers d'arguments des workers sous Windows ; les sources Java et les
tests restent en UTF-8. Ce reglage permet l'execution depuis le dossier `Memoire`
avec un accent sous Windows et JDK 21.

## Verification

```powershell
.\gradlew.bat test
```

Dans le frontend : `npm test -- --watch=false` et `npm run build`.
