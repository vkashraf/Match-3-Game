package com.example.game.badge

import com.example.core.GameDataProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BadgeState(
    val dailyRewardAvailable: Boolean = false,
    val missionClaimableCount: Int = 0,
    val achievementClaimableCount: Int = 0,
    val hasPendingRewards: Boolean = false
) {
    val totalBadges: Int
        get() = (if (dailyRewardAvailable) 1 else 0) + missionClaimableCount + achievementClaimableCount + (if (hasPendingRewards) 1 else 0)
}

object BadgeManager {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val _badgeState = MutableStateFlow(BadgeState())
    val badgeState: StateFlow<BadgeState> = _badgeState.asStateFlow()

    fun refreshBadges() {
        scope.launch {
            val dailyAvailable = try {
                GameDataProvider.dailyRewardRepository.isClaimAvailable()
            } catch (e: Exception) { false }

            val missionCount = try {
                val missions = GameDataProvider.missionRepository.ensureDailyMissionsLoaded()
                missions.count { it.isCompleted && !it.isClaimed }
            } catch (e: Exception) { 0 }

            val achievementCount = try {
                val achievements = GameDataProvider.achievementRepository.ensureAchievementsLoaded()
                achievements.count { it.isCompleted && !it.isRewardClaimed }
            } catch (e: Exception) { 0 }

            _badgeState.value = BadgeState(
                dailyRewardAvailable = dailyAvailable,
                missionClaimableCount = missionCount,
                achievementClaimableCount = achievementCount,
                hasPendingRewards = false
            )
        }
    }
}
