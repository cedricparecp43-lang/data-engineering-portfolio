# Data Engineering Portfolio

Projets d'entraînement et de démonstration en ingénierie de données, centrés sur Scala/Spark, l'orchestration Airflow, et les bonnes pratiques de pipeline (typage fort, gestion du skew, jointures optimisées, tests unitaires).

## Projets

### [spark-attribution-pipeline](./spark-attribution-pipeline)
Pipeline Spark/Scala d'attribution marketing multi-sources (clics publicitaires → visites en magasin → référentiel campagnes), orchestré avec Airflow. Démontre :
- Architecture modulaire (`model` / `transform` / `data` / `jobs`) séparant logique pure et orchestration Spark
- Gestion explicite du data skew et des stratégies de jointure (`BroadcastHashJoin` vs `SortMergeJoin`)
- Fenêtre d'attribution temporelle avec gestion des événements en retard/désynchronisés
- Lecture CSV avec schéma typé (`Dataset[T]`) plutôt que `DataFrame` non typé
- Orchestration Airflow avec DAGs dédiés par pipeline

---

*Plus de projets à venir.*
