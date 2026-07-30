package com.example.game.match3.level

import com.example.core.GameDataProvider
import com.example.game.match3.BoardModel
import com.example.game.match3.goal.GoalManager
import com.example.game.match3.obstacle.Obstacle
import com.example.game.match3.score.ScoreManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LevelController(
    val levelConfig: LevelConfig,
    val boardModel: BoardModel,
    val scoreManager: ScoreManager,
    val goalManager: GoalManager,
    val moveCounter: MoveCounter
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    var gameState: LevelGameState = LevelGameState.READY
        private set

    var finalStarsEarned = 0
        private set

    var finalCoinsEarned = 0
        private set

    var finalXpEarned = 0
        private set

    private var hasProcessedCompletion = false

    init {
        setupBoardObstacles()
        com.example.core.event.GameEventBus.postEvent(
            com.example.core.event.GameEvent(
                type = com.example.core.event.GameEventType.LEVEL_STARTED,
                levelId = levelConfig.levelId
            )
        )
    }

    private fun setupBoardObstacles() {
        for (obs in levelConfig.obstacleLayouts) {
            boardModel.setObstacle(obs.row, obs.col, Obstacle(obs.obstacleType, obs.health))
        }
    }

    fun checkGameStatus(isResolverBusy: Boolean) {
        if (gameState == LevelGameState.VICTORY || gameState == LevelGameState.DEFEAT) return
        if (isResolverBusy) return

        if (goalManager.isAllGoalsCompleted()) {
            gameState = LevelGameState.VICTORY
            onLevelVictory()
        } else if (moveCounter.movesRemaining <= 0) {
            gameState = LevelGameState.DEFEAT
            onLevelDefeat()
        }
    }

    private fun onLevelDefeat() {
        if (hasProcessedCompletion) return
        hasProcessedCompletion = true
        com.example.core.event.GameEventBus.postEvent(
            com.example.core.event.GameEvent(
                type = com.example.core.event.GameEventType.LEVEL_FAILED,
                levelId = levelConfig.levelId
            )
        )
        scope.launch {
            GameDataProvider.levelRepository.recordLoss(levelConfig.levelId)
        }
    }

    private fun onLevelVictory() {
        if (hasProcessedCompletion) return
        hasProcessedCompletion = true
        com.example.core.event.GameEventBus.postEvent(
            com.example.core.event.GameEvent(
                type = com.example.core.event.GameEventType.LEVEL_COMPLETED,
                levelId = levelConfig.levelId
            )
        )

        // Award remaining move bonus
        val unusedMoves = moveCounter.movesRemaining
        val bonusScore = unusedMoves * 50
        scoreManager.addScore(bonusScore)

        val totalScore = scoreManager.currentScore
        finalStarsEarned = StarCalculator.calculateStars(
            allGoalsCompleted = true,
            finalScore = totalScore,
            baseTarget = levelConfig.baseScoreTarget
        )

        finalCoinsEarned = levelConfig.coinReward
        finalXpEarned = levelConfig.xpReward

        // Save progress to Room
        scope.launch {
            val levelRepo = GameDataProvider.levelRepository
            val playerRepo = GameDataProvider.playerRepository

            val existing = levelRepo.getLevel(levelConfig.levelId)
            val oldStars = existing?.stars ?: 0
            val isFirstCompletion = !(existing?.isCompleted ?: false)

            levelRepo.completeLevel(
                levelId = levelConfig.levelId,
                stars = finalStarsEarned,
                score = totalScore,
                movesLeft = unusedMoves
            )

            // Grant rewards & stars
            if (isFirstCompletion) {
                playerRepo.addCoins(finalCoinsEarned.toLong())
                playerRepo.addXp(finalXpEarned.toLong())

                // Grant island building materials based on level progression
                val woodAmount = 15 + (levelConfig.levelId % 5) * 2
                val stoneAmount = 10 + (levelConfig.levelId % 4) * 2
                val metalAmount = if (levelConfig.levelId >= 10) 5 + (levelConfig.levelId % 3) * 2 else 0
                val foodAmount = 10 + (levelConfig.levelId % 4) * 2

                com.example.game.resource.ResourceManager.addResource(com.example.game.resource.ResourceType.WOOD, woodAmount, ignoreCapacity = true)
                com.example.game.resource.ResourceManager.addResource(com.example.game.resource.ResourceType.STONE, stoneAmount, ignoreCapacity = true)
                if (metalAmount > 0) {
                    com.example.game.resource.ResourceManager.addResource(com.example.game.resource.ResourceType.METAL, metalAmount, ignoreCapacity = true)
                }
                com.example.game.resource.ResourceManager.addResource(com.example.game.resource.ResourceType.FOOD, foodAmount, ignoreCapacity = true)
            }

            val starDiff = finalStarsEarned - oldStars
            if (starDiff > 0) {
                playerRepo.addStars(starDiff)
            }

            if (levelConfig.levelId < 100) {
                playerRepo.updateCurrentLevel(levelConfig.levelId + 1)
            }
        }
    }

    fun pause() {
        if (gameState == LevelGameState.READY || gameState == LevelGameState.PLAYER_MOVING) {
            gameState = LevelGameState.PAUSED
        }
    }

    fun resume() {
        if (gameState == LevelGameState.PAUSED) {
            gameState = LevelGameState.READY
        }
    }
}
