package com.example.game.island.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.example.core.GameConstants
import com.example.core.GameDataProvider
import com.example.data.local.entity.IslandZoneEntity
import com.example.game.island.zone.ZoneManager
import com.example.ui.GameButton
import com.example.utils.TextureFactory

class ZoneUnlockPopup(
    private val stage: Stage,
    private val font: BitmapFont,
    private val zone: IslandZoneEntity,
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
            width = 460, height = 400,
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

        val titleLabel = Label(if (zone.isUnlocked) "ZONE UNLOCKED!" else "UNLOCK ZONE", titleStyle)
        val zoneNameLbl = Label(zone.name, Label.LabelStyle(font, Color.WHITE))

        val player = GameDataProvider.cachedPlayer.value
        val playerLevel = player?.playerLevel ?: 1
        val playerStars = player?.totalStars ?: 0
        val playerCoins = player?.coins ?: 0L

        val canUnlock = !zone.isUnlocked &&
                        playerLevel >= zone.requiredPlayerLevel &&
                        playerStars >= zone.requiredStars &&
                        playerCoins >= zone.unlockCostCoins

        cardTable.add(titleLabel).padBottom(8f).row()
        cardTable.add(zoneNameLbl).padBottom(16f).row()

        if (!zone.isUnlocked) {
            val reqLevelLbl = Label("Req Player Level: ${zone.requiredPlayerLevel}", subStyle)
            val reqStarsLbl = Label("Req Stars: ${zone.requiredStars}", subStyle)
            val costLbl = Label("Unlock Cost: ${zone.unlockCostCoins} Coins", textStyle)

            cardTable.add(reqLevelLbl).padBottom(4f).row()
            cardTable.add(reqStarsLbl).padBottom(4f).row()
            cardTable.add(costLbl).padBottom(16f).row()

            val btnFont = BitmapFont()
            btnFont.data.setScale(1.1f)
            val btnStyle = Label.LabelStyle(btnFont, Color.WHITE)

            val unlockBtn = GameButton(
                text = if (canUnlock) "UNLOCK ZONE" else "LOCKED",
                iconDrawable = if (canUnlock) TextureFactory.createIcon("coin", 28) else null,
                bgColor = if (canUnlock) GameConstants.COLOR_PLAY_BUTTON else Color(0.4f, 0.4f, 0.45f, 1f),
                labelStyle = btnStyle,
                onClick = {
                    if (canUnlock) {
                        ZoneManager.unlockZone(
                            zone = zone,
                            onSuccess = { dismiss(); onClose() },
                            onError = { }
                        )
                    }
                }
            )
            cardTable.add(unlockBtn).size(280f, 50f).padBottom(10f).row()
        } else {
            val descLbl = Label("You have full access to construct buildings and expand plots in this area!", subStyle)
            cardTable.add(descLbl).padBottom(20f).row()
        }

        val closeBtn = GameButton(
            text = "CLOSE",
            bgColor = Color(0.35f, 0.4f, 0.48f, 1f),
            labelStyle = Label.LabelStyle(font, Color.WHITE),
            onClick = { dismiss(); onClose() }
        )
        cardTable.add(closeBtn).size(150f, 42f)

        containerTable.add(cardTable).center()
        stage.addActor(overlay)
        stage.addActor(containerTable)
    }

    private fun dismiss() {
        containerTable.remove()
    }
}
