package com.example.game.resource

import com.example.core.GameDataProvider
import com.example.core.event.GameEvent
import com.example.core.event.GameEventBus
import com.example.core.event.GameEventType
import com.example.game.island.building.BuildingType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object ResourceManager {

    private val scope = CoroutineScope(Dispatchers.IO)

    const val BASE_STORAGE_CAPACITY = 200

    private val _woodFlow = MutableStateFlow(0)
    val woodFlow: StateFlow<Int> = _woodFlow.asStateFlow()

    private val _stoneFlow = MutableStateFlow(0)
    val stoneFlow: StateFlow<Int> = _stoneFlow.asStateFlow()

    private val _metalFlow = MutableStateFlow(0)
    val metalFlow: StateFlow<Int> = _metalFlow.asStateFlow()

    private val _foodFlow = MutableStateFlow(0)
    val foodFlow: StateFlow<Int> = _foodFlow.asStateFlow()

    init {
        scope.launch {
            try {
                GameDataProvider.inventoryRepository.allItemsFlow.collect { items ->
                    _woodFlow.value = items.find { it.itemId == "WOOD" }?.quantity ?: 0
                    _stoneFlow.value = items.find { it.itemId == "STONE" }?.quantity ?: 0
                    _metalFlow.value = items.find { it.itemId == "METAL" }?.quantity ?: 0
                    _foodFlow.value = items.find { it.itemId == "FOOD" }?.quantity ?: 0
                }
            } catch (e: Exception) {
                // Ignore initialization flow errors
            }
        }
    }

    suspend fun getStorageCapacity(): Int = withContext(Dispatchers.IO) {
        val buildings = GameDataProvider.buildingRepository.getAllBuildings()
        val storageBuilding = buildings.find { it.buildingType == BuildingType.STORAGE.typeId || it.buildingType == "STORAGE" }
        val level = if (storageBuilding != null && storageBuilding.isBuilt) storageBuilding.level else 1
        return@withContext BASE_STORAGE_CAPACITY * level
    }

    suspend fun getMaterialCount(type: ResourceType): Int = withContext(Dispatchers.IO) {
        when (type) {
            ResourceType.WOOD -> _woodFlow.value
            ResourceType.STONE -> _stoneFlow.value
            ResourceType.METAL -> _metalFlow.value
            ResourceType.FOOD -> _foodFlow.value
            else -> 0
        }
    }

    suspend fun getResourceAmount(type: ResourceType): Int = getMaterialCount(type)

    suspend fun spendResource(type: ResourceType, amount: Int): Boolean = withContext(Dispatchers.IO) {
        if (amount <= 0) return@withContext false
        when (type) {
            ResourceType.WOOD -> spendResources(wood = amount)
            ResourceType.STONE -> spendResources(stone = amount)
            ResourceType.METAL -> spendResources(metal = amount)
            ResourceType.FOOD -> spendResources(food = amount)
            ResourceType.COINS -> spendResources(coins = amount.toLong())
            ResourceType.GEMS -> spendResources(gems = amount)
            else -> false
        }
    }

    suspend fun isStorageFull(type: ResourceType): Boolean = withContext(Dispatchers.IO) {
        if (type != ResourceType.WOOD && type != ResourceType.STONE &&
            type != ResourceType.METAL && type != ResourceType.FOOD) {
            return@withContext false
        }
        val capacity = getStorageCapacity()
        val current = getMaterialCount(type)
        return@withContext current >= capacity
    }

    suspend fun addResource(type: ResourceType, amount: Int, ignoreCapacity: Boolean = false): Boolean = withContext(Dispatchers.IO) {
        if (amount <= 0) return@withContext false

        when (type) {
            ResourceType.COINS -> {
                GameDataProvider.playerRepository.addCoins(amount.toLong())
                GameEventBus.postEvent(GameEvent(type = GameEventType.COINS_EARNED, amount = amount))
                true
            }
            ResourceType.GEMS -> {
                GameDataProvider.playerRepository.addGems(amount)
                GameEventBus.postEvent(GameEvent(type = GameEventType.GEMS_EARNED, amount = amount))
                true
            }
            ResourceType.ENERGY -> {
                GameDataProvider.playerRepository.addEnergy(amount)
                true
            }
            ResourceType.XP -> {
                GameDataProvider.playerRepository.addXp(amount.toLong())
                true
            }
            ResourceType.WOOD, ResourceType.STONE, ResourceType.METAL, ResourceType.FOOD -> {
                val current = getMaterialCount(type)
                val capacity = getStorageCapacity()
                val addable = if (ignoreCapacity) amount else (capacity - current).coerceAtLeast(0).coerceAtMost(amount)

                if (addable <= 0) return@withContext false

                val success = GameDataProvider.inventoryRepository.addItem(type.id, addable, "MATERIAL")
                if (success) {
                    GameEventBus.postEvent(GameEvent(type = GameEventType.RESOURCE_COLLECTED, itemId = type.id, amount = addable))
                }
                success
            }
        }
    }

    suspend fun canAfford(
        coins: Long = 0L,
        wood: Int = 0,
        stone: Int = 0,
        metal: Int = 0,
        food: Int = 0,
        gems: Int = 0
    ): Boolean = withContext(Dispatchers.IO) {
        val player = GameDataProvider.playerRepository.getPlayer() ?: return@withContext false
        if (player.coins < coins) return@withContext false
        if (player.gems < gems) return@withContext false

        if (wood > 0 && GameDataProvider.inventoryRepository.getQuantity("WOOD") < wood) return@withContext false
        if (stone > 0 && GameDataProvider.inventoryRepository.getQuantity("STONE") < stone) return@withContext false
        if (metal > 0 && GameDataProvider.inventoryRepository.getQuantity("METAL") < metal) return@withContext false
        if (food > 0 && GameDataProvider.inventoryRepository.getQuantity("FOOD") < food) return@withContext false

        return@withContext true
    }

    suspend fun spendResources(
        coins: Long = 0L,
        wood: Int = 0,
        stone: Int = 0,
        metal: Int = 0,
        food: Int = 0,
        gems: Int = 0
    ): Boolean = withContext(Dispatchers.IO) {
        if (!canAfford(coins, wood, stone, metal, food, gems)) return@withContext false

        if (coins > 0) GameDataProvider.playerRepository.spendCoins(coins)
        if (gems > 0) GameDataProvider.playerRepository.spendGems(gems)
        if (wood > 0) GameDataProvider.inventoryRepository.removeItem("WOOD", wood)
        if (stone > 0) GameDataProvider.inventoryRepository.removeItem("STONE", stone)
        if (metal > 0) GameDataProvider.inventoryRepository.removeItem("METAL", metal)
        if (food > 0) GameDataProvider.inventoryRepository.removeItem("FOOD", food)

        return@withContext true
    }
}
