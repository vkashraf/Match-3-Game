package com.example.game.energy.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.example.core.GameConstants
import com.example.core.GameDataProvider
import com.example.game.energy.EnergyManager
import com.example.ui.GameButton
import com.example.utils.TextureFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NoEnergyPopup(
    private val stage: Stage,
    private val font: BitmapFont,
    private val onRefilled: () -> Unit,
    private val onClose: () -> Unit
) {

    private val containerTable = Table()
    private val scope = CoroutineScope(Dispatchers.Main)

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
            width = 420, height = 380,
            fillColor = Color(0.12f, 0.16f, 0.28f, 0.98f),
            borderColor = GameConstants.COLOR_GOLD,
            borderThickness = 4,
            cornerRadius = 24
        )
        cardTable.pad(20f)

        val titleFont = BitmapFont()
        titleFont.data.setScale(1.6f)
        val titleStyle = Label.LabelStyle(titleFont, Color(0.95f, 0.35f, 0.35f, 1f))

        val fontMedium = BitmapFont()
        fontMedium.data.setScale(1.1f)
        val textStyle = Label.LabelStyle(fontMedium, Color.WHITE)

        val subStyle = Label.LabelStyle(fontMedium, Color(0.85f, 0.9f, 0.95f, 1f))

        cardTable.add(Label("NO ENERGY!", titleStyle)).padBottom(12f).row()
        cardTable.add(Label("You need 1 Energy to play this level.", subStyle)).padBottom(20f).row()

        val player = GameDataProvider.cachedPlayer.value
        val timerSecs = if (player != null) EnergyManager.getRemainingSecondsForNextEnergy(player) else 1800L
        val timeStr = EnergyManager.formatRemainingTime(timerSecs)

        cardTable.add(Label("Next Energy in: $timeStr", textStyle)).padBottom(24f).row()

        val btnFont = BitmapFont()
        btnFont.data.setScale(1.0f)
        val btnStyle = Label.LabelStyle(btnFont, Color.WHITE)

        val refillBtn = GameButton(
            text = "REFILL FULL (20 Gems)",
            bgColor = GameConstants.COLOR_ISLAND_GREEN,
            labelStyle = btnStyle,
            onClick = {
                scope.launch {
                    val success = EnergyManager.fullRefillEnergyWithGems(GameDataProvider.playerRepository)
                    if (success) {
                        withContext(Dispatchers.Main) {
                            containerTable.remove()
                            onRefilled()
                        }
                    }
                }
            }
        )

        val waitBtn = GameButton(
            text = "WAIT",
            bgColor = Color(0.3f, 0.35f, 0.45f, 1f),
            labelStyle = btnStyle,
            onClick = {
                containerTable.remove()
                onClose()
            }
        )

        cardTable.add(refillBtn).size(280f, 52f).padBottom(12f).row()
        cardTable.add(waitBtn).size(180f, 44f)

        containerTable.addActor(overlay)
        containerTable.add(cardTable)

        stage.addActor(containerTable)
    }
}
