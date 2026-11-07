# Sécurité et reprise des opérations MCP

## Identité machine et droits

Chaque machine possède un jeton opaque aléatoire de 256 bits, préfixé `mds_`.
Seule son empreinte SHA-256 est persistée dans `MachineCredential`. Le jeton
ne dérive ni du mot de passe ni du cookie familial. La route est exclue du
contrôle familial uniquement pour appliquer son propre contrôle **obligatoire**.

- `read` : recherche, consultation, aperçu, consultation des reçus de cette machine.
- `write` : création d’ingrédients/recettes et application d’import.
- `read,write` : combinaison explicite pour l’assistant complet.

`write` n’implique pas `read`. Un accès lecture peut inspecter les schémas de
création, mais tout appel à une écriture est refusé. `MCP_ALLOW_WRITES=false`
ou absent désactive les trois mutations indépendamment des droits des jetons.
L’aperçu est réellement sans écriture ; ce n’est pas un import suivi d’un undo.

Les outils sont limités au catalogue et aux semaines de l’application familiale
actuelle, qui ne possède pas de modèle multi-foyers. Un jeton autorisé peut donc
lire ce catalogue et ces semaines, pas un sous-ensemble par personne.

## Révocation et rotation

Dans l’environnement opérateur de la base cible :

```sh
npm run mcp:credential -- revoke IDENTIFIANT_MACHINE
```

Le prochain appel HTTP et chaque outil reconsultent la base. Les mutations
prennent aussi un verrou partagé sur l’identité dans leur transaction, pour
sérialiser la révocation et la validation des droits. Une révocation attend une
mutation déjà engagée ; elle ne prétend pas annuler une opération déjà validée.
Après son commit, aucune nouvelle mutation ne peut utiliser le jeton révoqué.
Les lectures déjà en cours peuvent se terminer.

Pour une rotation : créer une nouvelle identité, transférer son jeton par un
canal privé, configurer le client, puis révoquer l’ancienne. Les reçus restent
attachés à l’ancienne identité et ne sont pas exposés à la nouvelle. Résoudre
les opérations incertaines avant rotation, ou les faire vérifier par l’opérateur.
Le MCP ne fournit aucun outil de création ou de révocation de jetons.

## Idempotence persistante

L’assistant attribue à **chaque mutation** une `operationKey` stable de 8–128
caractères (`A-Z`, `a-z`, chiffres, `_`, `-`) et conserve cette clé avant l’envoi.
La clé est commune à tous les outils pour une même identité, pas seulement à
un type de mutation. Ne pas y inscrire de nom ou de texte personnel.

L’empreinte de la requête porte sur le nom d’outil et les arguments validés,
normalisés et triés par clé JSON. Les mutations et leur résultat sont validés
dans **une même transaction PostgreSQL SERIALIZABLE**. La contrainte unique
`(credentialId, key)` empêche deux reçus concurrents. Une même clé et les mêmes
arguments retournent le résultat d’origine, sans réexécuter les services.
Changer les arguments ou l’outil sous la même clé produit `IDEMPOTENCY_CONFLICT`.

La même transaction enveloppe les services de catalogue, de semaine, de choix
manuel, de construction des instantanés et de courses existants. Le contexte
`withPrismaTransaction` réutilise la transaction externe pour les callbacks
transactionnels de ces services. Ce ne sont pas des transactions imbriquées
indépendantes. Les transactions par tableau sont refusées dans ce contexte ;
toute erreur doit remonter au propriétaire de la transaction externe.

Un savepoint permet d’annuler intégralement le travail métier et de conserver
un reçu **d’échec** dans la transaction externe. Les erreurs de sérialisation ou
de concurrence SQL annulent tout et sont réessayables. La validation de schéma,
l’authentification, le refus de droits ou le mode écriture désactivé interviennent
avant l’acceptation de l’opération et ne créent pas de reçu.

Les reçus n’ont pas d’expiration ni de purge automatique. Leur suppression
manuelle supprimerait la garantie pour les anciennes clés. Sauvegarder et
restaurer les tables MCP avec la base métier ; ne pas restaurer l’une sans
l’autre. Les résultats persistés contiennent des données de menu autorisées :
ce sont des données applicatives privées, pas des journaux techniques.

### Réponse perdue

