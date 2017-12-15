====================================================================================
Systèmes d'exploitation pour l'embarqué
Université d'Avignon et des Pays de Vaucluse
2017-2018

RODRÍGUEZ, Ricardo
M2 ILSEN Alternants
====================================================================================
Rendu TP 2 - Météo
Application Android pour récupérer des données météorologiques
====================================================================================


1) Lancement de l'application
------------------------------------------------------------------------------------

Le répertoire contenant ce README correspond au projet créé avec AndroidStudio.
Dans mon ordinateur personnel je n'ai pas suffisamment de mémoire pour lancer l'IDE
et l'émulateur Android. Les derniers tests ont donc été réalisés par console.
Le fichier makefile contient plusieurs tâches pour lancer un émulateur, compiler et
installer l'application.

Je n'ai pas testé la dernière version de l'application dans un vrai smartphone
Android ni depuis AndroidStudio, mais le lancement via le makefile fonctionne
correctement.

Avant d'utiliser make il faut configurer dans le makefile les différents paths aux
outils du SDK Android ainsi que le nom de l'émulateur à lancer.


2) État d'avancement
------------------------------------------------------------------------------------

Le TP est complètement finalisé et fonctionnel.

2.a - BDD SQLite

    Une base de données SQLite est crée lors du premier lancement de l'application.
    La méthode onCreate de la classe DBHelper se charge de créer la table weather.
    Pour tester la re-création de la table, dans la méthode onOpen il y a un code
    qui supprime la table et fait un appel à onCreate. Ce code peut être activé via
    un flag booléen.

2.b - ContentProvider

    La classe WeatherContentProvider implémente une abstraction pour accéder et
    modifier les données dans la base. L'URI associé est
    "content://fr.uapv.rrodriguez.tp2_meteo2.provider/weather".
    
    Le provider gère deux types d'URI, la précédente qui permet l'accès à toute la
    table weather et une autre avec le pays et le nom de la ville ajoutés à la fin
    ("content://.../weather/pays/ville") pour accéder directement à la ville donnée.
    
    Les méthodes qui modifient les données (insert, update, delete) vont faire appel
    à la méthode notifyChange du ContentResolver associé au contexte actuel afin de
    notifier tous les observateurs de ce ContentProvider. Dans l'activité principale
    j'utilise un ContentObserver pour récupérer les changements après les
    modifications dans la base.

2.c - Loader
    
    Un loader est configuré via la classe LoaderManager pour accéder de manière
    asynchrone aux données en base. Après chaque modification de données
    l'application fait appel à la méthode restartLoader avec l'id identifiant le
    loader pour lancer une tâche en arrière plan qui va récupérer les données en
    base et actualiser les éléments visuels.
    
    L'observateur associé au provider va utiliser ce même loader à chaque fois qu'il
    détecte un changement dans la BDD.

2.d - SyncAdapter

    La classe YahooWeatherSyncAdapter implémente AbstractThreadedSyncAdapter pour
    réaliser des requêtes au webservice de Yahoo qui permet d'obtenir les
    informations météorologiques. Cette classe, configurée via des fichiers XML,
    utilise une classe qui contient la logique d'authentification. Lors de
    l'initilisation de l'application, ce SyncAdapter est configuré pour être lancé
    toutes les heures. En plus, un clic sur le bouton Recharger (en haut à droite)
    va lancer une nouvelle requête qui mettra à jour les informations en base.
    
    Le SyncAdapter est associé, via les fichiers de configuration, au provider de la
    table weather. De cette manière je peux accéder à la base pour mettre à jour les
    données des villes en arrière plan.

2.e - ContentObserver
    
    Après d'avoir implémenté le SyncAdapter, j'ai vu que les données en base étaient
    bien actualisées mais ce n'était pas le cas pour les informations affichées.
    J'ai dû ajouter une classe qui implémente un ContentObserver associé au
    ContentProvider, afin de rester à l'écoute de changements dans les données. Cet
    observer va exécuter le loader à chaque fois qu'il reçoit un appel à la méthode
    onChange.


====================================================================================
FIN DU DOCUMENT
====================================================================================