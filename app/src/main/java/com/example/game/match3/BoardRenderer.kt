package com.example.game.match3

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.example.core.GameConstants
import com.example.game.match3.score.FloatingText
import com.example.game.match3.tile.TileState
import com.example.utils.TextureFactory

class BoardRenderer(private val boardModel: BoardModel) {

    private val font: BitmapFont = BitmapFont().apply { data.setScale(1.2f) }

    fun render(
        batch: Batch,
        boardX: Float,
        boardY: Float,
        boardWidth: Float,
        boardHeight: Float,
        tileSize: Float,
        floatingTexts: List<FloatingText> = emptyList(),
        pad: Float = 4f
    ) {
        // 1. Draw Board Background Panel
        val panelDrawable = TextureFactory.createRoundedPanel(
            width = boardWidth.toInt(),
            height = boardHeight.toInt(),
            fillColor = Color(0.1f, 0.16f, 0.28f, 0.95f),
            borderColor = GameConstants.COLOR_GOLD,
            borderThickness = 4,
            cornerRadius = 16
        )
        panelDrawable.draw(batch, boardX, boardY, boardWidth, boardHeight)

        // 2. Draw 8x8 Cell Grid Background Slots
        val slotDrawable = TextureFactory.createRoundedPanel(
            width = (tileSize - pad * 2).toInt(),
            height = (tileSize - pad * 2).toInt(),
            fillColor = Color(0.06f, 0.1f, 0.18f, 0.7f),
            borderColor = Color(0.18f, 0.28f, 0.42f, 0.5f),
            borderThickness = 2,
            cornerRadius = 8
        )

        val gridOriginX = boardX + (boardWidth - (boardModel.columns * tileSize)) / 2f
        val gridOriginY = boardY + (boardHeight - (boardModel.rows * tileSize)) / 2f

        for (r in 0 until boardModel.rows) {
            for (c in 0 until boardModel.columns) {
                val cellX = gridOriginX + c * tileSize + pad
                val cellY = gridOriginY + r * tileSize + pad
                val cellW = tileSize - pad * 2
                val cellH = tileSize - pad * 2
                slotDrawable.draw(batch, cellX, cellY, cellW, cellH)
            }
        }

        // 3. Draw Tiles
        val highlightCircle = TextureFactory.createCircleTexture(
            size = tileSize.toInt(),
            fillColor = Color(1f, 0.95f, 0.4f, 0.4f),
            borderColor = GameConstants.COLOR_GOLD
        )

        for (r in 0 until boardModel.rows) {
            for (c in 0 until boardModel.columns) {
                val tile = boardModel.getTile(r, c) ?: continue
                if (tile.state == TileState.EMPTY) continue

                val baseTileX = gridOriginX + c * tileSize + pad
                val baseTileY = gridOriginY + r * tileSize + pad

                val renderX = baseTileX + tile.renderOffsetX
                val renderY = baseTileY + tile.renderOffsetY

                val drawSize = tileSize - pad * 2

                val tileDrawable = TextureFactory.createTileTexture(
                    type = tile.type.name,
                    size = drawSize.toInt()
                )

                // Render selection glow/scale
                if (tile.isSelected) {
                    val glowSize = drawSize * 1.15f
                    val glowOffset = (glowSize - drawSize) / 2f
                    highlightCircle.draw(
                        batch,
                        renderX - glowOffset,
                        renderY - glowOffset,
                        glowSize,
                        glowSize
                    )
                }

                val scale = if (tile.isSelected) 1.1f else tile.scale
                val scaledSize = drawSize * scale
                val offset = (scaledSize - drawSize) / 2f

                if (tile.alpha < 1f) {
                    batch.setColor(1f, 1f, 1f, tile.alpha)
                }

                tileDrawable.draw(
                    batch,
                    renderX - offset,
                    renderY - offset,
                    scaledSize,
                    scaledSize
                )

                // Render special overlay (Rocket, Bomb, Rainbow)
                if (tile.specialType != com.example.game.match3.special.SpecialType.NONE) {
                    val specialOverlay = TextureFactory.createSpecialOverlayTexture(
                        specialTypeStr = tile.specialType.name,
                        size = scaledSize.toInt()
                    )
                    specialOverlay.draw(
                        batch,
                        renderX - offset,
                        renderY - offset,
                        scaledSize,
                        scaledSize
                    )
                }

                if (tile.alpha < 1f) {
                    batch.setColor(1f, 1f, 1f, 1f)
                }
            }
        }

        // 3.5 Render Obstacles
        for (r in 0 until boardModel.rows) {
            for (c in 0 until boardModel.columns) {
                val obstacle = boardModel.getObstacle(r, c) ?: continue
                val baseCellX = gridOriginX + c * tileSize + pad
                val baseCellY = gridOriginY + r * tileSize + pad
                val drawSize = tileSize - pad * 2

                val obstacleDrawable = TextureFactory.createObstacleTexture(
                    typeStr = obstacle.type.name,
                    size = drawSize.toInt()
                )
                obstacleDrawable.draw(batch, baseCellX, baseCellY, drawSize, drawSize)
            }
        }

        // 4. Render Floating Texts
        for (text in floatingTexts) {
            font.color = Color(1f, 0.95f, 0.3f, text.alpha)
            font.draw(batch, text.text, text.x, text.y)
        }
        font.color = Color.WHITE
    }
}

