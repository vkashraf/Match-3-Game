package com.example.game.match3.gravity

import com.badlogic.gdx.math.Interpolation
import com.example.game.match3.BoardModel
import com.example.game.match3.tile.Tile
import com.example.game.match3.tile.TileState

class GravityManager {

    private var fallAnimTime = 0f
    private val fallDuration = 0.25f // 250ms
    var isFalling = false
        private set

    private val fallingTiles = mutableListOf<Tile>()

    fun applyGravity(boardModel: BoardModel, tileSize: Float): Boolean {
        fallingTiles.clear()

        var anyFell = false

        for (c in 0 until boardModel.columns) {
            val nonNullTiles = mutableListOf<Tile>()

            // Collect non-empty tiles bottom-to-top
            for (r in 0 until boardModel.rows) {
                val tile = boardModel.getTile(r, c)
                if (tile != null && tile.state != TileState.EMPTY) {
                    nonNullTiles.add(tile)
                }
            }

            // Clear column in board model
            for (r in 0 until boardModel.rows) {
                boardModel.setTile(r, c, null)
            }

            // Place collected tiles starting at row 0 upwards
            for (newRow in nonNullTiles.indices) {
                val tile = nonNullTiles[newRow]
                val oldRow = tile.row

                boardModel.setTile(newRow, c, tile)

                if (oldRow != newRow) {
                    anyFell = true
                    tile.state = TileState.FALLING
                    tile.renderOffsetY = (oldRow - newRow) * tileSize
                    fallingTiles.add(tile)
                } else {
                    tile.state = TileState.IDLE
                    tile.renderOffsetY = 0f
                }
            }
        }

        if (anyFell) {
            isFalling = true
            fallAnimTime = 0f
        }

        return anyFell
    }

    fun update(delta: Float) {
        if (!isFalling) return

        fallAnimTime += delta
        val progress = Math.min(1f, fallAnimTime / fallDuration)
        val alpha = Interpolation.bounceOut.apply(progress)

        for (tile in fallingTiles) {
            // Animate renderOffsetY towards 0
            tile.renderOffsetY = (1f - progress) * (tile.renderOffsetY)
        }

        if (progress >= 1f) {
            for (tile in fallingTiles) {
                tile.renderOffsetY = 0f
                tile.state = TileState.IDLE
            }
            fallingTiles.clear()
            isFalling = false
        }
    }
}
