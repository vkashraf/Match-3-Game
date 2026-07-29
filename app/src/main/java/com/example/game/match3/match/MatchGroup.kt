package com.example.game.match3.match

import com.example.game.match3.board.BoardPosition
import com.example.game.match3.tile.TileType

data class MatchGroup(
    val positions: List<BoardPosition>,
    val tileType: TileType,
    val orientation: MatchOrientation,
    val size: Int = positions.size
)
