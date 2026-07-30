package com.example.data.repository

import com.example.data.local.dao.PendingRewardDao
import com.example.data.local.entity.PendingRewardEntity
import com.example.game.reward.Reward
import com.example.game.reward.RewardManager
import com.example.game.reward.RewardType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID

class PendingRewardRepository(private val pendingRewardDao: PendingRewardDao) {

    val pendingRewardsFlow: Flow<List<PendingRewardEntity>> = pendingRewardDao.observePendingRewards()

    suspend fun addPendingReward(
        sourceType: String,
        rewardType: RewardType,
        amount: Int,
        itemId: String? = null,
        referenceId: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        if (referenceId != null) {
            val existing = pendingRewardDao.getByReferenceId(referenceId)
            if (existing != null) return@withContext false // duplicate protection
        }

        val entity = PendingRewardEntity(
            pendingRewardId = UUID.randomUUID().toString(),
            sourceType = sourceType,
            rewardType = rewardType.name,
            itemId = itemId,
            amount = amount,
            referenceId = referenceId,
            createdAt = System.currentTimeMillis(),
            isClaimed = false
        )
        pendingRewardDao.insertPendingReward(entity)
        true
    }

    suspend fun claimPendingReward(rewardId: String): Boolean = withContext(Dispatchers.IO) {
        val list = pendingRewardDao.getPendingRewards()
        val rewardEntity = list.firstOrNull { it.pendingRewardId == rewardId } ?: return@withContext false

        val rewardType = try {
            RewardType.valueOf(rewardEntity.rewardType)
        } catch (e: Exception) {
            RewardType.COINS
        }

        val reward = Reward(rewardType, rewardEntity.itemId, rewardEntity.amount)
        val granted = RewardManager.grantReward(reward)
        if (granted) {
            pendingRewardDao.markClaimed(rewardId)
            true
        } else {
            false
        }
    }

    suspend fun claimAllPendingRewards(): Int = withContext(Dispatchers.IO) {
        val list = pendingRewardDao.getPendingRewards()
        var claimedCount = 0
        for (item in list) {
            if (claimPendingReward(item.pendingRewardId)) {
                claimedCount++
            }
        }
        pendingRewardDao.clearClaimed()
        claimedCount
    }
}
