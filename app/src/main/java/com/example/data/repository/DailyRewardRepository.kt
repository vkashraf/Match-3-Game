package com.example.data.repository

import com.example.core.GameTimeProvider
import com.example.data.local.dao.DailyRewardDao
import com.example.data.local.entity.DailyRewardStateEntity
import com.example.game.reward.Reward
import com.example.game.reward.RewardManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class DailyRewardRepository(private val dailyRewardDao: DailyRewardDao) {

    val stateFlow: Flow<DailyRewardStateEntity?> = dailyRewardDao.observeDailyRewardState()

    suspend fun getOrCreateState(): DailyRewardStateEntity = withContext(Dispatchers.IO) {
        val existing = dailyRewardDao.getDailyRewardState()
        if (existing != null) {
            existing
        } else {
            val defaultState = DailyRewardStateEntity(
                id = 1,
                currentDay = 1,
                lastClaimTimestamp = 0L,
                streakCount = 0,
                cycleStartTimestamp = GameTimeProvider.getCurrentTimeMillis()
            )
            dailyRewardDao.insertOrUpdateState(defaultState)
            defaultState
        }
    }

    suspend fun isClaimAvailable(): Boolean = withContext(Dispatchers.IO) {
        val state = getOrCreateState()
        if (state.lastClaimTimestamp <= 0L) return@withContext true
        val now = GameTimeProvider.getCurrentTimeMillis()
        !GameTimeProvider.isSameDay(state.lastClaimTimestamp, now)
    }

    suspend fun claimReward(): Pair<Boolean, List<Reward>> = withContext(Dispatchers.IO) {
        val state = getOrCreateState()
        val now = GameTimeProvider.getCurrentTimeMillis()

        if (state.lastClaimTimestamp > 0L && GameTimeProvider.isSameDay(state.lastClaimTimestamp, now)) {
            // Already claimed today
            return@withContext Pair(false, emptyList())
        }

        val daysDiff = if (state.lastClaimTimestamp > 0L) {
            GameTimeProvider.daysBetween(state.lastClaimTimestamp, now)
        } else {
            1
        }

        val newStreak = if (daysDiff <= 1) {
            state.streakCount + 1
        } else {
            1 // streak reset due to missed day
        }

        val dayInCycle = ((newStreak - 1) % 7) + 1
        val config = DailyRewardData.getForDay(dayInCycle)

        // Grant rewards atomically
        val granted = RewardManager.grantRewards(config.rewards)
        if (granted) {
            val newState = state.copy(
                currentDay = dayInCycle,
                lastClaimTimestamp = now,
                streakCount = newStreak
            )
            dailyRewardDao.insertOrUpdateState(newState)
            Pair(true, config.rewards)
        } else {
            Pair(false, emptyList())
        }
    }
}
