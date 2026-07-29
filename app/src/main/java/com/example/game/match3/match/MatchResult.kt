package com.example.game.match3.match

import com.example.game.match3.board.BoardPosition

data class MatchResult(
    val hasMatch: Boolean,
    val groups: List<MatchGroup>,
    val matchedPositions: Set<BoardPosition>,
    val totalMatchedTiles: Int
)
