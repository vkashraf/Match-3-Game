package com.example.game.island.zone

import com.example.core.GameDataProvider
import com.example.data.local.entity.IslandZoneEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object ZoneManager {

    private val scope = CoroutineScope(Dispatchers.IO)
    private var isBusy = false

    fun unlockZone(
        zone: IslandZoneEntity,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (isBusy) return
        isBusy = true

        scope.launch {
            try {
                if (zone.isUnlocked) {
                    withContext(Dispatchers.Main) {
                        isBusy = false
                        onSuccess()
                    }
                    return@launch
                }

                val player = GameDataProvider.playerRepository.getPlayer()
                if (player == null) {
                    withContext(Dispatchers.Main) {
                        isBusy = false
                        onError("Player error")
                    }
                    return@launch
                }

                if (player.playerLevel < zone.requiredPlayerLevel) {
                    withContext(Dispatchers.Main) {
                        isBusy = false
                        onError("Requires Player Level ${zone.requiredPlayerLevel}.")
                    }
                    return@launch
                }

                if (player.totalStars < zone.requiredStars) {
                    withContext(Dispatchers.Main) {
                        isBusy = false
                        onError("Requires ${zone.requiredStars} Stars.")
                    }
                    return@launch
                }

                if (player.coins < zone.unlockCostCoins) {
                    withContext(Dispatchers.Main) {
                        isBusy = false
                        onError("Not enough coins! Need ${zone.unlockCostCoins} coins.")
                    }
                    return@launch
                }

                if (zone.unlockCostCoins > 0) {
                    GameDataProvider.spendCoins(zone.unlockCostCoins)
                }

                // Update Zone
                val updatedZone = zone.copy(isUnlocked = true)
                GameDataProvider.islandRepository.updateZone(updatedZone)

                com.example.core.event.GameEventBus.postEvent(
                    com.example.core.event.GameEvent(
                        type = com.example.core.event.GameEventType.LAND_UNLOCKED,
                        levelId = zone.zoneId
                    )
                )

                // Also unlock all plots in this zone automatically
                val allPlots = GameDataProvider.islandRepository.allPlotsFlow.first()
                allPlots.filter { it.zoneId == zone.zoneId }.forEach { plot ->
                    if (!plot.isUnlocked) {
                        GameDataProvider.islandRepository.updatePlot(plot.copy(isUnlocked = true))
                    }
                }

                withContext(Dispatchers.Main) {
                    isBusy = false
                    onSuccess()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isBusy = false
                    onError(e.localizedMessage ?: "Zone unlock failed")
                }
            }
        }
    }
}
