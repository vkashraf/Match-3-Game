package com.example.game.island.building

import com.example.core.GameDataProvider
import com.example.core.event.GameEvent
import com.example.core.event.GameEventBus
import com.example.core.event.GameEventType
import com.example.data.local.entity.BuildingEntity
import com.example.data.local.entity.BuildingPlotEntity
import com.example.game.resource.ResourceManager
import com.example.game.resource.ResourceType
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
                val costCoins = config.costCoinsForLevel(1)
                val costWood = config.costWoodForLevel(1)
                val costStone = config.costStoneForLevel(1)
                val costMetal = config.costMetalForLevel(1)

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

                val affordable = ResourceManager.canAfford(
                    coins = costCoins,
                    wood = costWood,
                    stone = costStone,
                    metal = costMetal
                )

                if (!affordable) {
                    withContext(Dispatchers.Main) {
                        isBusy = false
                        onError("Insufficient materials or coins to construct ${type.displayName}!")
                    }
                    return@launch
                }

                // Spend resources
                ResourceManager.spendResources(
                    coins = costCoins,
                    wood = costWood,
                    stone = costStone,
                    metal = costMetal
                )

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

                GameEventBus.postEvent(
                    GameEvent(
                        type = GameEventType.BUILDING_BUILT,
                        buildingId = newBuildingId
                    )
                )

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

                val costCoins = config.costCoinsForLevel(nextLevel)
                val costWood = config.costWoodForLevel(nextLevel)
                val costStone = config.costStoneForLevel(nextLevel)
                val costMetal = config.costMetalForLevel(nextLevel)

                val affordable = ResourceManager.canAfford(
                    coins = costCoins,
                    wood = costWood,
                    stone = costStone,
                    metal = costMetal
                )

                if (!affordable) {
                    withContext(Dispatchers.Main) {
                        isBusy = false
                        onError("Not enough resources to upgrade to Level $nextLevel!")
                    }
                    return@launch
                }

                // Spend resources
                ResourceManager.spendResources(
                    coins = costCoins,
                    wood = costWood,
                    stone = costStone,
                    metal = costMetal
                )

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

                GameEventBus.postEvent(
                    GameEvent(
                        type = GameEventType.BUILDING_UPGRADED,
                        buildingId = updatedBuilding.buildingId
                    )
                )

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
        onSuccess: (Int) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (isBusy) return
        isBusy = true

        scope.launch {
            try {
                val pendingAmount = ProductionManager.calculatePendingAmount(building)
                val resType = ProductionManager.getProductionType(building)

                if (pendingAmount <= 0) {
                    withContext(Dispatchers.Main) {
                        isBusy = false
                        onError("Nothing to collect yet!")
                    }
                    return@launch
                }

                if (ResourceManager.isStorageFull(resType)) {
                    withContext(Dispatchers.Main) {
                        isBusy = false
                        onError("Storage for ${resType.displayName} is FULL! Upgrade Storage to collect more.")
                    }
                    return@launch
                }

                val now = System.currentTimeMillis()
                val updatedBuilding = building.copy(lastCollectedAt = now)

                GameDataProvider.buildingRepository.updateBuilding(updatedBuilding)
                ResourceManager.addResource(resType, pendingAmount)

                GameEventBus.postEvent(
                    GameEvent(
                        type = GameEventType.PRODUCTION_COLLECTED,
                        amount = pendingAmount
                    )
                )

                withContext(Dispatchers.Main) {
                    isBusy = false
                    onSuccess(pendingAmount)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isBusy = false
                    onError(e.localizedMessage ?: "Collection failed")
                }
            }
        }
    }

    /**
     * Relocates a building to a new empty unlocked plot.
     */
    fun moveBuilding(
        building: BuildingEntity,
        currentPlot: BuildingPlotEntity,
        newPlot: BuildingPlotEntity,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (isBusy) return
        isBusy = true

        scope.launch {
            try {
                if (!newPlot.isUnlocked || newPlot.buildingId != null) {
                    withContext(Dispatchers.Main) {
                        isBusy = false
                        onError("Target plot is unavailable!")
                    }
                    return@launch
                }

                // Clear current plot and assign to new plot
                GameDataProvider.islandRepository.updatePlot(currentPlot.copy(buildingId = null))
                GameDataProvider.islandRepository.updatePlot(newPlot.copy(buildingId = building.buildingId))
                GameDataProvider.buildingRepository.updateBuilding(building.copy(plotId = newPlot.plotId))

                withContext(Dispatchers.Main) {
                    isBusy = false
                    onSuccess()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isBusy = false
                    onError(e.localizedMessage ?: "Move building failed")
                }
            }
        }
    }
}
