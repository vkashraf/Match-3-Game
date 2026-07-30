package com.example.game.world

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.example.core.GameConstants

enum class NodeState {
    LOCKED,
    AVAILABLE,
    PLAYING,
    COMPLETED,
    PERFECT
}

data class LevelNodeConfig(
    val levelId: Int,
    val worldId: Int,
    val positionX: Float,
    val positionY: Float,
    val requiredStars: Int,
    val unlockLevelId: Int,
    val isBossLevel: Boolean,
    val isCheckpoint: Boolean
)

object LevelNodeGenerator {

    private val nodeCache = mutableMapOf<Int, LevelNodeConfig>()

    init {
        generateNodes()
    }

    private fun generateNodes() {
        val nodeSpacingY = 130f
        val startY = 180f

        for (levelId in 1..WorldConfigRepository.TOTAL_LEVELS) {
            val worldId = ((levelId - 1) / WorldConfigRepository.LEVELS_PER_WORLD) + 1
            val y = startY + (levelId - 1) * nodeSpacingY
            
            // Generate a winding sine wave trail
            val wave = MathUtils.sin(levelId * 0.45f)
            val x = GameConstants.VIRTUAL_WIDTH / 2f + wave * 190f

            val isBoss = (levelId % WorldConfigRepository.LEVELS_PER_WORLD == 0)
            val isCheckpoint = (levelId % 5 == 0 && !isBoss)
            val reqStars = (worldId - 1) * 15
            val unlockLvl = if (levelId == 1) 1 else levelId - 1

            val node = LevelNodeConfig(
                levelId = levelId,
                worldId = worldId,
                positionX = x,
                positionY = y,
                requiredStars = reqStars,
                unlockLevelId = unlockLvl,
                isBossLevel = isBoss,
                isCheckpoint = isCheckpoint
            )
            nodeCache[levelId] = node
        }
    }

    fun getNodeConfig(levelId: Int): LevelNodeConfig {
        return nodeCache[levelId] ?: LevelNodeConfig(
            levelId = levelId.coerceIn(1, WorldConfigRepository.TOTAL_LEVELS),
            worldId = 1,
            positionX = GameConstants.VIRTUAL_WIDTH / 2f,
            positionY = 180f,
            requiredStars = 0,
            unlockLevelId = 1,
            isBossLevel = false,
            isCheckpoint = false
        )
    }

    fun getNodePosition(levelId: Int): Vector2 {
        val config = getNodeConfig(levelId)
        return Vector2(config.positionX, config.positionY)
    }

    fun getAllNodes(): List<LevelNodeConfig> = nodeCache.values.toList()
}
