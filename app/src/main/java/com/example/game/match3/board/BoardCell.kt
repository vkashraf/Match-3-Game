package com.example.game.match3.board

import com.example.game.match3.obstacle.Obstacle
import com.example.game.match3.tile.Tile

data class BoardCell(
    val row: Int,
    val col: Int,
    var tile: Tile? = null,
    var obstacle: Obstacle? = null
) {
    val isLocked: Boolean get() = obstacle?.blocksMovement == true
}
