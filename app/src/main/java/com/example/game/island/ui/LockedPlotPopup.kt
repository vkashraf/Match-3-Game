package com.example.game.island.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.example.core.GameConstants
import com.example.core.GameDataProvider
import com.example.data.local.entity.BuildingPlotEntity
import com.example.game.island.plot.PlotManager
import com.example.ui.GameButton
import com.example.utils.TextureFactory

class LockedPlotPopup(
    private val stage: Stage,
    private val font: BitmapFont,
    private val plot: BuildingPlotEntity,
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
            fillColor = Color(0f, 0f, 0f, 0.65f),
            borderColor = Color.CLEAR,
            borderThickness = 0
        ))
        overlay.setFillParent(true)

        val cardTable = Table()
        cardTable.background = TextureFactory.createRoundedPanel(
            width = 440, height = 380,
            fillColor = Color(0.12f, 0.16f, 0.24f, 0.96f),
            borderColor = GameConstants.COLOR_GOLD,
            borderThickness = 4,
            cornerRadius = 24
        )
        cardTable.pad(20f)

        val titleFont = BitmapFont()
        titleFont.data.setScale(1.5f)
        val titleStyle = Label.LabelStyle(titleFont, GameConstants.COLOR_GOLD)

        val textFont = BitmapFont()
        textFont.data.setScale(1.1f)
        val textStyle = Label.LabelStyle(textFont, Color.WHITE)
        val subStyle = Label.LabelStyle(textFont, Color(0.8f, 0.85f, 0.9f, 1f))

        val titleLabel = Label("LOCKED PLOT", titleStyle)
        val lockImg = Image(TextureFactory.createBuildingTexture("plot_locked", 80, 80))

        val player = GameDataProvider.cachedPlayer.value
        val playerLevel = player?.playerLevel ?: 1
        val playerStars = player?.totalStars ?: 0
        val playerCoins = player?.coins ?: 0L

        val reqLevelText = "Required Player Level: ${plot.requiredPlayerLevel} ${if (playerLevel >= plot.requiredPlayerLevel) "✓" else "✗"}"
        val reqStarsText = "Required Stars: ${plot.requiredStars} ${if (playerStars >= plot.requiredStars) "✓" else "✗"}"
        val costText = "Unlock Cost: ${plot.unlockCostCoins} Coins"

        val reqLevelLbl = Label(reqLevelText, subStyle)
        val reqStarsLbl = Label(reqStarsText, subStyle)
        val costLbl = Label(costText, textStyle)

        cardTable.add(titleLabel).padBottom(12f).row()
        cardTable.add(lockImg).size(80f, 80f).padBottom(12f).row()
        cardTable.add(reqLevelLbl).padBottom(4f).row()
        cardTable.add(reqStarsLbl).padBottom(8f).row()
        cardTable.add(costLbl).padBottom(16f).row()

        val canUnlock = playerLevel >= plot.requiredPlayerLevel &&
                        playerStars >= plot.requiredStars &&
                        playerCoins >= plot.unlockCostCoins

        val btnFont = BitmapFont()
        btnFont.data.setScale(1.1f)
        val btnStyle = Label.LabelStyle(btnFont, Color.WHITE)

        val unlockBtn = GameButton(
            text = if (canUnlock) "UNLOCK NOW" else "REQUIREMENTS NOT MET",
            iconDrawable = if (canUnlock) TextureFactory.createIcon("coin", 28) else null,
            bgColor = if (canUnlock) GameConstants.COLOR_PLAY_BUTTON else Color(0.4f, 0.4f, 0.45f, 1f),
            labelStyle = btnStyle,
            onClick = {
                if (canUnlock) {
                    PlotManager.unlockPlot(
                        plot = plot,
                        onSuccess = { dismiss(); onClose() },
                        onError = { }
                    )
                }
            }
        )

        val closeBtn = GameButton(
            text = "CLOSE",
            bgColor = Color(0.35f, 0.4f, 0.48f, 1f),
            labelStyle = btnStyle,
            onClick = { dismiss(); onClose() }
        )

        cardTable.add(unlockBtn).size(280f, 50f).padBottom(10f).row()
        cardTable.add(closeBtn).size(150f, 42f)

        containerTable.add(cardTable).center()
        stage.addActor(overlay)
        stage.addActor(containerTable)
    }

    private fun dismiss() {
        containerTable.remove()
    }
}
