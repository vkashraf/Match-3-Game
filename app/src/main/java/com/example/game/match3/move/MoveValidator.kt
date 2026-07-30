package com.example.game.match3.move

import com.example.game.match3.BoardModel
import com.example.game.match3.match.MatchDetector
import com.example.game.match3.special.SpecialType

class MoveValidator(private val matchDetector: MatchDetector = MatchDetector()) {

    fun hasAnyValidMove(boardModel: BoardModel): Boolean {
        for (r in 0 until boardModel.rows) {
            for (c in 0 until boardModel.columns) {
                val t1 = boardModel.getTile(r, c) ?: continue

                // Check right swap
                if (c + 1 < boardModel.columns) {
                    val t2 = boardModel.getTile(r, c + 1)
                    if (t2 != null) {
                        // Check if special combo move
                        if (t1.specialType == SpecialType.RAINBOW || t2.specialType == SpecialType.RAINBOW ||
                            (t1.specialType != SpecialType.NONE && t2.specialType != SpecialType.NONE)) {
                            return true
                        }

                        boardModel.swapTiles(r, c, r, c + 1)
                        val hasMatch = matchDetector.findAllMatches(boardModel).hasMatch
                        boardModel.swapTiles(r, c, r, c + 1) // Swap back
                        if (hasMatch) return true
                    }
                }

                // Check up swap
                if (r + 1 < boardModel.rows) {
                    val t2 = boardModel.getTile(r + 1, c)
                    if (t2 != null) {
                        // Check if special combo move
                        if (t1.specialType == SpecialType.RAINBOW || t2.specialType == SpecialType.RAINBOW ||
                            (t1.specialType != SpecialType.NONE && t2.specialType != SpecialType.NONE)) {
                            return true
                        }

                        boardModel.swapTiles(r, c, r + 1, c)
                        val hasMatch = matchDetector.findAllMatches(boardModel).hasMatch
                        boardModel.swapTiles(r, c, r + 1, c) // Swap back
                        if (hasMatch) return true
                    }
                }
            }
        }
        return false
    }
}
