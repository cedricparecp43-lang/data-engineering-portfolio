package training.data

import org.apache.spark.sql.{Dataset, SparkSession}
import training.model.{CampaignRef, ClickEvent, StoreVisit}

/**
 * Génère des jeux de données synthétiques mais réalistes pour simuler le
 * contexte oKube : skew volontaire sur campaignId (une grosse enseigne
 * nationale domine le volume), et des visites en magasin avec des délais
 * variés (normaux, en retard au-delà de la fenêtre d'attribution, ou
 * négatifs pour simuler une désynchronisation d'horloge entre systèmes).
 *
 * Ce module dépend de SparkSession (contrairement à CampaignTransformer,
 * qui reste une logique pure) car générer des Dataset nécessite le moteur
 * Spark — mais il reste séparé du job pour ne pas polluer l'orchestration.
 */
object SyntheticDataGenerator {

  private val baseTs = 1700000000L // point de référence temporel arbitraire

  def generateClicks(spark: SparkSession, numRows: Int = 2000000): Dataset[ClickEvent] = {
    import spark.implicits._

    spark.range(numRows).map { i =>
      // skew volontaire : camp_A représente ~85% du volume, comme une
      // grosse enseigne nationale face à des campagnes locales
      val campaignId = if (i % 100 < 85) "camp_A" else s"camp_${(i % 9) + 1}"
      val userId = s"user_${i % 100000}"
      val clickTs = baseTs + (i % 86400)
      ClickEvent(userId, campaignId, clickTs)
    }
  }

  def generateStoreVisits(spark: SparkSession, numRows: Int = 800000): Dataset[StoreVisit] = {
    import spark.implicits._

    spark.range(numRows).map { i =>
      val userId = s"user_${i % 100000}"
      val storeId = s"store_${i % 500}"

      // Distribution des délais visite/clic, volontairement hétérogène :
      //  - 80% des visites : délai normal, quelques heures après le clic
      //  - 15% des visites : en retard, au-delà de la fenêtre d'attribution (>72h)
      //  -  5% des visites : délai négatif, simule une désynchronisation
      //     d'horloge entre le serveur de clics et le capteur IoT en magasin
      val delaySeconds: Long = i % 100 match {
        case n if n < 80 => (i % 259200)          // 0 à 72h en secondes
        case n if n < 95 => 259200L + (i % 604800) // 72h à ~7 jours (en retard)
        case _           => -((i % 7200) + 1L)     // délai négatif (désync horloge)
      }

      val visitTs = baseTs + (i % 86400) + delaySeconds
      StoreVisit(userId, storeId, visitTs)
    }
  }

  def generateCampaignsRef(spark: SparkSession): Dataset[CampaignRef] = {
    import spark.implicits._

    val minorCampaigns = (1 to 9).map(i => CampaignRef(s"camp_$i", s"Campagne locale $i", s"Enseigne indépendante $i"))
    val majorCampaign = CampaignRef("camp_A", "Grande campagne nationale", "Enseigne nationale")

    spark.createDataset(minorCampaigns :+ majorCampaign)
  }
}
