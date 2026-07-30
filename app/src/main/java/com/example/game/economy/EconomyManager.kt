package com.example.game.economy

import com.example.core.GameDataProvider
import com.example.core.event.GameEvent
import com.example.core.event.GameEventBus
import com.example.core.event.GameEventType
import com.example.game.resource.ResourceManager
import com.example.game.resource.ResourceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

object EconomyManager {

    suspend fun getCoins(): Long = withContext(Dispatchers.IO) {
        GameDataProvider.playerRepository.getPlayer().coins
    }

    suspend fun addCoins(amount: Long, source: TransactionSource = TransactionSource.SYSTEM, referenceId: String? = null): Boolean = withContext(Dispatchers.IO) {
        if (amount <= 0) return@withContext false
        val success = GameDataProvider.playerRepository.addCoins(amount)
        if (success) {
            GameEventBus.emit(GameEvent(GameEventType.COINS_CHANGED, amount = amount.toInt()))
            GameEventBus.emit(GameEvent(GameEventType.COINS_EARNED, amount = amount.toInt()))
        }
        success
    }

    suspend fun removeCoins(amount: Long, source: TransactionSource = TransactionSource.SYSTEM, referenceId: String? = null): Boolean = withContext(Dispatchers.IO) {
        if (amount <= 0) return@withContext false
        val currentCoins = getCoins()
        if (currentCoins < amount) return@withContext false
        val success = GameDataProvider.playerRepository.spendCoins(amount)
        if (success) {
            GameEventBus.emit(GameEvent(GameEventType.COINS_CHANGED, amount = -amount.toInt()))
        }
        success
    }

    suspend fun getGems(): Int = withContext(Dispatchers.IO) {
        GameDataProvider.playerRepository.getPlayer().gems
    }

    suspend fun addGems(amount: Int, source: TransactionSource = TransactionSource.SYSTEM, referenceId: String? = null): Boolean = withContext(Dispatchers.IO) {
        if (amount <= 0) return@withContext false
        val success = GameDataProvider.playerRepository.addGems(amount)
        if (success) {
            GameEventBus.emit(GameEvent(GameEventType.GEMS_CHANGED, amount = amount))
            GameEventBus.emit(GameEvent(GameEventType.GEMS_EARNED, amount = amount))
        }
        success
    }

    suspend fun removeGems(amount: Int, source: TransactionSource = TransactionSource.SYSTEM, referenceId: String? = null): Boolean = withContext(Dispatchers.IO) {
        if (amount <= 0) return@withContext false
        val currentGems = getGems()
        if (currentGems < amount) return@withContext false
        val success = GameDataProvider.playerRepository.spendGems(amount)
        if (success) {
            GameEventBus.emit(GameEvent(GameEventType.GEMS_CHANGED, amount = -amount))
        }
        success
    }

    suspend fun getEnergy(): Int = withContext(Dispatchers.IO) {
        GameDataProvider.playerRepository.getPlayer().energy
    }

    suspend fun addEnergy(amount: Int, source: TransactionSource = TransactionSource.SYSTEM, referenceId: String? = null): Boolean = withContext(Dispatchers.IO) {
        if (amount <= 0) return@withContext false
        val success = GameDataProvider.playerRepository.addEnergy(amount)
        if (success) {
            GameEventBus.emit(GameEvent(GameEventType.ENERGY_CHANGED, amount = amount))
        }
        success
    }

    suspend fun consumeEnergy(amount: Int, source: TransactionSource = TransactionSource.SYSTEM, referenceId: String? = null): Boolean = withContext(Dispatchers.IO) {
        if (amount <= 0) return@withContext false
        val currentEnergy = getEnergy()
        if (currentEnergy < amount) return@withContext false
        val success = GameDataProvider.playerRepository.spendEnergy(amount)
        if (success) {
            GameEventBus.emit(GameEvent(GameEventType.ENERGY_USED, amount = amount))
            GameEventBus.emit(GameEvent(GameEventType.ENERGY_CHANGED, amount = -amount))
        }
        success
    }

    /**
     * Executes atomic multi-resource purchase validation and deduction.
     * Validate -> Calculate -> Apply -> Persist -> Notify.
     */
    suspend fun executeAtomicPurchase(
        coinCost: Long = 0,
        gemCost: Int = 0,
        resourceCosts: Map<ResourceType, Int> = emptyMap(),
        onSuccess: suspend () -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        val player = GameDataProvider.playerRepository.getPlayer()

        // 1. Validate
        if (coinCost > 0 && player.coins < coinCost) return@withContext false
        if (gemCost > 0 && player.gems < gemCost) return@withContext false
        for ((resType, cost) in resourceCosts) {
            val owned = ResourceManager.getResourceAmount(resType)
            if (owned < cost) return@withContext false
        }

        // 2. Apply / Deduct
        if (coinCost > 0) {
            if (!GameDataProvider.playerRepository.spendCoins(coinCost)) return@withContext false
        }
        if (gemCost > 0) {
            if (!GameDataProvider.playerRepository.spendGems(gemCost)) {
                // Rollback coins if gem spend failed
                if (coinCost > 0) GameDataProvider.playerRepository.addCoins(coinCost)
                return@withContext false
            }
        }
        for ((resType, cost) in resourceCosts) {
            if (cost > 0) {
                ResourceManager.spendResource(resType, cost)
            }
        }

        // 3. Callback & Events
        onSuccess()

        if (coinCost > 0) GameEventBus.emit(GameEvent(GameEventType.COINS_CHANGED, amount = -coinCost.toInt()))
        if (gemCost > 0) GameEventBus.emit(GameEvent(GameEventType.GEMS_CHANGED, amount = -gemCost))

        true
    }
}
