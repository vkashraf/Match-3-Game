package com.example.game.match3.shuffle

import com.badlogic.gdx.math.Interpolation
import com.example.game.match3.BoardGenerator
import com.example.game.match3.BoardModel
import com.example.game.match3.match.MatchDetector
import com.example.game.match3.move.MoveValidator
import com.example.game.match3.tile.TileState
import com.example.game.match3.tile.TileType
import kotlin.random.Random

class BoardShuffleManager(
    private val matchDetector: MatchDetector = MatchDetector(),
    private val moveValidator: MoveValidator = MoveValidator(),
    private val boardGenerator: BoardGenerator = BoardGenerator()
) {

    private var shuffleAnimTime = 0f
    private val shuffleDuration = 0.4f // 400ms
    var isShuffling = false
        private set

    fun shuffleBoard(boardModel: BoardModel) {
        val allTypes = mutableListOf<TileType>()
        for (r in 0 until boardModel.rows) {
            for (c in 0 until boardModel.columns) {
                val tile = boardModel.getTile(r, c)
                if (tile != null && tile.state != TileState.EMPTY) {
                    allTypes.add(tile.type)
                }
            }
        }

        var attempts = 0
        var success = false

        while (attempts < 100) {
            attempts++
            allTypes.shuffle()

            var idx = 0
            for (r in 0 until boardModel.rows) {
                for (c in 0 until boardModel.columns) {
                    val tile = boardModel.getTile(r, c)
                    if (tile != null && tile.state != TileState.EMPTY) {
                        tile.type = allTypes[idx++]
                    }
                }
            }

            val hasMatches = matchDetector.findAllMatches(boardModel).hasMatch
            val hasMoves = moveValidator.hasAnyValidMove(boardModel)

            if (!hasMatches && hasMoves) {
                success = true
                break
            }
        }

        if (!success) {
            // Fallback: regenerate fresh valid board
            boardGenerator.generateInitialBoard(boardModel)
        }

        // Start shuffle animation
        isShuffling = true
        shuffleAnimTime = 0f
    }

    fun update(delta: Float, boardModel: BoardModel) {
        if (!isShuffling) return

        shuffleAnimTime += delta
        val progress = Math.min(1f, shuffleAnimTime / shuffleDuration)

        // Scale effect: shrink to 0 then pop back to 1
        val scale = if (progress < 0.5f) {
            1f - (progress / 0.5f)
        } else {
            (progress - 0.5f) / 0.5f
        }

        for (r in 0 until boardModel.rows) {
            for (c in 0 until boardModel.columns) {
                val tile = boardModel.getTile(r, c)
                if (tile != null) {
                    tile.scale = scale
                }
            }
        }

        if (progress >= 1f) {
            for (r in 0 until boardModel.rows) {
                for (c in 0 until boardModel.columns) {
                    boardModel.getTile(r, c)?.scale = 1f
                }
            }
            isShuffling = false
        }
    }
}
