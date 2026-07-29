package com.example.game.island.plot

import com.example.core.GameDataProvider
import com.example.data.local.entity.BuildingPlotEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object PlotManager {

    private val scope = CoroutineScope(Dispatchers.IO)
    private var isBusy = false

    fun unlockPlot(
        plot: BuildingPlotEntity,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (isBusy) return
        isBusy = true

        scope.launch {
            try {
                if (plot.isUnlocked) {
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

                if (player.playerLevel < plot.requiredPlayerLevel) {
                    withContext(Dispatchers.Main) {
                        isBusy = false
                        onError("Requires Player Level ${plot.requiredPlayerLevel}.")
                    }
                    return@launch
                }

                if (player.totalStars < plot.requiredStars) {
                    withContext(Dispatchers.Main) {
                        isBusy = false
                        onError("Requires ${plot.requiredStars} Stars.")
                    }
                    return@launch
                }

                if (player.coins < plot.unlockCostCoins) {
                    withContext(Dispatchers.Main) {
                        isBusy = false
                        onError("Not enough coins! Need ${plot.unlockCostCoins} coins.")
                    }
                    return@launch
                }

                // Deduct unlock cost
                if (plot.unlockCostCoins > 0) {
                    GameDataProvider.spendCoins(plot.unlockCostCoins)
                }

                val updatedPlot = plot.copy(isUnlocked = true)
                GameDataProvider.islandRepository.updatePlot(updatedPlot)

                withContext(Dispatchers.Main) {
                    isBusy = false
                    onSuccess()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isBusy = false
                    onError(e.localizedMessage ?: "Unlock failed")
                }
            }
        }
    }
}
