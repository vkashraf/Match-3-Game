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
        }
    }

    private fun onLevelVictory() {
        if (hasProcessedCompletion) return
        hasProcessedCompletion = true

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
