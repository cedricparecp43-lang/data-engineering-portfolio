package training.model

case class AttributionCandidate(
  userId: String,
  campaignId: String,
  storeId: String,
  clickTs: Long,
  visitTs: Long,
  delayHours: Double
)
