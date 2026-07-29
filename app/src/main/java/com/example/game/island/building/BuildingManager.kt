package com.example.game.island.building

import com.example.core.GameDataProvider
import com.example.data.local.entity.BuildingEntity
import com.example.data.local.entity.BuildingPlotEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object BuildingManager {

    private val scope = CoroutineScope(Dispatchers.IO)
    private var isBusy = false

    /**
     * Constructs a new building on an empty unlocked plot.
     */
    fun constructBuilding(
        plot: BuildingPlotEntity,
        type: BuildingType,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (isBusy) return
        isBusy = true

        scope.launch {
            try {
                val player = GameDataProvider.playerRepository.getPlayer()
                if (player == null) {
                    withContext(Dispatchers.Main) {
                        isBusy = false
                        onError("Player error")
                    }
                    return@launch
                }

                val config = BuildingConfigRepository.getConfig(type)
                val cost = config.costForLevel(1)

                if (player.coins < cost) {
                    withContext(Dispatchers.Main) {
                        isBusy = false
                        onError("Not enough coins! Need $cost coins.")
                    }
                    return@launch
                }

                if (player.playerLevel < config.requiredPlayerLevel) {
                    withContext(Dispatchers.Main) {
                        isBusy = false
                        onError("Requires Player Level ${config.requiredPlayerLevel}.")
                    }
                    return@launch
                }

                if (player.totalStars < config.requiredStars) {
                    withContext(Dispatchers.Main) {
                        isBusy = false
                        onError("Requires ${config.requiredStars} Stars.")
                    }
                    return@launch
                }

                // Spend coins
                GameDataProvider.spendCoins(cost)

                val now = System.currentTimeMillis()
                val durationSecs = config.constructionDurationSecs(1)
                val endTime = now + (durationSecs * 1000L)
                val newBuildingId = "building_${plot.plotId}_${type.typeId.lowercase()}"

                val building = BuildingEntity(
                    buildingId = newBuildingId,
                    buildingType = type.typeId,
                    plotId = plot.plotId,
                    level = 1,
                    isBuilt = false,
                    isConstructing = true,
                    constructionStartTime = now,
                    constructionEndTime = endTime,
                    productionPerHour = config.baseProductionPerHour,
                    lastCollectedAt = now
                )

                // Save building and link to plot
                GameDataProvider.buildingRepository.insertBuilding(building)
                GameDataProvider.islandRepository.updatePlot(plot.copy(buildingId = newBuildingId))

                withContext(Dispatchers.Main) {
                    isBusy = false
                    onSuccess()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isBusy = false
                    onError(e.localizedMessage ?: "Construction failed")
                }
            }
        }
    }

    /**
     * Upgrades an existing building to the next level.
     */
    fun upgradeBuilding(
        building: BuildingEntity,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (isBusy) return
        isBusy = true

        scope.launch {
            try {
                if (building.isConstructing) {
                    withContext(Dispatchers.Main) {
                        isBusy = false
                        onError("Building is currently constructing!")
                    }
                    return@launch
                }

                val config = BuildingConfigRepository.getConfig(BuildingType.fromId(building.buildingType))
                val nextLevel = building.level + 1

                if (nextLevel > config.maxLevel) {
                    withContext(Dispatchers.Main) {
                        isBusy = false
                        onError("Building is already at MAX level (${config.maxLevel})!")
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

                val cost = config.costForLevel(nextLevel)
                if (player.coins < cost) {
                    withContext(Dispatchers.Main) {
                        isBusy = false
                        onError("Not enough coins! Need $cost coins.")
                    }
                    return@launch
                }

                // Spend coins
                GameDataProvider.spendCoins(cost)

                val now = System.currentTimeMillis()
                val durationSecs = config.constructionDurationSecs(nextLevel)
                val endTime = now + (durationSecs * 1000L)

                val updatedBuilding = building.copy(
                    level = nextLevel,
                    isConstructing = true,
                    constructionStartTime = now,
                    constructionEndTime = endTime,
                    productionPerHour = config.productionForLevel(nextLevel)
                )

                GameDataProvider.buildingRepository.updateBuilding(updatedBuilding)

                withContext(Dispatchers.Main) {
                    isBusy = false
                    onSuccess()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isBusy = false
                    onError(e.localizedMessage ?: "Upgrade failed")
                }
            }
        }
    }

    /**
     * Instantly finishes construction using Gems.
     */
    fun finishInstantWithGems(
        building: BuildingEntity,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (isBusy) return
        isBusy = true

        scope.launch {
            try {
                if (!building.isConstructing) {
                    withContext(Dispatchers.Main) {
                        isBusy = false
                        onError("Building is not constructing!")
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

                val gemCost = ConstructionManager.getInstantFinishGemCost(building)
                if (player.gems < gemCost) {
                    withContext(Dispatchers.Main) {
                        isBusy = false
                        onError("Not enough Gems! Need $gemCost Gems.")
                    }
                    return@launch
                }

                // Spend Gems
                GameDataProvider.spendGems(gemCost)

                val now = System.currentTimeMillis()
                val updatedBuilding = building.copy(
                    isConstructing = false,
                    isBuilt = true,
                    lastCollectedAt = now
                )

                GameDataProvider.buildingRepository.updateBuilding(updatedBuilding)

                withContext(Dispatchers.Main) {
                    isBusy = false
                    onSuccess()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isBusy = false
                    onError(e.localizedMessage ?: "Instant finish failed")
                }
            }
        }
    }

    /**
     * Collects pending production from a building.
     */
    fun collectProduction(
        building: BuildingEntity,
        onSuccess: (Long) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (isBusy) return
        isBusy = true

        scope.launch {
            try {
                val pendingCoins = ProductionManager.calculatePendingCoins(building)
                if (pendingCoins <= 0) {
                    withContext(Dispatchers.Main) {
                        isBusy = false
                        onError("Nothing to collect yet!")
                    }
                    return@launch
                }

                val now = System.currentTimeMillis()
                val updatedBuilding = building.copy(lastCollectedAt = now)

                GameDataProvider.buildingRepository.updateBuilding(updatedBuilding)
                GameDataProvider.addCoins(pendingCoins)

                withContext(Dispatchers.Main) {
                    isBusy = false
                    onSuccess(pendingCoins)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isBusy = false
                    onError(e.localizedMessage ?: "Collection failed")
                }
            }
        }
    }
}
