package com.example.game.match3.special

import com.example.game.match3.BoardModel
import com.example.game.match3.board.BoardPosition
import com.example.game.match3.match.MatchGroup
import com.example.game.match3.match.MatchOrientation
import com.example.game.match3.tile.Tile
import com.example.game.match3.tile.TileState
import com.example.game.match3.tile.TileType
import kotlin.random.Random

data class SpecialCreationInfo(
    val spawnPosition: BoardPosition,
    val specialType: SpecialType,
    val baseType: TileType
)

class SpecialComboResolver {

    /**
     * Determines if any match groups should produce a special tile (Rocket, Bomb, Rainbow).
     */
    fun detectSpecialCreations(
        groups: List<MatchGroup>,
        swapPos: BoardPosition?
    ): List<SpecialCreationInfo> {
        val creations = mutableListOf<SpecialCreationInfo>()

        // Check L or T shapes (intersections of HORIZONTAL & VERTICAL groups of same tileType)
        val hGroups = groups.filter { it.orientation == MatchOrientation.HORIZONTAL }
        val vGroups = groups.filter { it.orientation == MatchOrientation.VERTICAL }

        val usedGroups = mutableSetOf<MatchGroup>()

        for (hg in hGroups) {
            for (vg in vGroups) {
                if (hg.tileType == vg.tileType) {
                    val intersection = hg.positions.firstOrNull { it in vg.positions }
                    if (intersection != null) {
                        creations.add(SpecialCreationInfo(intersection, SpecialType.BOMB, hg.tileType))
                        usedGroups.add(hg)
                        usedGroups.add(vg)
                    }
                }
            }
        }

        // Process remaining linear groups
        for (g in groups) {
            if (g in usedGroups) continue

            val spawnPos = if (swapPos != null && swapPos in g.positions) {
                swapPos
            } else {
                g.positions[g.positions.size / 2]
            }

            if (g.size >= 5) {
                creations.add(SpecialCreationInfo(spawnPos, SpecialType.RAINBOW, g.tileType))
            } else if (g.size == 4) {
                val specType = if (g.orientation == MatchOrientation.HORIZONTAL) {
                    SpecialType.ROCKET_HORIZONTAL
                } else {
                    SpecialType.ROCKET_VERTICAL
                }
                creations.add(SpecialCreationInfo(spawnPos, specType, g.tileType))
            }
        }

        return creations
    }

