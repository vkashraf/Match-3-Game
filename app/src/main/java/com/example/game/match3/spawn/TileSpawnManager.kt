package com.example.game.match3.spawn

import com.badlogic.gdx.math.Interpolation
import com.example.game.match3.BoardModel
import com.example.game.match3.tile.Tile
import com.example.game.match3.tile.TileState
import com.example.game.match3.tile.TileType
import java.util.UUID
import kotlin.random.Random

class TileSpawnManager(private val seed: Long? = null) {

    private val random = seed?.let { Random(it) } ?: Random.Default

    private var spawnAnimTime = 0f
    private val spawnDuration = 0.25f // 250ms
    var isSpawning = false
        private set

    private val spawningTiles = mutableListOf<Tile>()

    fun spawnNewTiles(boardModel: BoardModel, tileSize: Float): Boolean {
        spawningTiles.clear()
        val types = TileType.entries.toTypedArray()
        var spawnedAny = false

        for (c in 0 until boardModel.columns) {
            var emptyCount = 0
            for (r in 0 until boardModel.rows) {
                val tile = boardModel.getTile(r, c)
                if (tile == null || tile.state == TileState.EMPTY) {
                    emptyCount++
                    val newType = types[random.nextInt(types.size)]
                    val newTile = Tile(
                        id = UUID.randomUUID().toString(),
                        row = r,
                        column = c,
                        type = newType,
                        state = TileState.SPAWNING
                    )

                    // Start offset above the board
                    val spawnHeightOffset = (boardModel.rows - r + emptyCount) * tileSize
                    newTile.renderOffsetY = spawnHeightOffset

                    boardModel.setTile(r, c, newTile)
                    spawningTiles.add(newTile)
                    spawnedAny = true
                }
            }
        }

        if (spawnedAny) {
            isSpawning = true
            spawnAnimTime = 0f
        }

        return spawnedAny
    }

    fun update(delta: Float) {
        if (!isSpawning) return

        spawnAnimTime += delta
        val progress = Math.min(1f, spawnAnimTime / spawnDuration)

        for (tile in spawningTiles) {
            tile.renderOffsetY = (1f - progress) * tile.renderOffsetY
        }

        if (progress >= 1f) {
            for (tile in spawningTiles) {
                tile.renderOffsetY = 0f
                tile.state = TileState.IDLE
            }
            spawningTiles.clear()
            isSpawning = false
        }
    }
}
