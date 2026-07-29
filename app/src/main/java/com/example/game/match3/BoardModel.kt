package com.example.game.match3

import com.example.game.match3.board.BoardCell
import com.example.game.match3.obstacle.Obstacle
import com.example.game.match3.tile.Tile

class BoardModel(val rows: Int = 8, val columns: Int = 8) {

    val cells: Array<Array<BoardCell>> = Array(rows) { r ->
        Array(columns) { c ->
            BoardCell(r, c)
        }
    }

    fun isValidPosition(row: Int, col: Int): Boolean {
        return row in 0 until rows && col in 0 until columns
    }

    fun getCell(row: Int, col: Int): BoardCell? {
        if (!isValidPosition(row, col)) return null
        return cells[row][col]
    }

    fun getTile(row: Int, col: Int): Tile? {
        return getCell(row, col)?.tile
    }

    fun setTile(row: Int, col: Int, tile: Tile?) {
        val cell = getCell(row, col) ?: return
        cell.tile = tile
        tile?.row = row
        tile?.column = col
    }

    fun getObstacle(row: Int, col: Int): Obstacle? {
        return getCell(row, col)?.obstacle
    }

    fun setObstacle(row: Int, col: Int, obstacle: Obstacle?) {
        getCell(row, col)?.obstacle = obstacle
    }

    fun swapTiles(r1: Int, c1: Int, r2: Int, c2: Int) {
        val cell1 = getCell(r1, c1) ?: return
        val cell2 = getCell(r2, c2) ?: return

        // Prevent swapping if cell is locked
        if (cell1.isLocked || cell2.isLocked) return

        val tile1 = cell1.tile
        val tile2 = cell2.tile

        cell1.tile = tile2
        cell2.tile = tile1

        tile1?.let {
            it.row = r2
            it.column = c2
        }
        tile2?.let {
            it.row = r1
            it.column = c1
        }
    }

    fun isAdjacent(r1: Int, c1: Int, r2: Int, c2: Int): Boolean {
        val rowDiff = Math.abs(r1 - r2)
        val colDiff = Math.abs(c1 - c2)
        return (rowDiff == 1 && colDiff == 0) || (rowDiff == 0 && colDiff == 1)
    }

    fun clear() {
        for (r in 0 until rows) {
            for (c in 0 until columns) {
                cells[r][c].tile = null
                cells[r][c].obstacle = null
            }
        }
    }
}

