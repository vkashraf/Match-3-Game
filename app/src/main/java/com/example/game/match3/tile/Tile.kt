package com.example.game.match3.tile

import com.example.game.match3.special.SpecialType

data class Tile(
    val id: String,
    var row: Int,
    var column: Int,
    var type: TileType,
    var specialType: SpecialType = SpecialType.NONE,
    var state: TileState = TileState.IDLE,
    var isSelected: Boolean = false,
    var renderOffsetX: Float = 0f,
    var renderOffsetY: Float = 0f,
    var scale: Float = 1f,
    var alpha: Float = 1f
)

