name := "spark-training"
version := "0.1"
scalaVersion := "2.13.16"

val sparkVersion = "4.2.0"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-sql"  % sparkVersion % "provided"
)

// "provided" car spark-submit fournit déjà ces dépendances à l'exécution
// (elles sont dans SPARK_HOME/jars) — un simple `sbt package` (jar fin) suffit,
// pas besoin de plugin d'assembly puisqu'on n'a aucune dépendance tierce à embarquer.