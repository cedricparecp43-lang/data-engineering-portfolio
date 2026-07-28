package training.model

case class StoreVisit(
  userId: String,
  storeId: String,
  visitTs: Long // epoch seconds
)
