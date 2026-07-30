package com.example.game.match3.booster

import com.example.core.GameDataProvider
import com.example.game.match3.BoardModel
import com.example.game.match3.level.LevelController
import com.example.game.match3.level.LevelGameState
import com.example.game.match3.match.MatchResolver
import com.example.game.match3.special.SpecialType
import com.example.game.match3.tile.Tile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class BoosterState {
    NONE,
    HAMMER_SELECT,
    SWAP_SELECT_FIRST,
    SWAP_SELECT_SECOND,
    ROW_CLEAR_SELECT,
    COLOR_REMOVE_SELECT
}

class BoosterController(
    private val boardModel: BoardModel,
    private val matchResolver: MatchResolver,
    private val levelController: LevelController
) {

    var currentState: BoosterState = BoosterState.NONE
        private set

    var activeBooster: BoosterType? = null
        private set

    var selectedFirstTile: Pair<Int, Int>? = null
        private set

    private val scope = CoroutineScope(Dispatchers.IO)

    fun selectBooster(
        booster: BoosterType,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (levelController.gameState == LevelGameState.VICTORY ||
            levelController.gameState == LevelGameState.DEFEAT
        ) {
            onError("Cannot use boosters after level end.")
            return
        }

        scope.launch {
            val hasItem = GameDataProvider.inventoryRepository.hasItem(booster.id, 1)
            if (!hasItem) {
                withContext(Dispatchers.Main) {
                    onError("No ${booster.displayName} in inventory!")
                }
                return@launch
            }

            withContext(Dispatchers.Main) {
                when (booster) {
                    BoosterType.EXTRA_MOVES -> {
                        executeExtraMoves(booster, onSuccess, onError)
                    }
                    BoosterType.SHUFFLE -> {
                        executeShuffle(booster, onSuccess, onError)
                    }
                    BoosterType.HAMMER -> {
                        activeBooster = booster
                        currentState = BoosterState.HAMMER_SELECT
                        onSuccess("Select a tile or obstacle to destroy.")
                    }
                    BoosterType.SWAP -> {
                        activeBooster = booster
                        selectedFirstTile = null
                        currentState = BoosterState.SWAP_SELECT_FIRST
                        onSuccess("Select first tile to swap.")
                    }
                    BoosterType.ROW_CLEAR -> {
                        activeBooster = booster
                        currentState = BoosterState.ROW_CLEAR_SELECT
                        onSuccess("Select a row to clear.")
                    }
                    BoosterType.COLOR_REMOVE -> {
                        activeBooster = booster
                        currentState = BoosterState.COLOR_REMOVE_SELECT
                        onSuccess("Select a tile color to clear.")
                    }
                    else -> {
                        onError("Booster not applicable during level.")
                    }
                }
            }
        }
    }

    fun cancelBooster() {
        currentState = BoosterState.NONE
        activeBooster = null
        selectedFirstTile = null
    }

    fun handleTileTap(
        row: Int,
        col: Int,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!boardModel.isValidPosition(row, col)) return

        when (currentState) {
            BoosterState.HAMMER_SELECT -> {
                executeHammerOnTile(row, col, onSuccess, onError)
            }
            BoosterState.ROW_CLEAR_SELECT -> {
                executeRowClear(row, col, onSuccess, onError)
            }
            BoosterState.COLOR_REMOVE_SELECT -> {
                executeColorRemove(row, col, onSuccess, onError)
            }
            BoosterState.SWAP_SELECT_FIRST -> {
                selectedFirstTile = Pair(row, col)
                currentState = BoosterState.SWAP_SELECT_SECOND
                onSuccess("Now select adjacent tile to swap.")
            }
            BoosterState.SWAP_SELECT_SECOND -> {
                val first = selectedFirstTile
                if (first == null) {
                    cancelBooster()
                    return
                }

                val rowDiff = kotlin.math.abs(first.first - row)
                val colDiff = kotlin.math.abs(first.second - col)

                if (rowDiff + colDiff == 1) {
                    executeSwapBetweenTiles(first.first, first.second, row, col, onSuccess, onError)
                } else if (first.first == row && first.second == col) {
                    cancelBooster()
                    onSuccess("Swap cancelled.")
                } else {
                    selectedFirstTile = Pair(row, col)
                    onSuccess("Select adjacent tile to swap.")
                }
            }
            BoosterState.NONE -> { /* handled by standard board input */ }
        }
    }

    private fun executeExtraMoves(
        booster: BoosterType,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        scope.launch {
            val removed = GameDataProvider.inventoryRepository.removeItem(booster.id, 1)
            if (removed) {
                com.example.core.event.GameEventBus.postEvent(
                    com.example.core.event.GameEvent(
                        type = com.example.core.event.GameEventType.BOOSTER_USED,
                        itemId = booster.id
                    )
                )
                withContext(Dispatchers.Main) {
                    levelController.moveCounter.addMoves(5)
                    cancelBooster()
                    onSuccess("+5 Extra Moves added!")
                }
            } else {
                withContext(Dispatchers.Main) {
                    onError("Failed to consume booster.")
                }
            }
        }
    }

    private fun executeShuffle(
        booster: BoosterType,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        scope.launch {
            val removed = GameDataProvider.inventoryRepository.removeItem(booster.id, 1)
            if (removed) {
                com.example.core.event.GameEventBus.postEvent(
                    com.example.core.event.GameEvent(
                        type = com.example.core.event.GameEventType.BOOSTER_USED,
                        itemId = booster.id
                    )
                )
                withContext(Dispatchers.Main) {
                    var attempts = 0
                    do {
                        shuffleBoardTiles()
                        attempts++
                    } while (!hasAnyValidMatchPossibility() && attempts < 10)

                    cancelBooster()
                    onSuccess("Board Shuffled!")
                }
            } else {
                withContext(Dispatchers.Main) {
                    onError("Failed to consume booster.")
                }
            }
        }
    }

    private fun executeHammerOnTile(
        row: Int,
        col: Int,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val booster = activeBooster ?: BoosterType.HAMMER
        scope.launch {
            val removed = GameDataProvider.inventoryRepository.removeItem(booster.id, 1)
            if (removed) {
                com.example.core.event.GameEventBus.postEvent(
                    com.example.core.event.GameEvent(
                        type = com.example.core.event.GameEventType.BOOSTER_USED,
                        itemId = booster.id
                    )
                )
                withContext(Dispatchers.Main) {
                    val tile = boardModel.getTile(row, col)
                    if (tile != null) {
                        boardModel.setTile(row, col, null)
                        levelController.goalManager.onTilesCleared(listOf(tile.type))
                        levelController.scoreManager.addScore(100)
                    }

                    val obstacle = boardModel.getObstacle(row, col)
                    if (obstacle != null) {
                        obstacle.health--
                        if (obstacle.health <= 0) {
                            boardModel.setObstacle(row, col, null)
                            levelController.goalManager.onObstaclesDestroyed(listOf(obstacle.type))
                        }
                    }

                    cancelBooster()
                    onSuccess("Hammer used!")
                }
            } else {
                withContext(Dispatchers.Main) {
                    onError("Failed to consume Hammer.")
                }
            }
        }
    }

    private fun executeRowClear(
        row: Int,
        col: Int,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val booster = activeBooster ?: BoosterType.ROW_CLEAR
        scope.launch {
            val removed = GameDataProvider.inventoryRepository.removeItem(booster.id, 1)
            if (removed) {
                com.example.core.event.GameEventBus.postEvent(
                    com.example.core.event.GameEvent(
                        type = com.example.core.event.GameEventType.BOOSTER_USED,
                        itemId = booster.id
                    )
                )
                withContext(Dispatchers.Main) {
                    val positionsToClear = mutableSetOf<com.example.game.match3.board.BoardPosition>()
                    for (c in 0 until boardModel.columns) {
                        positionsToClear.add(com.example.game.match3.board.BoardPosition(row, c))
                    }
                    matchResolver.triggerBoosterClear(positionsToClear)
                    cancelBooster()
                    onSuccess("Row Cleared!")
                }
            } else {
                withContext(Dispatchers.Main) { onError("Failed to consume Row Clear booster.") }
            }
        }
    }

    private fun executeColorRemove(
        row: Int,
        col: Int,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val booster = activeBooster ?: BoosterType.COLOR_REMOVE
        val targetTile = boardModel.getTile(row, col)
        if (targetTile == null) {
            cancelBooster()
            return
        }
        val targetType = targetTile.type
        scope.launch {
            val removed = GameDataProvider.inventoryRepository.removeItem(booster.id, 1)
            if (removed) {
                com.example.core.event.GameEventBus.postEvent(
                    com.example.core.event.GameEvent(
                        type = com.example.core.event.GameEventType.BOOSTER_USED,
                        itemId = booster.id
                    )
                )
                withContext(Dispatchers.Main) {
                    val positionsToClear = mutableSetOf<com.example.game.match3.board.BoardPosition>()
                    for (r in 0 until boardModel.rows) {
                        for (c in 0 until boardModel.columns) {
                            val tile = boardModel.getTile(r, c)
                            if (tile != null && tile.type == targetType) {
                                positionsToClear.add(com.example.game.match3.board.BoardPosition(r, c))
                            }
                        }
                    }
                    matchResolver.triggerBoosterClear(positionsToClear)
                    cancelBooster()
                    onSuccess("All ${targetType.name} Cleared!")
                }
            } else {
                withContext(Dispatchers.Main) { onError("Failed to consume Color Clear booster.") }
            }
        }
    }

    private fun executeSwapBetweenTiles(
        r1: Int, c1: Int,
        r2: Int, c2: Int,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val booster = activeBooster ?: BoosterType.SWAP
        scope.launch {
            val removed = GameDataProvider.inventoryRepository.removeItem(booster.id, 1)
            if (removed) {
                com.example.core.event.GameEventBus.postEvent(
                    com.example.core.event.GameEvent(
                        type = com.example.core.event.GameEventType.BOOSTER_USED,
                        itemId = booster.id
                    )
                )
                withContext(Dispatchers.Main) {
                    boardModel.swapTiles(r1, c1, r2, c2)
                    cancelBooster()
                    onSuccess("Tiles swapped!")
                }
            } else {
                withContext(Dispatchers.Main) {
                    onError("Failed to consume Free Swap.")
                }
            }
        }
    }

    private fun shuffleBoardTiles() {
        val tiles = mutableListOf<Tile>()
        for (r in 0 until boardModel.rows) {
            for (c in 0 until boardModel.columns) {
                val tile = boardModel.getTile(r, c)
                if (tile != null && tile.specialType == SpecialType.NONE) {
                    tiles.add(tile)
                }
            }
        }

        tiles.shuffle()
        var index = 0

        for (r in 0 until boardModel.rows) {
            for (c in 0 until boardModel.columns) {
                val tile = boardModel.getTile(r, c)
                if (tile != null && tile.specialType == SpecialType.NONE && index < tiles.size) {
                    boardModel.setTile(r, c, tiles[index])
                    index++
                }
            }
        }
    }

    private fun hasAnyValidMatchPossibility(): Boolean {
        for (r in 0 until boardModel.rows) {
            for (c in 0 until boardModel.columns) {
                val tile = boardModel.getTile(r, c) ?: continue
                if (c + 1 < boardModel.columns) {
                    val rightTile = boardModel.getTile(r, c + 1)
                    if (rightTile != null && rightTile.type == tile.type) return true
                }
                if (r + 1 < boardModel.rows) {
                    val downTile = boardModel.getTile(r + 1, c)
                    if (downTile != null && downTile.type == tile.type) return true
                }
            }
        }
        return true
    }
}
