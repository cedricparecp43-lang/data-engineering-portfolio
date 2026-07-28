package training.helper

import training.model.{ClickEvent, StoreVisit, CampaignRef, AttributionCandidate, AttributedVisit}

/**
 * Logique pure d'attribution : aucune dependance a SparkSession, uniquement
 * des fonctions Scala sur des case class. Testable unitairement sans Spark.
 */
object AttributionTransformer {

  /** Delai en heures entre le clic et la visite. Peut etre negatif (desync horloge). */
  def delayHours(clickTs: Long, visitTs: Long): Double =
    (visitTs - clickTs) / 3600.0

  /**
   * Une visite n'est attribuee a un clic que si le delai est dans une fenetre
   * raisonnable : positif (la visite suit le clic, pas l'inverse) et borne a
   * maxHours (au-dela, l'attribution est trop incertaine pour etre fiable
   * metier, meme si l'evenement existe techniquement).
   */
  def isWithinAttributionWindow(delay: Double, maxHours: Double = 72.0): Boolean =
    delay >= 0 && delay <= maxHours

  /** Construit un candidat d'attribution a partir d'une paire clic/visite jointe. */
  def buildCandidate(click: ClickEvent, visit: StoreVisit): AttributionCandidate = {
    val delay = delayHours(click.clickTs, visit.visitTs)
    AttributionCandidate(
      userId = click.userId,
      campaignId = click.campaignId,
      storeId = visit.storeId,
      clickTs = click.clickTs,
      visitTs = visit.visitTs,
      delayHours = delay
    )
  }

  /** Valide un candidat deja construit (utilise apres buildCandidate). */
  def isValid(candidate: AttributionCandidate): Boolean =
    isWithinAttributionWindow(candidate.delayHours)

  /** Enrichit un candidat valide avec les informations de la campagne (post broadcast join). */
  def enrich(candidate: AttributionCandidate, ref: CampaignRef): AttributedVisit =
    AttributedVisit(
      userId = candidate.userId,
      campaignId = candidate.campaignId,
      campaignName = ref.campaignName,
      chainName = ref.chainName,
      storeId = candidate.storeId,
      clickTs = candidate.clickTs,
      visitTs = candidate.visitTs,
      delayHours = candidate.delayHours
    )
}
