package com.example.game.match3.move

import com.example.game.match3.BoardModel
import com.example.game.match3.match.MatchDetector

class MoveValidator(private val matchDetector: MatchDetector = MatchDetector()) {

    fun hasAnyValidMove(boardModel: BoardModel): Boolean {
        for (r in 0 until boardModel.rows) {
            for (c in 0 until boardModel.columns) {
                // Check right swap
                if (c + 1 < boardModel.columns) {
                    boardModel.swapTiles(r, c, r, c + 1)
                    val hasMatch = matchDetector.findAllMatches(boardModel).hasMatch
                    boardModel.swapTiles(r, c, r, c + 1) // Swap back
                    if (hasMatch) return true
                }

                // Check up swap
                if (r + 1 < boardModel.rows) {
                    boardModel.swapTiles(r, c, r + 1, c)
                    val hasMatch = matchDetector.findAllMatches(boardModel).hasMatch
                    boardModel.swapTiles(r, c, r + 1, c) // Swap back
                    if (hasMatch) return true
                }
            }
        }
        return false
    }
}
