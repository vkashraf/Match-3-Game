package com.example.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.example.manager.SoundManager
import com.example.utils.TextureFactory

class GameButton(
    text: String = "",
    iconDrawable: Drawable? = null,
    bgColor: Color = Color(0.12f, 0.55f, 0.88f, 1f),
    borderColor: Color = Color(0.08f, 0.35f, 0.65f, 1f),
    labelStyle: Label.LabelStyle,
    val onClick: () -> Unit
) : Table() {

    init {
        val backgroundDrawable = TextureFactory.createRoundedPanel(
            width = 160,
            height = 64,
            fillColor = bgColor,
            borderColor = borderColor,
            borderThickness = 4
        )
        background = backgroundDrawable
        pad(8f)

        if (iconDrawable != null) {
            val iconImage = Image(iconDrawable)
            add(iconImage).size(36f).padRight(8f)
        }

        if (text.isNotEmpty()) {
            val label = Label(text, labelStyle)
            add(label)
        }

        setTransform(true)
        setOrigin(width / 2f, height / 2f)

        addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                SoundManager.playSound("click")
                clearActions()
                addAction(
                    Actions.sequence(
                        Actions.scaleTo(0.92f, 0.92f, 0.06f),
                        Actions.scaleTo(1.0f, 1.0f, 0.06f),
                        Actions.run { onClick() }
                    )
                )
            }
        })
    }
}
