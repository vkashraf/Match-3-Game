package com.example.game.match3.match

import com.example.game.match3.BoardModel
import com.example.game.match3.board.BoardPosition
import com.example.game.match3.tile.TileState
import com.example.game.match3.tile.TileType

class MatchDetector {

    fun findAllMatches(boardModel: BoardModel): MatchResult {
        val groups = mutableListOf<MatchGroup>()
        val matchedPositions = mutableSetOf<BoardPosition>()

        // 1. Horizontal Match Detection
        for (r in 0 until boardModel.rows) {
            var startCol = 0
            var currentType: TileType? = null
            var matchLen = 0

            for (c in 0 until boardModel.columns) {
                val tile = boardModel.getTile(r, c)
                val isValidTile = tile != null && tile.state != TileState.EMPTY && tile.state != TileState.DESTROYING
                val tileType = if (isValidTile) tile?.type else null

                if (tileType != null && tileType == currentType) {
                    matchLen++
                } else {
                    if (matchLen >= 3 && currentType != null) {
                        val posList = (startCol until startCol + matchLen).map { col -> BoardPosition(r, col) }
                        groups.add(MatchGroup(posList, currentType, MatchOrientation.HORIZONTAL))
                        matchedPositions.addAll(posList)
                    }
                    currentType = tileType
                    startCol = c
                    matchLen = if (tileType != null) 1 else 0
                }
            }
            if (matchLen >= 3 && currentType != null) {
                val posList = (startCol until startCol + matchLen).map { col -> BoardPosition(r, col) }
                groups.add(MatchGroup(posList, currentType, MatchOrientation.HORIZONTAL))
                matchedPositions.addAll(posList)
            }
        }

        // 2. Vertical Match Detection
        for (c in 0 until boardModel.columns) {
            var startRow = 0
            var currentType: TileType? = null
            var matchLen = 0

            for (r in 0 until boardModel.rows) {
                val tile = boardModel.getTile(r, c)
                val isValidTile = tile != null && tile.state != TileState.EMPTY && tile.state != TileState.DESTROYING
                val tileType = if (isValidTile) tile?.type else null

                if (tileType != null && tileType == currentType) {
                    matchLen++
                } else {
                    if (matchLen >= 3 && currentType != null) {
                        val posList = (startRow until startRow + matchLen).map { row -> BoardPosition(row, c) }
                        groups.add(MatchGroup(posList, currentType, MatchOrientation.VERTICAL))
                        matchedPositions.addAll(posList)
                    }
                    currentType = tileType
                    startRow = r
                    matchLen = if (tileType != null) 1 else 0
                }
            }
            if (matchLen >= 3 && currentType != null) {
                val posList = (startRow until startRow + matchLen).map { row -> BoardPosition(row, c) }
                groups.add(MatchGroup(posList, currentType, MatchOrientation.VERTICAL))
                matchedPositions.addAll(posList)
            }
        }

        return MatchResult(
            hasMatch = matchedPositions.isNotEmpty(),
            groups = groups,
            matchedPositions = matchedPositions,
            totalMatchedTiles = matchedPositions.size
        )
    }
}
