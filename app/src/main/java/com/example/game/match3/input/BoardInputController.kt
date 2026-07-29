package com.example.game.match3.input

import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.example.game.match3.BoardModel
import com.example.game.match3.tile.Tile

class BoardInputController(
    private val boardModel: BoardModel,
    private val camera: OrthographicCamera,
    private val getBoardX: () -> Float,
    private val getBoardY: () -> Float,
    private val getTileSize: () -> Float,
    private val onSwapRequested: (r1: Int, c1: Int, r2: Int, c2: Int) -> Unit
) : InputAdapter() {

    private var selectedTile: Tile? = null

    private var touchStartPos = Vector2()
    private var touchStartRow = -1
    private var touchStartCol = -1
    private var isDragging = false

    private val swipeThreshold = 25f // pixels

    var isInputLocked: Boolean = false

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        if (isInputLocked) return false

        val worldPos = camera.unproject(Vector3(screenX.toFloat(), screenY.toFloat(), 0f))
        val (row, col) = worldToBoard(worldPos.x, worldPos.y)

        if (boardModel.isValidPosition(row, col)) {
            touchStartPos.set(worldPos.x, worldPos.y)
            touchStartRow = row
            touchStartCol = col
            isDragging = true
            return true
        } else {
            clearSelection()
        }

        return false
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        if (!isDragging || isInputLocked) return false

        val worldPos = camera.unproject(Vector3(screenX.toFloat(), screenY.toFloat(), 0f))
        val dist = touchStartPos.dst(worldPos.x, worldPos.y)

        if (dist >= swipeThreshold) {
            val dx = worldPos.x - touchStartPos.x
            val dy = worldPos.y - touchStartPos.y

            val direction = when {
                Math.abs(dx) > Math.abs(dy) -> if (dx > 0) SwipeDirection.RIGHT else SwipeDirection.LEFT
                else -> if (dy > 0) SwipeDirection.UP else SwipeDirection.DOWN
            }

            var targetRow = touchStartRow
            var targetCol = touchStartCol

            when (direction) {
                SwipeDirection.UP -> targetRow++
                SwipeDirection.DOWN -> targetRow--
                SwipeDirection.LEFT -> targetCol--
                SwipeDirection.RIGHT -> targetCol++
            }

            if (boardModel.isValidPosition(targetRow, targetCol)) {
                val r1 = touchStartRow
                val c1 = touchStartCol
                clearSelection()
                onSwapRequested(r1, c1, targetRow, targetCol)
            }

            isDragging = false // Consumed swipe
            return true
        }

        return false
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        if (!isDragging) return false
        isDragging = false

        if (isInputLocked) return false

        val worldPos = camera.unproject(Vector3(screenX.toFloat(), screenY.toFloat(), 0f))
        val dist = touchStartPos.dst(worldPos.x, worldPos.y)

        // If touchUp happened within threshold, treat as TAP
        if (dist < swipeThreshold) {
            val (row, col) = worldToBoard(worldPos.x, worldPos.y)
            if (boardModel.isValidPosition(row, col)) {
                handleTileTap(row, col)
                return true
            }
        }

        return false
    }

    private fun handleTileTap(row: Int, col: Int) {
        val tappedTile = boardModel.getTile(row, col) ?: return

        val sel = selectedTile
        if (sel == null) {
            // Select this tile
            setSelectedTile(tappedTile)
        } else if (sel.row == row && sel.column == col) {
            // Deselect
            clearSelection()
        } else if (boardModel.isAdjacent(sel.row, sel.column, row, col)) {
            // Swap with adjacent tile
            val r1 = sel.row
            val c1 = sel.column
            clearSelection()
            onSwapRequested(r1, c1, row, col)
        } else {
            // Select new non-adjacent tile
            setSelectedTile(tappedTile)
        }
    }

    private fun setSelectedTile(tile: Tile) {
        clearSelection()
        selectedTile = tile
        tile.isSelected = true
    }

    fun clearSelection() {
        selectedTile?.isSelected = false
        selectedTile = null
    }

    fun worldToBoard(worldX: Float, worldY: Float): Pair<Int, Int> {
        val boardX = getBoardX()
        val boardY = getBoardY()
        val tileSize = getTileSize()

        val relX = worldX - boardX
        val relY = worldY - boardY

        val col = (relX / tileSize).toInt()
        val row = (relY / tileSize).toInt()

        return Pair(row, col)
    }
}

