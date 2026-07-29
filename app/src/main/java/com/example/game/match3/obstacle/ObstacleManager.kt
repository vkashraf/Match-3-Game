package com.example.game.match3.obstacle

import com.example.game.match3.BoardModel
import com.example.game.match3.board.BoardPosition

class ObstacleManager {

    fun damageObstacle(boardModel: BoardModel, row: Int, col: Int, amount: Int = 1): ObstacleType? {
        val obstacle = boardModel.getObstacle(row, col) ?: return null
        obstacle.health -= amount
        if (obstacle.health <= 0) {
            val destroyedType = obstacle.type
            boardModel.setObstacle(row, col, null)
            return destroyedType
        }
        return null
    }

    fun damageAdjacentObstacles(boardModel: BoardModel, positions: Set<BoardPosition>): List<Pair<BoardPosition, ObstacleType>> {
        val destroyed = mutableListOf<Pair<BoardPosition, ObstacleType>>()
        val adjacentDirs = arrayOf(Pair(1, 0), Pair(-1, 0), Pair(0, 1), Pair(0, -1))

        val checkedPositions = mutableSetOf<BoardPosition>()

        for (pos in positions) {
            for (dir in adjacentDirs) {
                val nr = pos.row + dir.first
                val nc = pos.col + dir.second
                val adjPos = BoardPosition(nr, nc)

                if (boardModel.isValidPosition(nr, nc) && !positions.contains(adjPos) && !checkedPositions.contains(adjPos)) {
                    checkedPositions.add(adjPos)
                    val destroyedType = damageObstacle(boardModel, nr, nc, 1)
                    if (destroyedType != null) {
                        destroyed.add(Pair(adjPos, destroyedType))
                    }
                }
            }
        }
        return destroyed
    }
}
