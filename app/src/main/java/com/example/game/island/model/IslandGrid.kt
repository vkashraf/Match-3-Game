package com.example.game.island.model

class IslandGrid(val rows: Int = 20, val columns: Int = 20) {

    private val cells: Array<Array<IslandCell>> = Array(rows) { r ->
        Array(columns) { c ->
            IslandCell(row = r, column = c)
        }
    }

    fun getCell(row: Int, col: Int): IslandCell? {
        if (row !in 0 until rows || col !in 0 until columns) return null
        return cells[row][col]
    }

    fun setTerrain(row: Int, col: Int, terrainType: TerrainType) {
        getCell(row, col)?.let { cell ->
            cell.terrainType = terrainType
            cell.isBuildable = terrainType.isBuildable
        }
    }

    fun isAreaBuildable(startRow: Int, startCol: Int, width: Int, height: Int): Boolean {
        for (r in startRow until (startRow + height)) {
            for (c in startCol until (startCol + width)) {
                val cell = getCell(r, c) ?: return false
                if (!cell.isUnlocked || cell.isOccupied || !cell.isBuildable) {
                    return false
                }
            }
        }
        return true
    }

    fun occupyArea(startRow: Int, startCol: Int, width: Int, height: Int, buildingId: String?, decorationId: String?) {
        for (r in startRow until (startRow + height)) {
            for (c in startCol until (startCol + width)) {
                getCell(r, c)?.let { cell ->
                    cell.isOccupied = (buildingId != null || decorationId != null)
                    cell.buildingId = buildingId
                    cell.decorationId = decorationId
                }
            }
        }
    }

    fun clearArea(startRow: Int, startCol: Int, width: Int, height: Int) {
        occupyArea(startRow, startCol, width, height, null, null)
    }

    fun unlockArea(startRow: Int, startCol: Int, width: Int, height: Int) {
        for (r in startRow until (startRow + height)) {
            for (c in startCol until (startCol + width)) {
                getCell(r, c)?.let { cell ->
                    cell.isUnlocked = true
                }
            }
        }
    }
}
