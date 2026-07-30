package com.example.game.match3.match

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.example.game.match3.BoardModel
import com.example.game.match3.board.BoardPosition
import com.example.game.match3.combo.ComboManager
import com.example.game.match3.effect.ParticleEffectManager
import com.example.game.match3.effect.ScreenShakeController
import com.example.game.match3.goal.GoalManager
import com.example.game.match3.gravity.GravityManager
import com.example.game.match3.level.MoveCounter
import com.example.game.match3.move.MoveValidator
import com.example.game.match3.obstacle.ObstacleManager
import com.example.game.match3.obstacle.ObstacleType
import com.example.game.match3.score.FloatingText
import com.example.game.match3.score.ScoreConfig
import com.example.game.match3.score.ScoreManager
import com.example.game.match3.shuffle.BoardShuffleManager
import com.example.game.match3.spawn.TileSpawnManager
import com.example.game.match3.special.SpecialComboResolver
import com.example.game.match3.special.SpecialType
import com.example.game.match3.swap.SwapManager
import com.example.game.match3.tile.Tile
import com.example.game.match3.tile.TileState
import com.example.game.match3.tile.TileType
import com.example.manager.SoundManager
import com.example.utils.HapticManager

enum class ResolverState {
    IDLE,
    ANIMATING_SWAP,
    CHECKING_SWAP,
    ANIMATING_SWAP_BACK,
    CLEARING_TILES,
    APPLYING_GRAVITY,
    SPAWNING_TILES,
    CHECKING_CASCADE,
    SHUFFLING
}

