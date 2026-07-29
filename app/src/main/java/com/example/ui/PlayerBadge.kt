package com.example.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.example.core.GameConstants
import com.example.utils.TextureFactory

class PlayerBadge(
    playerLevel: Int,
    levelStyle: Label.LabelStyle
) : Table() {

    private val levelLabel = Label(playerLevel.toString(), levelStyle)

    init {
        val avatar = Image(TextureFactory.createCharacterAvatar(60))
        val avatarCircle = TextureFactory.createCircleTexture(
            size = 64,
            fillColor = Color(0.2f, 0.6f, 0.9f, 1f),
            borderColor = GameConstants.COLOR_GOLD
        )
        
        val avatarTable = Table()
        avatarTable.background = avatarCircle
        avatarTable.add(avatar).size(52f)

        val badgeTable = Table()
        badgeTable.background = TextureFactory.createCircleTexture(
            size = 28,
            fillColor = GameConstants.COLOR_GOLD,
            borderColor = Color(0.6f, 0.4f, 0.1f, 1f)
        )
        badgeTable.add(levelLabel).center()

        add(avatarTable).size(60f)
        add(badgeTable).size(26f).padLeft(-16f).bottom()
    }

    fun setLevel(newLevel: Int) {
        levelLabel.setText(newLevel.toString())
    }
}
