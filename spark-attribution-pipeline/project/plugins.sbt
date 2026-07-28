// Plugin d'assembly retiré : incompatible avec sbt 2.x installé par Homebrew.
// Pas nécessaire ici puisque les dépendances Spark sont "provided" (fournies
// par spark-submit) et qu'on n'a aucune dépendance tierce à embarquer —
// un simple `sbt package` (jar fin) suffit.