class MatchResolver(
    private val boardModel: BoardModel,
    private val swapManager: SwapManager,
    private val moveCounter: MoveCounter,
    private val scoreManager: ScoreManager,
    private val goalManager: GoalManager,
    private val tileSize: Float,
    val particleEffectManager: ParticleEffectManager = ParticleEffectManager(),
    val screenShakeController: ScreenShakeController = ScreenShakeController()
) {

    val matchDetector = MatchDetector()
    val gravityManager = GravityManager()
    val tileSpawnManager = TileSpawnManager()
    val comboManager = ComboManager()
    val moveValidator = MoveValidator(matchDetector)
    val boardShuffleManager = BoardShuffleManager(matchDetector, moveValidator)

    val specialComboResolver = SpecialComboResolver()
    val obstacleManager = ObstacleManager()

    var state = ResolverState.IDLE
        private set

    val isBusy: Boolean get() = state != ResolverState.IDLE

    // Active swap tracking
    private var swapR1 = -1
    private var swapC1 = -1
    private var swapR2 = -1
    private var swapC2 = -1

    // Clear animation tracking
    private var clearAnimTime = 0f
    private val clearDuration = 0.25f // 250ms
    private val clearingTiles = mutableListOf<Tile>()

    // Floating text popups
    val floatingTexts = mutableListOf<FloatingText>()

    fun requestSwap(r1: Int, c1: Int, r2: Int, c2: Int) {
        if (isBusy) return

        swapR1 = r1
        swapC1 = c1
        swapR2 = r2
        swapC2 = c2

        state = ResolverState.ANIMATING_SWAP
        swapManager.startSwap(r1, c1, r2, c2) {
            state = ResolverState.CHECKING_SWAP
        }
    }

    fun update(delta: Float, boardX: Float, boardY: Float, camera: OrthographicCamera? = null) {
        particleEffectManager.update(delta)
        if (camera != null) {
            screenShakeController.update(delta, camera, camera.position.x, camera.position.y)
        }

        // Update floating text popups
        val it = floatingTexts.iterator()
        while (it.hasNext()) {
            val text = it.next()
            text.update(delta)
            if (text.isDead) {
                it.remove()
            }
        }

        when (state) {
            ResolverState.IDLE -> {}

            ResolverState.ANIMATING_SWAP -> {
                swapManager.update(delta, tileSize)
            }

            ResolverState.CHECKING_SWAP -> {
                val t1 = boardModel.getTile(swapR1, swapC1)
                val t2 = boardModel.getTile(swapR2, swapC2)

                val isSpecialSwap = t1 != null && t2 != null &&
                        ((t1.specialType != SpecialType.NONE && t2.specialType != SpecialType.NONE) ||
                         t1.specialType == SpecialType.RAINBOW || t2.specialType == SpecialType.RAINBOW)

                if (isSpecialSwap) {
                    moveCounter.consumeMove()
                    comboManager.reset()

                    val (expandedPositions, bonusScore) = specialComboResolver.expandSpecialActivations(
                        boardModel = boardModel,
                        initialPositions = emptySet(),
                        isSpecialSwap = true,
                        swapPos1 = BoardPosition(swapR1, swapC1),
                        swapPos2 = BoardPosition(swapR2, swapC2)
                    )
                    startClearingTiles(expandedPositions, bonusScore, boardX, boardY)
                } else {
                    val matchResult = matchDetector.findAllMatches(boardModel)
                    if (matchResult.hasMatch) {
                        moveCounter.consumeMove()
                        comboManager.reset()

                        // Detect & create special tiles (Rockets, Bombs, Rainbows)
                        val specialCreations = specialComboResolver.detectSpecialCreations(
                            groups = matchResult.groups,
                            swapPos = BoardPosition(swapR2, swapC2)
                        )

                        val initialPositions = matchResult.matchedPositions.toMutableSet()

                        for (creation in specialCreations) {
                            val pos = creation.spawnPosition
                            val tile = boardModel.getTile(pos.row, pos.col)
                            if (tile != null) {
                                tile.specialType = creation.specialType
                                initialPositions.remove(pos) // Do not clear the newly created special tile
                                val worldX = boardX + pos.col * tileSize + tileSize / 2f
                                val worldY = boardY + pos.row * tileSize + tileSize / 2f
                                particleEffectManager.spawnExplosion(worldX, worldY, Color.GOLD, 15)

                                com.example.core.event.GameEventBus.postEvent(
                                    com.example.core.event.GameEvent(
                                        type = com.example.core.event.GameEventType.SPECIAL_CREATED,
                                        itemId = creation.specialType.name
                                    )
                                )
                            }
                        }

                        // Expand special activations
                        val (expandedPositions, bonusScore) = specialComboResolver.expandSpecialActivations(
                            boardModel = boardModel,
                            initialPositions = initialPositions,
                            isSpecialSwap = false,
                            swapPos1 = null,
                            swapPos2 = null
                        )

                        startClearingTiles(expandedPositions, bonusScore, boardX, boardY)
                    } else {
                        // Invalid swap -> animate back
                        state = ResolverState.ANIMATING_SWAP_BACK
                        swapManager.startSwap(swapR1, swapC1, swapR2, swapC2) {
                            state = ResolverState.IDLE
                        }
                    }
                }
            }

            ResolverState.ANIMATING_SWAP_BACK -> {
                swapManager.update(delta, tileSize)
            }

            ResolverState.CLEARING_TILES -> {
                clearAnimTime += delta
                val progress = Math.min(1f, clearAnimTime / clearDuration)

                val scale = 1f - progress
                val alpha = 1f - progress

                for (tile in clearingTiles) {
                    tile.scale = Math.max(0f, scale)
                    tile.alpha = Math.max(0f, alpha)
                }

                if (progress >= 1f) {
                    for (tile in clearingTiles) {
                        tile.state = TileState.EMPTY
                        tile.scale = 1f
                        tile.alpha = 1f
                    }
                    clearingTiles.clear()

                    state = ResolverState.APPLYING_GRAVITY
                    gravityManager.applyGravity(boardModel, tileSize)
                }
            }

            ResolverState.APPLYING_GRAVITY -> {
                gravityManager.update(delta)
                if (!gravityManager.isFalling) {
                    state = ResolverState.SPAWNING_TILES
                    tileSpawnManager.spawnNewTiles(boardModel, tileSize)
                }
            }

            ResolverState.SPAWNING_TILES -> {
                tileSpawnManager.update(delta)
                if (!tileSpawnManager.isSpawning) {
                    state = ResolverState.CHECKING_CASCADE
                }
            }

            ResolverState.CHECKING_CASCADE -> {
                val matchResult = matchDetector.findAllMatches(boardModel)
                if (matchResult.hasMatch) {
                    val specialCreations = specialComboResolver.detectSpecialCreations(
                        groups = matchResult.groups,
                        swapPos = null
                    )

                    val initialPositions = matchResult.matchedPositions.toMutableSet()

                    for (creation in specialCreations) {
                        val pos = creation.spawnPosition
                        val tile = boardModel.getTile(pos.row, pos.col)
                        if (tile != null) {
                            tile.specialType = creation.specialType
                            initialPositions.remove(pos)

                            com.example.core.event.GameEventBus.postEvent(
                                com.example.core.event.GameEvent(
                                    type = com.example.core.event.GameEventType.SPECIAL_CREATED,
                                    itemId = creation.specialType.name
                                )
                            )
                        }
                    }

                    val (expandedPositions, bonusScore) = specialComboResolver.expandSpecialActivations(
                        boardModel = boardModel,
                        initialPositions = initialPositions,
                        isSpecialSwap = false,
                        swapPos1 = null,
                        swapPos2 = null
                    )

                    startClearingTiles(expandedPositions, bonusScore, boardX, boardY)
                } else {
                    comboManager.reset()

                    if (!moveValidator.hasAnyValidMove(boardModel) && !goalManager.isAllGoalsCompleted()) {
                        state = ResolverState.SHUFFLING
                        boardShuffleManager.shuffleBoard(boardModel)
                        floatingTexts.add(
                            FloatingText("NO MOVES! SHUFFLING...", boardX + 100f, boardY + 200f, duration = 1.2f)
                        )
                    } else {
                        state = ResolverState.IDLE
                    }
                }
            }

            ResolverState.SHUFFLING -> {
                boardShuffleManager.update(delta, boardModel)
                if (!boardShuffleManager.isShuffling) {
                    state = ResolverState.IDLE
                }
            }
        }
    }

    fun triggerBoosterClear(positions: Set<BoardPosition>, bonusScore: Int = 150) {
        if (positions.isEmpty()) return
        val (expandedPositions, bonus) = specialComboResolver.expandSpecialActivations(
            boardModel = boardModel,
            initialPositions = positions,
            isSpecialSwap = false,
            swapPos1 = null,
            swapPos2 = null
        )
        startClearingTiles(expandedPositions, bonusScore + bonus, 0f, 0f)
    }

    private fun startClearingTiles(
        positions: Set<BoardPosition>,
        bonusScore: Int,
        boardX: Float,
        boardY: Float
    ) {
        val combo = comboManager.increment()
        val comboMultiplier = comboManager.getMultiplier()

        if (combo > 1) {
            com.example.core.event.GameEventBus.postEvent(
                com.example.core.event.GameEvent(
                    type = com.example.core.event.GameEventType.COMBO_TRIGGERED,
                    amount = combo
                )
            )
            com.example.core.event.GameEventBus.postEvent(
                com.example.core.event.GameEvent(
                    type = com.example.core.event.GameEventType.CASCADE_TRIGGERED,
                    amount = combo
                )
            )
            com.example.manager.SoundManager.playSound("combo")
            if (combo >= 5) {
                com.example.utils.HapticManager.vibrateStrong()
            } else {
                com.example.utils.HapticManager.vibrateMedium()
            }
        } else {
            com.example.manager.SoundManager.playSound("tile_match")
            com.example.utils.HapticManager.vibrateSmall()
        }

        val basePoints = ScoreConfig.calculateScore(positions.size, comboMultiplier) + bonusScore
        scoreManager.addScore(basePoints)

        val clearedTypes = mutableListOf<TileType>()
        val destroyedObstacleTypes = mutableListOf<ObstacleType>()
        clearingTiles.clear()

        var avgX = 0f
        var avgY = 0f

        // Damage adjacent and direct obstacles
        val destroyedAdj = obstacleManager.damageAdjacentObstacles(boardModel, positions)
        for ((pos, obsType) in destroyedAdj) {
            destroyedObstacleTypes.add(obsType)
            val worldX = boardX + pos.col * tileSize + tileSize / 2f
            val worldY = boardY + pos.row * tileSize + tileSize / 2f
            particleEffectManager.spawnExplosion(worldX, worldY, Color.BROWN, 10)
        }

        for (pos in positions) {
            val directObs = obstacleManager.damageObstacle(boardModel, pos.row, pos.col, 1)
            if (directObs != null) {
                destroyedObstacleTypes.add(directObs)
            }

            val tile = boardModel.getTile(pos.row, pos.col)
            if (tile != null) {
                tile.state = TileState.DESTROYING
                clearingTiles.add(tile)
                clearedTypes.add(tile.type)

                val worldX = boardX + pos.col * tileSize + tileSize / 2f
                val worldY = boardY + pos.row * tileSize + tileSize / 2f
                avgX += worldX
                avgY += worldY

                if (tile.specialType != SpecialType.NONE) {
                    screenShakeController.shake(0.3f, 12f)
                    particleEffectManager.spawnExplosion(worldX, worldY, Color.CYAN, 18)
                    com.example.manager.SoundManager.playSound("special_activate")
                    com.example.utils.HapticManager.vibrateMedium()
                    com.example.core.event.GameEventBus.postEvent(
                        com.example.core.event.GameEvent(
                            type = com.example.core.event.GameEventType.SPECIAL_ACTIVATED,
                            itemId = tile.specialType.name
                        )
                    )
                } else {
                    particleEffectManager.spawnExplosion(worldX, worldY, Color.CORAL, 6)
                }
            }
        }

        if (clearingTiles.isNotEmpty()) {
            avgX /= clearingTiles.size
            avgY /= clearingTiles.size
        } else {
            avgX = boardX + 200f
            avgY = boardY + 200f
        }

        goalManager.onTilesCleared(clearedTypes)
        if (clearedTypes.isNotEmpty()) {
            com.example.core.event.GameEventBus.postEvent(
                com.example.core.event.GameEvent(
                    type = com.example.core.event.GameEventType.TILE_MATCHED,
                    amount = clearedTypes.size
                )
            )
            com.example.core.event.GameEventBus.postEvent(
                com.example.core.event.GameEvent(
                    type = com.example.core.event.GameEventType.TILES_CLEARED,
                    amount = clearedTypes.size
                )
            )
        }
        if (destroyedObstacleTypes.isNotEmpty()) {
            goalManager.onObstaclesDestroyed(destroyedObstacleTypes)
            com.example.core.event.GameEventBus.postEvent(
                com.example.core.event.GameEvent(
                    type = com.example.core.event.GameEventType.OBSTACLE_DESTROYED,
                    amount = destroyedObstacleTypes.size
                )
            )
        }

        val milestoneStr = when (combo) {
            3 -> "\nNICE!"
            5 -> "\nGREAT!"
            8 -> "\nAMAZING!"
            10 -> "\nUNSTOPPABLE!"
            else -> ""
        }

        val textStr = if (combo > 1) "+$basePoints\nCOMBO x$combo!$milestoneStr" else "+$basePoints"
        floatingTexts.add(FloatingText(textStr, avgX, avgY))

        clearAnimTime = 0f
        state = ResolverState.CLEARING_TILES
    }
}
