package com.example.game.level.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.example.core.GameConstants
import com.example.game.reward.Reward
import com.example.ui.GameButton
import com.example.utils.TextureFactory

class LevelUpPopup(
    private val stage: Stage,
    private val font: BitmapFont,
    private val oldLevel: Int,
    private val newLevel: Int,
    private val rewards: List<Reward>,
    private val onClose: () -> Unit
) {

    private val containerTable = Table()

    init {
        setupUI()
    }

    private fun setupUI() {
        containerTable.clear()
        containerTable.setFillParent(true)

        val overlay = Image(TextureFactory.createRoundedPanel(
            width = 10, height = 10,
            fillColor = Color(0f, 0f, 0f, 0.75f),
            borderColor = Color.CLEAR,
            borderThickness = 0
        ))
        overlay.setFillParent(true)

        val cardTable = Table()
        cardTable.background = TextureFactory.createRoundedPanel(
            width = 440, height = 480,
            fillColor = Color(0.12f, 0.16f, 0.28f, 0.98f),
            borderColor = GameConstants.COLOR_GOLD,
            borderThickness = 4,
            cornerRadius = 24
        )
        cardTable.pad(20f)

        val titleFont = BitmapFont()
        titleFont.data.setScale(1.8f)
        val titleStyle = Label.LabelStyle(titleFont, GameConstants.COLOR_GOLD)

        val levelFont = BitmapFont()
        levelFont.data.setScale(1.4f)
        val levelStyle = Label.LabelStyle(levelFont, Color.WHITE)

        val subFont = BitmapFont()
        subFont.data.setScale(1.0f)
        val subStyle = Label.LabelStyle(subFont, Color(0.85f, 0.9f, 0.95f, 1f))

        cardTable.add(Label("LEVEL UP!", titleStyle)).padBottom(10f).row()
        cardTable.add(Label("Level $oldLevel ➔ Level $newLevel", levelStyle)).padBottom(16f).row()
        cardTable.add(Label("REWARDS EARNED", subStyle)).padBottom(12f).row()

        val rewardsTable = Table()
        for (r in rewards) {
            val iconName = when (r.type) {
                com.example.game.reward.RewardType.COINS -> "coin"
                com.example.game.reward.RewardType.GEMS -> "gem"
                com.example.game.reward.RewardType.ENERGY -> "energy"
                com.example.game.reward.RewardType.BOOSTER -> "star"
                else -> "star"
            }
            val icon = Image(TextureFactory.createIcon(iconName, 32))
            val label = Label("+${r.quantity} ${r.type.name}", subStyle)
            rewardsTable.add(icon).size(32f).padRight(10f)
            rewardsTable.add(label).left().padBottom(6f).row()
        }

        cardTable.add(rewardsTable).padBottom(24f).row()

        val btnFont = BitmapFont()
        btnFont.data.setScale(1.1f)
        val btnStyle = Label.LabelStyle(btnFont, Color.WHITE)

        val continueBtn = GameButton(
            text = "CONTINUE",
            bgColor = GameConstants.COLOR_ISLAND_GREEN,
            labelStyle = btnStyle,
            onClick = {
                containerTable.remove()
                onClose()
            }
        )

        cardTable.add(continueBtn).size(220f, 52f)

        containerTable.addActor(overlay)
        containerTable.add(cardTable)

        stage.addActor(containerTable)
    }
}
