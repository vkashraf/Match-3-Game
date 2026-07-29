package com.example.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.example.core.GameConstants
import com.example.utils.TextureFactory

class BuildingLabel(
    buildingName: String,
    levelText: String,
    nameStyle: Label.LabelStyle,
    levelStyle: Label.LabelStyle
) : Table() {

    init {
        background = TextureFactory.createRoundedPanel(
            width = 110,
            height = 36,
            fillColor = GameConstants.COLOR_WOOD_BEIGE,
            borderColor = Color(0.6f, 0.45f, 0.25f, 1f),
            borderThickness = 2
        )
        pad(2f, 6f, 2f, 6f)

        val nameLabel = Label(buildingName, nameStyle)
        val levelLabel = Label(levelText, levelStyle)

        add(nameLabel).row()
        add(levelLabel)
    }
}
