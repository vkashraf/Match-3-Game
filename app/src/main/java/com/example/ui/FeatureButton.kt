package com.example.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.example.core.GameConstants
import com.example.manager.SoundManager
import com.example.utils.TextureFactory

class FeatureButton(
    iconType: String,
    title: String,
    badgeText: String = "",
    labelStyle: Label.LabelStyle,
    badgeStyle: Label.LabelStyle,
    onClick: () -> Unit
) : Table() {

    init {
        background = TextureFactory.createRoundedPanel(
            width = 110,
            height = 54,
            fillColor = GameConstants.COLOR_HUD_NAVY,
            borderColor = Color(0.25f, 0.5f, 0.8f, 1f),
            borderThickness = 3
        )
        pad(4f)

        val icon = Image(TextureFactory.createIcon(iconType, size = 32))
        val label = Label(title, labelStyle)

        add(icon).size(28f).padRight(4f)
        add(label)

        if (badgeText.isNotEmpty()) {
            val badgeTable = Table()
            badgeTable.background = TextureFactory.createCircleTexture(
                size = 20,
                fillColor = Color(0.9f, 0.2f, 0.2f, 1f),
                borderColor = Color.WHITE
            )
            val badgeLabel = Label(badgeText, badgeStyle)
            badgeTable.add(badgeLabel).center()
            add(badgeTable).size(20f).padLeft(2f).top()
        }

        setTransform(true)

        addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                SoundManager.playSound("click")
                clearActions()
                addAction(
                    Actions.sequence(
                        Actions.scaleTo(0.9f, 0.9f, 0.05f),
                        Actions.scaleTo(1f, 1f, 0.05f),
                        Actions.run { onClick() }
                    )
                )
            }
        })
    }
}
