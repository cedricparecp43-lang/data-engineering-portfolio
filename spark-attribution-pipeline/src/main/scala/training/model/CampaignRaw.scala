package training.model


case class CampaignRaw(
                        campaignId: Int,
                        company: String,
                        campaignType: String,
                        targetAudience: String,
                        duration: String,
                        channelUsed: String,
                        conversionRate: Double,
                        acquisitionCost: String,
                        roi: Double,
                        location: String,
                        language: String,
                        clicks: Int,
                        impressions: Int,
                        engagementScore: Int,
                        customerSegment: String,
                        date: String
                      )