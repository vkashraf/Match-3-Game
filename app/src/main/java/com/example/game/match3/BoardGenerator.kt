package com.example.game.match3

import com.example.game.match3.tile.Tile
import com.example.game.match3.tile.TileType
import java.util.UUID
import kotlin.random.Random

class BoardGenerator(private val seed: Long? = null) {

    private val random = seed?.let { Random(it) } ?: Random.Default

    fun generateInitialBoard(boardModel: BoardModel) {
        boardModel.clear()
        val types = TileType.entries.toTypedArray()

        for (r in 0 until boardModel.rows) {
            for (c in 0 until boardModel.columns) {
                var candidateType: TileType
                do {
                    candidateType = types[random.nextInt(types.size)]
                } while (createsMatchAt(boardModel, r, c, candidateType))

                val tile = Tile(
                    id = UUID.randomUUID().toString(),
                    row = r,
                    column = c,
                    type = candidateType
                )
                boardModel.setTile(r, c, tile)
            }
        }
    }

    private fun createsMatchAt(boardModel: BoardModel, row: Int, col: Int, type: TileType): Boolean {
        // Check horizontal left (2 tiles left)
        if (col >= 2) {
            val left1 = boardModel.getTile(row, col - 1)?.type
            val left2 = boardModel.getTile(row, col - 2)?.type
            if (left1 == type && left2 == type) return true
        }

        // Check vertical below (2 tiles down, where row 0 is bottom)
        if (row >= 2) {
            val down1 = boardModel.getTile(row - 1, col)?.type
            val down2 = boardModel.getTile(row - 2, col)?.type
            if (down1 == type && down2 == type) return true
        }

        return false
    }
}
