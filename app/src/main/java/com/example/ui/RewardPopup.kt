package com.example.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.example.core.GameConstants
import com.example.game.reward.Reward
import com.example.game.reward.RewardType
import com.example.utils.TextureFactory

object RewardPopup {

    fun show(
        stage: Stage,
        title: String,
        rewards: List<Reward>,
        onDismiss: () -> Unit = {}
    ) {
        val overlay = Table()
        overlay.setFillParent(true)
        overlay.background = TextureFactory.createRoundedPanel(
            width = 1, height = 1,
            fillColor = Color(0f, 0f, 0f, 0.75f),
            borderColor = Color.CLEAR,
            borderThickness = 0
        )

        val dialog = Table()
        dialog.background = TextureFactory.createRoundedPanel(
            width = 380,
            height = 260,
            fillColor = Color(0.12f, 0.18f, 0.32f, 0.98f),
            borderColor = GameConstants.COLOR_GOLD,
            borderThickness = 3
        )
        dialog.setSize(380f, 260f)
        dialog.pad(20f)

        val fontTitle = BitmapFont().apply { data.setScale(1.2f) }
        val fontBody = BitmapFont().apply { data.setScale(1.0f) }

        val titleStyle = Label.LabelStyle(fontTitle, GameConstants.COLOR_GOLD)
        val bodyStyle = Label.LabelStyle(fontBody, Color.WHITE)

        dialog.add(Label(title, titleStyle)).padBottom(16f).row()

        val rewardsTable = Table()
        rewards.forEach { reward ->
            val rewardRow = Table()
            val iconDrawable = when (reward.type) {
                RewardType.COINS -> TextureFactory.createIcon("coin", 36)
                RewardType.GEMS -> TextureFactory.createIcon("gem", 36)
                RewardType.ENERGY -> TextureFactory.createIcon("energy", 36)
                RewardType.BOOSTER -> TextureFactory.createIcon(reward.itemId?.lowercase() ?: "hammer", 36)
                RewardType.XP -> TextureFactory.createIcon("star", 36)
                else -> TextureFactory.createIcon(reward.type.name.lowercase(), 36)
            }

            rewardRow.add(Image(iconDrawable)).size(36f).padRight(10f)
            rewardRow.add(Label("+${reward.getDisplayName()}", bodyStyle))
            rewardsTable.add(rewardRow).pad(6f).row()
        }

        dialog.add(rewardsTable).expandY().center().row()

        val btnStyle = Label.LabelStyle(fontBody, Color.WHITE)
        val okBtn = GameButton("AWESOME!", bgColor = GameConstants.COLOR_GOLD, labelStyle = btnStyle) {
            dialog.addAction(
                Actions.sequence(
                    Actions.scaleTo(0.8f, 0.8f, 0.15f),
                    Actions.run {
                        overlay.remove()
                        onDismiss()
                    }
                )
            )
        }

        dialog.add(okBtn).size(160f, 48f).padTop(12f)

        // Scale animation on open
        dialog.setScale(0.5f)
        dialog.setOrigin(190f, 130f)
        dialog.addAction(Actions.scaleTo(1.0f, 1.0f, 0.25f, com.badlogic.gdx.math.Interpolation.bounceOut))

        overlay.add(dialog).center()
        stage.addActor(overlay)
    }
}
