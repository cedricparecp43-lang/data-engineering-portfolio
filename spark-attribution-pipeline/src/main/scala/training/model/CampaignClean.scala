package training.model

import java.sql.Date


case class CampaignClean(
                          campaignId: Int,
                          company: String,
                          channelUsed: String,
                          customerSegment: String,
                          conversionRate: Double,
                          acquisitionCostUsd: Double,
                          durationDays: Int,
                          roi: Double,
                          clicks: Int,
                          impressions: Int,
                          engagementScore: Int,
                          date: Date
                        )