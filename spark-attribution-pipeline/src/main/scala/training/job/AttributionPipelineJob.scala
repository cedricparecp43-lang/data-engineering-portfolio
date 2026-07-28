package training.jobs

import org.apache.spark.sql.{SparkSession, Dataset}
import org.apache.spark.sql.functions.{broadcast, desc, count, countDistinct, round, avg}
import training.model.{ClickEvent, StoreVisit, CampaignRef, AttributionCandidate, AttributedVisit}
import training.data.SyntheticDataGenerator
import training.helper.AttributionTransformer

object AttributionPipelineJob {


  def run(spark: SparkSession): Dataset[AttributedVisit] = {
    import spark.implicits._
    val clicksDS: Dataset[ClickEvent] = SyntheticDataGenerator.generateClicks(spark, numRows = 500000)
    val visitsDS: Dataset[StoreVisit] = SyntheticDataGenerator.generateStoreVisits(spark, numRows = 200000)
    val campaignsDS: Dataset[CampaignRef] = SyntheticDataGenerator.generateCampaignsRef(spark)

    // Jointure 1 — clicks <-> visits sur userId : deux gros volumes,
    // aucune table n'est petite -> SortMergeJoin attendu, shuffle des deux côtés.
    val candidates: Dataset[AttributionCandidate] =
      clicksDS.joinWith(visitsDS, clicksDS("userId") === visitsDS("userId"))
        .map { case (c, v) => AttributionTransformer.buildCandidate(c, v) }

    val validCandidates: Dataset[AttributionCandidate] =
      candidates.filter(AttributionTransformer.isValid _)

    // Jointure 2 — candidats valides <-> campaigns (broadcast explicite) :
    // campaigns tient largement en mémoire -> BroadcastHashJoin, pas de shuffle
    // sur cette jointure.
    validCandidates.joinWith(broadcast(campaignsDS), validCandidates("campaignId") === campaignsDS("campaignId"))
      .map { case (candidate, ref) => AttributionTransformer.enrich(candidate, ref) }
  }

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("AttributionPipeline")
      .master("local[*]")
      .config("spark.sql.shuffle.partitions", "8")
      .getOrCreate()

    import spark.implicits._
    spark.sparkContext.setLogLevel("WARN")

    val result: Dataset[AttributedVisit] = run(spark)
    result.cache()

    println(s"=== Visites attribuées (dans la fenêtre de 72h) : ${result.count()} ===")
    result.show(10, truncate = false)

    println("=== Plan physique de la jointure clicks <-> visits (chercher SortMergeJoin) ===")
    result.explain()

    // Agrégation : le skew injecté sur campaignId (85% sur camp_A) devient
    // ici un skew sur chainName -> observable dans la Spark UI sur cette étape.
    val statsByChain = result.groupBy("chainName")
      .agg(
        count("*").as("total_attributed_visits"),
        countDistinct("userId").as("unique_users"),
        round(avg("delayHours"), 2).as("avg_delay_hours")
      )
      .orderBy(desc("total_attributed_visits"))

    println("=== Statistiques d'attribution par enseigne ===")
    statsByChain.show(truncate = false)

    Thread.sleep(20000) // pause pour consulter la Spark UI avant fermeture
    spark.stop()
  }
}
