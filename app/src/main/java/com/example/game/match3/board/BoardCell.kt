package com.example.game.match3.board

import com.example.game.match3.obstacle.Obstacle
import com.example.game.match3.tile.Tile

data class BoardCell(
    val row: Int,
    val col: Int,
    var tile: Tile? = null,
    var obstacle: Obstacle? = null,
    var isBlocked: Boolean = false,
    var portalId: String? = null,
    var jellyLayers: Int = 0
) {
    val isLocked: Boolean get() = isBlocked || obstacle?.blocksMovement == true
    val isEmpty: Boolean get() = tile == null
}