1. Appeler `get_operation` avec la même clé et la même identité.
2. `completed` donne le résultat exact, y compris un échec métier enregistré.
3. `unknown` signifie **aucun reçu commité visible**, pas « rien n’est en cours ».
   Réessayer la requête originale avec la même clé ; ne pas en générer une nouvelle.
4. En cas de `CONCURRENT_CHANGE`, réessayer avec temporisation et la même clé.
5. Pour corriger un échec métier mémorisé, relire l’état et soumettre une nouvelle
   opération explicite avec une nouvelle clé.

Un aperçu n’est pas une réservation. L’application recalcule son empreinte dans
la transaction et contrôle la version, le statut, les deux verrous et les
remplacements. Les écritures concurrentes font échouer l’import sans laisser
une semaine partiellement modifiée. Aucun acquittement d’ambiguïté ou remplacement
ne permet de contourner un verrou ou une confirmation.

## Erreurs stables

| Code | Sens / conduite |
| --- | --- |
| `UNAUTHENTICATED` | Bearer absent, inconnu ou révoqué ; HTTP 401 à la frontière |
| `FORBIDDEN` | droit requis absent |
| `WRITE_DISABLED` | qualification en lecture/simulation |
| `MCP_NOT_CONFIGURED` | URL publique absente ou invalide ; HTTP 503 |
| `ORIGIN_REJECTED` | Host ou Origin différent de la configuration ; HTTP 403 |
| `METHOD_NOT_ALLOWED` | transport sans GET/DELETE ; HTTP 405 |
| `VALIDATION_ERROR` | champs invalides/manquants ; chemins et types d’erreurs, sans valeurs reçues |
| `MISSING_INFORMATION` | notamment semaine neuve sans 14 convives explicites |
| `CATALOG_MATCH` | nom normalisé existant ; réutiliser un candidat |
| `AMBIGUOUS_MATCH` | noms semblables à examiner ; aucune création implicite |
| `INVALID_REFERENCE` | ingrédient absent, archivé ou répété |
| `NOT_FOUND` | élément demandé absent |
| `INVALID_SELECTION` | plat/composition indisponible ou incomplet |
| `VERSION_CONFLICT` | version de semaine différente, ou existence différente de celle attendue |
| `WEEK_PROTECTED` | semaine confirmée ou archivée |
| `SLOT_LOCKED` | plat ou entrée du créneau ciblé verrouillé |
| `REPLACEMENT_REQUIRED` | contenu existant sans remplacement explicite |
| `PREVIEW_STALE` | l’état ou les instantanés ne correspondent plus à l’aperçu |
| `IDEMPOTENCY_CONFLICT` | clé déjà utilisée pour une autre requête |
| `CONCURRENT_CHANGE` | conflit SQL/sérialisation ; `retryable: true`, réessayer à clé identique |
| `BUSINESS_VALIDATION_FAILED` | garde-fou métier ou erreur de persistance non détaillée ; vérifier l’entrée, pas de message SQL brut |
| `INTERNAL_ERROR` | frontière HTTP indisponible ; aucune exception interne renvoyée |

Les erreurs de protocole, requêtes trop volumineuses et schémas rejetés par le
SDK utilisent ses réponses standard MCP/HTTP. Les clients doivent gérer ces
erreurs en plus de l’enveloppe métier.

## Données, journaux et textes non fiables

Aucun appel MCP n’exécute de shell, SQL construit à partir de texte reçu,
requête vers une URL reçue, instruction IA, suppression, gestion d’identité ou
commande de déploiement. Le SQL de verrouillage est paramétré ; les commandes
SAVEPOINT sont des constantes internes. Les noms ressemblant à des instructions
restent des libellés. Le serveur n’intègre pas d’interpréteur de photo.

Aucun corps d’appel, libellé, Bearer, erreur Prisma brute ou résultat de menu
n’est ajouté aux logs par le MCP. Les erreurs renvoyées n’exposent pas de stack,
connexion de base ou secret. Ne pas activer la journalisation Prisma des requêtes,
ni la capture des en-têtes/corps HTTP côté client, Cloudflare ou reverse proxy.
Les protections de journaux externes restent à contrôler lors du déploiement.
HTTPS et un stockage privé du jeton sont requis ; HTTP n’est autorisé par la
configuration que sur localhost pour les tests.
