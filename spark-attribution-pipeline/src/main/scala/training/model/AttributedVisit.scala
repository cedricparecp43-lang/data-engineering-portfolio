package training.model

case class AttributedVisit(
  userId: String,
  campaignId: String,
  campaignName: String,
  chainName: String,
  storeId: String,
  clickTs: Long,
  visitTs: Long,
  delayHours: Double
)
