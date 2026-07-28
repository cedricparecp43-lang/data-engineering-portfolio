package training.jobs

import org.apache.spark.sql.{SparkSession, Dataset, Encoders}
import training.model.{CampaignRaw, CampaignClean}
import training.helper.CampaignTransformer

object MarketingPipelineJob {


  def run(spark: SparkSession, inputPath: String): Dataset[CampaignClean] = {
    import spark.implicits._

    val rawSchema = Encoders.product[CampaignRaw].schema

    spark.read
      .option("header", "true")
      .schema(rawSchema)
      .csv(inputPath)
      .as[CampaignRaw]
      .map(CampaignTransformer.clean)
      .filter(CampaignTransformer.isValid(_))
  }

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("MarketingCampaignPipeline")
      .master("local[*]")
      .config("spark.sql.shuffle.partitions", "8")
      .getOrCreate()

    spark.sparkContext.setLogLevel("WARN")

    val inputPath = if (args.nonEmpty) args(0) else "data/raw/marketing_campaign_performance.csv"

    val result = run(spark, inputPath)

    result.show(10, truncate = false)
    println(s"Nombre de lignes valides : ${result.count()}")

    Thread.sleep(20000)
    spark.stop()
  }
}