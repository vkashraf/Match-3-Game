package com.example.data.repository

import com.example.core.GameTimeProvider
import com.example.core.event.GameEvent
import com.example.data.local.dao.MissionDao
import com.example.data.local.entity.MissionProgressEntity
import com.example.game.reward.Reward
import com.example.game.reward.RewardManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class MissionRepository(private val missionDao: MissionDao) {

    val allMissionsFlow: Flow<List<MissionProgressEntity>> = missionDao.observeAllMissions()

    suspend fun ensureMissionsLoaded(): List<MissionProgressEntity> = withContext(Dispatchers.IO) {
        val existing = missionDao.getAllMissions()
        val now = GameTimeProvider.getCurrentTimeMillis()

        if (existing.isNotEmpty()) {
            val dailySample = existing.firstOrNull { it.isDaily }
            if (dailySample != null && GameTimeProvider.isSameDay(dailySample.updatedAt, now)) {
                return@withContext existing
            }
        }

        val existingMap = existing.associateBy { it.missionId }
        val newEntities = MissionData.ALL_MISSIONS.map { config ->
            val prev = existingMap[config.missionId]
            if (config.isDaily) {
                // Reset daily if day changed
                MissionProgressEntity(
                    missionId = config.missionId,
                    currentProgress = 0,
                    target = config.target,
                    isCompleted = false,
                    isClaimed = false,
                    isDaily = true,
                    updatedAt = now
                )
            } else {
                // Keep weekly progress if not reset
                prev ?: MissionProgressEntity(
                    missionId = config.missionId,
                    currentProgress = 0,
                    target = config.target,
                    isCompleted = false,
                    isClaimed = false,
                    isDaily = false,
                    updatedAt = now
                )
            }
        }
        missionDao.insertOrUpdateMissions(newEntities)
        newEntities
    }

    suspend fun ensureDailyMissionsLoaded(): List<MissionProgressEntity> = ensureMissionsLoaded()

    suspend fun onGameEvent(event: GameEvent) = withContext(Dispatchers.IO) {
        val missions = ensureMissionsLoaded()
        val now = GameTimeProvider.getCurrentTimeMillis()

        for (m in missions) {
            if (m.isClaimed || m.isCompleted) continue
            val config = MissionData.getConfig(m.missionId) ?: continue
            if (config.type == event.type) {
                val newProgress = (m.currentProgress + event.amount).coerceAtMost(m.target)
                val isDone = newProgress >= m.target
                val updated = m.copy(
                    currentProgress = newProgress,
                    isCompleted = isDone,
                    updatedAt = now
                )
                missionDao.insertOrUpdateMission(updated)
            }
        }
    }

    suspend fun claimMission(missionId: String): Pair<Boolean, List<Reward>> = withContext(Dispatchers.IO) {
        val mission = missionDao.getMission(missionId) ?: return@withContext Pair(false, emptyList())
        if (!mission.isCompleted || mission.isClaimed) {
            return@withContext Pair(false, emptyList())
        }

        val config = MissionData.getConfig(missionId) ?: return@withContext Pair(false, emptyList())
        val granted = RewardManager.grantRewards(config.rewards, referenceId = "MISSION_$missionId")
        if (granted) {
            val updated = mission.copy(isClaimed = true, updatedAt = GameTimeProvider.getCurrentTimeMillis())
            missionDao.insertOrUpdateMission(updated)
            Pair(true, config.rewards)
        } else {
            Pair(false, emptyList())
        }
    }

    suspend fun hasUnclaimedCompletedMissions(): Boolean = withContext(Dispatchers.IO) {
        val list = ensureMissionsLoaded()
        list.any { it.isCompleted && !it.isClaimed }
    }
}
