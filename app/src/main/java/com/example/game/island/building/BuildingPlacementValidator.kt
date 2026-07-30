package com.example.game.island.building

import com.example.game.island.model.IslandGrid

object BuildingPlacementValidator {

    fun isValidPosition(
        grid: IslandGrid,
        startRow: Int,
        startCol: Int,
        width: Int,
        height: Int
    ): Boolean {
        return grid.isAreaBuildable(startRow, startCol, width, height)
    }

    fun isAreaFree(
        grid: IslandGrid,
        startRow: Int,
        startCol: Int,
        width: Int,
        height: Int
    ): Boolean {
        for (r in startRow until (startRow + height)) {
            for (c in startCol until (startCol + width)) {
                val cell = grid.getCell(r, c) ?: return false
                if (cell.isOccupied) return false
            }
        }
        return true
    }

    fun isLandUnlocked(
        grid: IslandGrid,
        startRow: Int,
        startCol: Int,
        width: Int,
        height: Int
    ): Boolean {
        for (r in startRow until (startRow + height)) {
            for (c in startCol until (startCol + width)) {
                val cell = grid.getCell(r, c) ?: return false
                if (!cell.isUnlocked) return false
            }
        }
        return true
    }
}
