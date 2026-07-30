package com.example.game.island.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.example.core.GameConstants
import com.example.ui.GameButton
import com.example.utils.TextureFactory

class EditModePanel(
    private val stage: Stage,
    private val font: BitmapFont,
    private val onRotateSelected: () -> Unit,
    private val onRemoveSelected: () -> Unit,
    private val onDone: () -> Unit
) {

    private val containerTable = Table()

    init {
        setupUI()
    }

    private fun setupUI() {
        containerTable.clear()
        containerTable.bottom().padBottom(30f)
        containerTable.setFillParent(true)

        val cardTable = Table()
        cardTable.background = TextureFactory.createRoundedPanel(
            width = 440, height = 80,
            fillColor = Color(0.12f, 0.16f, 0.24f, 0.92f),
            borderColor = GameConstants.COLOR_GOLD,
            borderThickness = 3,
            cornerRadius = 16
        )
        cardTable.pad(10f)

        val btnFont = BitmapFont()
        btnFont.data.setScale(0.95f)
        val btnStyle = Label.LabelStyle(btnFont, Color.WHITE)

        val rotateBtn = GameButton(
            text = "ROTATE 🔄",
            bgColor = GameConstants.COLOR_GOLD,
            labelStyle = btnStyle,
            onClick = { onRotateSelected() }
        )

        val removeBtn = GameButton(
            text = "REMOVE 🗑️",
            bgColor = Color(0.85f, 0.25f, 0.25f, 1f),
            labelStyle = btnStyle,
            onClick = { onRemoveSelected() }
        )

        val doneBtn = GameButton(
            text = "DONE ✅",
            bgColor = GameConstants.COLOR_ISLAND_GREEN,
            labelStyle = btnStyle,
            onClick = {
                hide()
                onDone()
            }
        )

        cardTable.add(rotateBtn).size(110f, 44f).padRight(10f)
        cardTable.add(removeBtn).size(110f, 44f).padRight(10f)
        cardTable.add(doneBtn).size(110f, 44f)

        containerTable.add(cardTable)
        stage.addActor(containerTable)
    }

    fun hide() {
        containerTable.remove()
    }
}
