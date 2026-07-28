package training.model

case class ClickEvent(
  userId: String,
  campaignId: String,
  clickTs: Long // epoch seconds
)
