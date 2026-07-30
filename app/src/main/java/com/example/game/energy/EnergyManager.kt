package com.example.game.energy

import com.example.data.local.entity.PlayerEntity
import com.example.data.repository.PlayerRepository
import com.example.game.shop.config.EconomyConfig
import java.util.concurrent.TimeUnit

object EnergyManager {

    /**
     * Checks real-world time elapsed since last energy update and applies
     * offline energy regeneration if currentEnergy < maxEnergy.
     */
    suspend fun checkAndApplyRegeneration(
        playerRepository: PlayerRepository,
        now: Long = System.currentTimeMillis()
    ): PlayerEntity {
        val player = playerRepository.getPlayer()
        if (player.energy >= player.maxEnergy) {
            return player
        }

        val elapsedMillis = now - player.lastPlayedAt
        if (elapsedMillis <= 0) return player

        val regenPeriodMillis = TimeUnit.MINUTES.toMillis(EconomyConfig.ENERGY_REGEN_MINUTES.toLong())
        val energyToAdd = (elapsedMillis / regenPeriodMillis).toInt()

        if (energyToAdd > 0) {
            val newEnergy = (player.energy + energyToAdd).coerceAtMost(player.maxEnergy)
            // Save remaining partial progress timestamp
            val remainderMillis = elapsedMillis % regenPeriodMillis
            val adjustedLastTime = now - remainderMillis

            playerRepository.addEnergy(newEnergy - player.energy)
            return playerRepository.getPlayer()
        }

        return player
    }

    /**
     * Calculates seconds remaining until the next +1 energy regeneration tick.
     */
    fun getRemainingSecondsForNextEnergy(
        player: PlayerEntity,
        now: Long = System.currentTimeMillis()
    ): Long {
        if (player.energy >= player.maxEnergy) return 0L

        val elapsedMillis = (now - player.lastPlayedAt).coerceAtLeast(0L)
        val regenPeriodMillis = TimeUnit.MINUTES.toMillis(EconomyConfig.ENERGY_REGEN_MINUTES.toLong())
        val remainderMillis = elapsedMillis % regenPeriodMillis
        val millisLeft = regenPeriodMillis - remainderMillis

        return (millisLeft / 1000L).coerceAtLeast(0L)
    }

    /**
     * Formats seconds left into MM:SS format.
     */
    fun formatRemainingTime(seconds: Long): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", mins, secs)
    }

    /**
     * Consumes level energy cost atomically if player has enough energy.
     */
    suspend fun consumeLevelEnergy(playerRepository: PlayerRepository): Boolean {
        return playerRepository.spendEnergy(EconomyConfig.LEVEL_ENERGY_COST)
    }

    /**
     * Refills 1 point of energy with gems.
     */
    suspend fun refillSingleEnergyWithGems(playerRepository: PlayerRepository): Boolean {
        val p = playerRepository.getPlayer()
        if (p.energy >= p.maxEnergy) return false
        if (!playerRepository.spendGems(EconomyConfig.ENERGY_1_REFILL_GEMS.toInt())) return false
        return playerRepository.addEnergy(1)
    }

    /**
     * Full energy refill to max capacity.
     */
    suspend fun fullRefillEnergyWithGems(playerRepository: PlayerRepository): Boolean {
        val p = playerRepository.getPlayer()
        if (p.energy >= p.maxEnergy) return false
        val cost = EconomyConfig.FULL_ENERGY_REFILL_GEMS.toInt()
        if (!playerRepository.spendGems(cost)) return false
        val needed = p.maxEnergy - p.energy
        return playerRepository.addEnergy(needed)
    }
}
