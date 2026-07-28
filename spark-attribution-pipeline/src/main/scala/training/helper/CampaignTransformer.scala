package training.helper

import training.model.{CampaignRaw, CampaignClean}

/**
 * Logique pure de transformation et de validation, isolée du job.
 * Pas de SparkSession ici, pas d'I/O — juste des fonctions Scala pures,
 * donc facilement testables unitairement (ex: avec un simple test ScalaTest
 * sur un CampaignRaw construit à la main, sans avoir besoin de démarrer Spark).
 */
object CampaignTransformer {

  def clean(raw: CampaignRaw): CampaignClean = {
    val cost = raw.acquisitionCost.replaceAll("[$,]", "").toDouble
    val days = raw.duration.split(" ").head.toInt
    val parsedDate = java.sql.Date.valueOf(raw.date)

    CampaignClean(
      campaignId = raw.campaignId,
      company = raw.company,
      channelUsed = raw.channelUsed,
      customerSegment = raw.customerSegment,
      conversionRate = raw.conversionRate,
      acquisitionCostUsd = cost,
      durationDays = days,
      roi = raw.roi,
      clicks = raw.clicks,
      impressions = raw.impressions,
      engagementScore = raw.engagementScore,
      date = parsedDate
    )
  }

  def isValid(c: CampaignClean): Boolean =
    c.conversionRate >= 0 && c.conversionRate <= 1 &&
      c.acquisitionCostUsd > 0 &&
      c.clicks >= 0 &&
      c.impressions >= 0
}