    /**
     * Expands set of matched positions by triggering special tile effects and handling special-special swaps.
     */
    fun expandSpecialActivations(
        boardModel: BoardModel,
        initialPositions: Set<BoardPosition>,
        isSpecialSwap: Boolean,
        swapPos1: BoardPosition?,
        swapPos2: BoardPosition?
    ): Pair<Set<BoardPosition>, Int> {

        val finalCleared = mutableSetOf<BoardPosition>()
        val activatedSpecials = mutableSetOf<BoardPosition>()
        val queue = mutableListOf<BoardPosition>()
        var scoreBonus = 0

        // Handle direct Special + Special swap
        if (isSpecialSwap && swapPos1 != null && swapPos2 != null) {
            val t1 = boardModel.getTile(swapPos1.row, swapPos1.col)
            val t2 = boardModel.getTile(swapPos2.row, swapPos2.col)

            if (t1 != null && t2 != null) {
                val s1 = t1.specialType
                val s2 = t2.specialType

                val centerPos = swapPos2

                if (isRainbow(s1) && isRainbow(s2)) {
                    // RAINBOW + RAINBOW -> Clear entire board!
                    for (r in 0 until boardModel.rows) {
                        for (c in 0 until boardModel.columns) {
                            finalCleared.add(BoardPosition(r, c))
                        }
                    }
                    scoreBonus += 1000
                } else if ((isRainbow(s1) && isRocket(s2)) || (isRainbow(s2) && isRocket(s1))) {
                    // RAINBOW + ROCKET -> Transform all of rocket's base color into rockets and trigger
                    val rocketTile = if (isRocket(s1)) t1 else t2
                    val targetType = rocketTile.type

                    for (r in 0 until boardModel.rows) {
                        for (c in 0 until boardModel.columns) {
                            val tile = boardModel.getTile(r, c)
                            if (tile != null && tile.type == targetType) {
                                tile.specialType = if (Random.nextBoolean()) SpecialType.ROCKET_HORIZONTAL else SpecialType.ROCKET_VERTICAL
                                queue.add(BoardPosition(r, c))
                            }
                        }
                    }
                    scoreBonus += 600
                } else if ((isRainbow(s1) && s2 == SpecialType.BOMB) || (isRainbow(s2) && s1 == SpecialType.BOMB)) {
                    // RAINBOW + BOMB -> Transform all of bomb's base color into bombs and trigger
                    val bombTile = if (s1 == SpecialType.BOMB) t1 else t2
                    val targetType = bombTile.type

                    for (r in 0 until boardModel.rows) {
                        for (c in 0 until boardModel.columns) {
                            val tile = boardModel.getTile(r, c)
                            if (tile != null && tile.type == targetType) {
                                tile.specialType = SpecialType.BOMB
                                queue.add(BoardPosition(r, c))
                            }
                        }
                    }
                    scoreBonus += 600
                } else if (isRainbow(s1) || isRainbow(s2)) {
                    // RAINBOW + NORMAL
                    val normalTile = if (!isRainbow(s1)) t1 else t2
                    val targetType = normalTile.type

                    for (r in 0 until boardModel.rows) {
                        for (c in 0 until boardModel.columns) {
                            val tile = boardModel.getTile(r, c)
                            if (tile != null && tile.type == targetType) {
                                finalCleared.add(BoardPosition(r, c))
                            }
                        }
                    }
                    finalCleared.add(swapPos1)
                    finalCleared.add(swapPos2)
                    scoreBonus += 300
                } else if (isRocket(s1) && isRocket(s2)) {
                    // ROCKET + ROCKET -> Row + Column
                    addRow(boardModel, centerPos.row, finalCleared)
                    addCol(boardModel, centerPos.col, finalCleared)
                    scoreBonus += 300
                } else if ((isRocket(s1) && s2 == SpecialType.BOMB) || (isRocket(s2) && s1 == SpecialType.BOMB)) {
                    // ROCKET + BOMB -> 3 Rows + 3 Columns
                    for (r in (centerPos.row - 1)..(centerPos.row + 1)) addRow(boardModel, r, finalCleared)
                    for (c in (centerPos.col - 1)..(centerPos.col + 1)) addCol(boardModel, c, finalCleared)
                    scoreBonus += 400
                } else if (s1 == SpecialType.BOMB && s2 == SpecialType.BOMB) {
                    // BOMB + BOMB -> 5x5 Area
                    addArea(boardModel, centerPos.row, centerPos.col, 2, finalCleared)
                    scoreBonus += 500
                }
            }
        } else {
            // Standard matches
            finalCleared.addAll(initialPositions)
            queue.addAll(initialPositions)
        }

        // Process activation queue
        while (queue.isNotEmpty()) {
            val pos = queue.removeAt(0)
            if (pos in activatedSpecials) continue

            val tile = boardModel.getTile(pos.row, pos.col) ?: continue
            if (tile.specialType == SpecialType.NONE) continue

            activatedSpecials.add(pos)

            when (tile.specialType) {
                SpecialType.ROCKET_HORIZONTAL, SpecialType.LINE_HORIZONTAL -> {
                    addRow(boardModel, pos.row, finalCleared)
                    for (c in 0 until boardModel.columns) {
                        val p = BoardPosition(pos.row, c)
                        if (p !in activatedSpecials && boardModel.getTile(pos.row, c)?.specialType != SpecialType.NONE) {
                            queue.add(p)
                        }
                    }
                    scoreBonus += 100
                }
                SpecialType.ROCKET_VERTICAL, SpecialType.LINE_VERTICAL -> {
                    addCol(boardModel, pos.col, finalCleared)
                    for (r in 0 until boardModel.rows) {
                        val p = BoardPosition(r, pos.col)
                        if (p !in activatedSpecials && boardModel.getTile(r, pos.col)?.specialType != SpecialType.NONE) {
                            queue.add(p)
                        }
                    }
                    scoreBonus += 100
                }
                SpecialType.BOMB -> {
                    val newlyCleared = mutableSetOf<BoardPosition>()
                    addArea(boardModel, pos.row, pos.col, 1, newlyCleared)
                    finalCleared.addAll(newlyCleared)
                    for (p in newlyCleared) {
                        if (p !in activatedSpecials && boardModel.getTile(p.row, p.col)?.specialType != SpecialType.NONE) {
                            queue.add(p)
                        }
                    }
                    scoreBonus += 150
                }
                SpecialType.RAINBOW, SpecialType.COLOR_CLEAR, SpecialType.CUSTOM_SPECIAL -> {
                    // Clear all tiles of same color as a target tile
                    val targetType = tile.type
                    for (r in 0 until boardModel.rows) {
                        for (c in 0 until boardModel.columns) {
                            val t = boardModel.getTile(r, c)
                            if (t != null && t.type == targetType) {
                                finalCleared.add(BoardPosition(r, c))
                            }
                        }
                    }
                    scoreBonus += 200
                }
                SpecialType.NONE, SpecialType.ROCKET -> {}
            }
        }

        return Pair(finalCleared, scoreBonus)
    }

    private fun isRocket(type: SpecialType): Boolean {
        return type == SpecialType.ROCKET_HORIZONTAL || type == SpecialType.ROCKET_VERTICAL ||
                type == SpecialType.LINE_HORIZONTAL || type == SpecialType.LINE_VERTICAL ||
                type == SpecialType.ROCKET
    }

    private fun isRainbow(type: SpecialType): Boolean {
        return type == SpecialType.RAINBOW || type == SpecialType.COLOR_CLEAR || type == SpecialType.CUSTOM_SPECIAL
    }

    private fun addRow(boardModel: BoardModel, r: Int, set: MutableSet<BoardPosition>) {
        if (r in 0 until boardModel.rows) {
            for (c in 0 until boardModel.columns) set.add(BoardPosition(r, c))
        }
    }

    private fun addCol(boardModel: BoardModel, c: Int, set: MutableSet<BoardPosition>) {
        if (c in 0 until boardModel.columns) {
            for (r in 0 until boardModel.rows) set.add(BoardPosition(r, c))
        }
    }

    private fun addArea(boardModel: BoardModel, centerR: Int, centerC: Int, radius: Int, set: MutableSet<BoardPosition>) {
        for (r in (centerR - radius)..(centerR + radius)) {
            for (c in (centerC - radius)..(centerC + radius)) {
                if (boardModel.isValidPosition(r, c)) {
                    set.add(BoardPosition(r, c))
                }
            }
        }
    }
}
