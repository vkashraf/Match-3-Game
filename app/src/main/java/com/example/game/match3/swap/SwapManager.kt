package com.example.game.match3.swap

import com.badlogic.gdx.math.Interpolation
import com.example.game.match3.BoardModel
import com.example.game.match3.tile.Tile
import com.example.game.match3.tile.TileState

class SwapManager(private val boardModel: BoardModel) {

    private var activeTile1: Tile? = null
    private var activeTile2: Tile? = null

    private var startRow1 = 0
    private var startCol1 = 0
    private var startRow2 = 0
    private var startCol2 = 0

    private var animProgress = 0f
    private val swapDuration = 0.2f // 200 milliseconds

    var isSwapping: Boolean = false
        private set

    fun startSwap(
        r1: Int, c1: Int,
        r2: Int, c2: Int,
        onComplete: (() -> Unit)? = null
    ) {
        val t1 = boardModel.getTile(r1, c1) ?: return
        val t2 = boardModel.getTile(r2, c2) ?: return

        activeTile1 = t1
        activeTile2 = t2
        startRow1 = r1
        startCol1 = c1
        startRow2 = r2
        startCol2 = c2

        t1.state = TileState.SWAPPING
        t2.state = TileState.SWAPPING

        // Perform swap in data model immediately
        boardModel.swapTiles(r1, c1, r2, c2)

        animProgress = 0f
        isSwapping = true
        this.onCompleteCallback = onComplete
    }

    private var onCompleteCallback: (() -> Unit)? = null

    fun update(delta: Float, tileSize: Float) {
        if (!isSwapping) return

        animProgress += delta / swapDuration
        val alpha = Interpolation.smooth.apply(Math.min(1f, animProgress))

        val t1 = activeTile1
        val t2 = activeTile2

        if (t1 != null && t2 != null) {
            val dRow = (startRow1 - startRow2) * tileSize
            val dCol = (startCol1 - startCol2) * tileSize

            // t1 moved from (r1, c1) to (r2, c2), so render offset starts at -dCol, -dRow -> 0, 0
            t1.renderOffsetX = (1f - alpha) * (startCol1 - startCol2) * tileSize
            t1.renderOffsetY = (1f - alpha) * (startRow1 - startRow2) * tileSize

            t2.renderOffsetX = (1f - alpha) * (startCol2 - startCol1) * tileSize
            t2.renderOffsetY = (1f - alpha) * (startRow2 - startRow1) * tileSize
        }

        if (animProgress >= 1f) {
            t1?.renderOffsetX = 0f
            t1?.renderOffsetY = 0f
            t1?.state = TileState.IDLE

            t2?.renderOffsetX = 0f
            t2?.renderOffsetY = 0f
            t2?.state = TileState.IDLE

            isSwapping = false
            activeTile1 = null
            activeTile2 = null

            val callback = onCompleteCallback
            onCompleteCallback = null
            callback?.invoke()
        }
    }
}
