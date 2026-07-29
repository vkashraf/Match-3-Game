package com.example.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.example.core.GameConstants
import com.example.utils.TextureFactory

class ResourceCounter(
    iconType: String,
    valueText: String,
    labelStyle: Label.LabelStyle
) : Table() {

    private val label = Label(valueText, labelStyle)

    init {
        background = TextureFactory.createRoundedPanel(
            width = 130,
            height = 42,
            fillColor = GameConstants.COLOR_HUD_NAVY,
            borderColor = Color(0.2f, 0.4f, 0.65f, 1f),
            borderThickness = 3
        )
        padLeft(4f).padRight(10f)

        val iconDrawable = TextureFactory.createIcon(iconType, size = 36)
        val iconImage = Image(iconDrawable)

        add(iconImage).size(32f).padRight(6f)
        add(label)
    }

    fun setValue(newText: String) {
        label.setText(newText)
    }
}
