package com.example.game.island.decoration

import com.example.core.GameDataProvider
import com.example.core.event.GameEvent
import com.example.core.event.GameEventBus
import com.example.core.event.GameEventType
import com.example.data.local.entity.DecorationEntity
import com.example.game.resource.ResourceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object DecorationManager {

    private val scope = CoroutineScope(Dispatchers.IO)

    fun placeDecoration(
        decorationId: String,
        gridX: Int,
        gridY: Int,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        scope.launch {
            try {
                val config = DecorationConfigRepository.getConfig(decorationId)
                val affordable = ResourceManager.canAfford(
                    coins = config.costCoins,
                    gems = config.costGems
                )

                if (!affordable) {
                    withContext(Dispatchers.Main) {
                        onError("Not enough resources to place ${config.name}!")
                    }
                    return@launch
                }

                ResourceManager.spendResources(
                    coins = config.costCoins,
                    gems = config.costGems
                )

                val instanceId = "dec_${System.currentTimeMillis()}_${decorationId.lowercase()}"
                val entity = DecorationEntity(
                    decorationInstanceId = instanceId,
                    decorationId = decorationId,
                    gridX = gridX,
                    gridY = gridY,
                    rotation = 0
                )

                GameDataProvider.decorationRepository.insertDecoration(entity)

                GameEventBus.postEvent(
                    GameEvent(
                        type = GameEventType.DECORATION_PLACED,
                        itemId = decorationId
                    )
                )

                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e.localizedMessage ?: "Placement failed")
                }
            }
        }
    }

    fun rotateDecoration(
        entity: DecorationEntity,
        onSuccess: () -> Unit = {}
    ) {
        scope.launch {
            val newRotation = (entity.rotation + 90) % 360
            val updated = entity.copy(rotation = newRotation)
            GameDataProvider.decorationRepository.updateDecoration(updated)

            GameEventBus.postEvent(
                GameEvent(
                    type = GameEventType.DECORATION_MOVED,
                    itemId = entity.decorationId
                )
            )

            withContext(Dispatchers.Main) {
                onSuccess()
            }
        }
    }

    fun removeDecoration(
        entity: DecorationEntity,
        onSuccess: () -> Unit = {}
    ) {
        scope.launch {
            GameDataProvider.decorationRepository.deleteDecoration(entity.decorationInstanceId)

            GameEventBus.postEvent(
                GameEvent(
                    type = GameEventType.DECORATION_REMOVED,
                    itemId = entity.decorationId
                )
            )

            withContext(Dispatchers.Main) {
                onSuccess()
            }
        }
    }
}
