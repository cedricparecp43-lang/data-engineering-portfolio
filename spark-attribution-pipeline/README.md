# Spark Attribution Pipeline

Pipeline Scala/Spark d'attribution marketing multi-sources, orchestré avec Apache Airflow. Projet d'entraînement construit pour pratiquer des scénarios réalistes d'un contexte AdTech/retail (mesure de performance publicitaire, attribution clic → visite en magasin).

## Ce que ce projet démontre

- **Architecture modulaire** : séparation stricte entre modèles de données (`model/`), génération de données (`data/`), logique métier pure et testable (`transform/`), et orchestration Spark (`jobs/`)
- **Gestion du data skew** : distribution volontairement déséquilibrée injectée sur une clé métier (`campaignId`), pour observer et gérer les partitions surchargées
- **Stratégies de jointure maîtrisées** : jointure `SortMergeJoin` entre deux grands volumes (clics/visites) et `BroadcastHashJoin` explicite pour la table de référence (campagnes)
- **Fenêtre d'attribution temporelle** : gestion des événements en retard et de la désynchronisation d'horloge entre systèmes sources, via une fenêtre de validité explicite (0 à 72h)
- **API typée `Dataset[T]`** plutôt que `DataFrame` non typé, avec vérification à la compilation
- **Orchestration Airflow** : DAGs séparés par pipeline, exécution via `spark-submit`

## Structure

```
spark-attribution-pipeline/
├── build.sbt
├── project/
├── src/main/scala/training/
│   ├── model/        # Case classes (schéma typé des données)
│   ├── data/          # Génération de données synthétiques réalistes
│   ├── transform/      # Logique métier pure (testable sans Spark)
│   └── jobs/          # Orchestration Spark (lecture, jointures, écriture)
├── data/raw/           # Données brutes (CSV, non versionné)
├── spark_training_dag.py        # DAG Airflow : pipeline marketing campaign (CSV)
├── attribution_pipeline_dag.py  # DAG Airflow : pipeline attribution multi-sources
└── setup_airflow.sh    # Installation Airflow locale
```

## Prérequis

```bash
brew install openjdk@11 scala sbt apache-spark
```

## Exécution locale

```bash
sbt clean package
spark-submit \
  --class training.jobs.AttributionPipelineJob \
  --master "local[*]" \
  target/out/jvm/scala-2.13.16/spark-attribution-pipeline/spark-attribution-pipeline_2.13-0.1.jar
```

Pendant l'exécution, la Spark UI est disponible sur `http://localhost:4040` (plans d'exécution, stages, détection du skew).

## Orchestration Airflow

```bash
./setup_airflow.sh
# puis, dans deux terminaux séparés :
airflow webserver --port 8080
airflow scheduler
```
