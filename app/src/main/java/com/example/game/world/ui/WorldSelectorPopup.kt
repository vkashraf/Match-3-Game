package com.example.game.world.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.example.core.GameConstants
import com.example.data.local.entity.WorldProgressEntity
import com.example.game.world.WorldConfig
import com.example.game.world.WorldConfigRepository
import com.example.ui.GameButton
import com.example.utils.TextureFactory

class WorldSelectorPopup(
    private val worldProgressMap: Map<Int, WorldProgressEntity>,
    private val worldStarsMap: Map<Int, Int>, // worldId -> total stars earned
    private val onWorldSelected: (WorldConfig) -> Unit,
    private val onClose: () -> Unit
) : Table() {

    init {
        background = TextureFactory.createRoundedPanel(
            width = 440,
            height = 560,
            fillColor = Color(0.1f, 0.16f, 0.28f, 0.98f),
            borderColor = GameConstants.COLOR_GOLD,
            borderThickness = 4
        )
        setSize(440f, 560f)
        setPosition(
            GameConstants.VIRTUAL_WIDTH / 2f - 220f,
            GameConstants.VIRTUAL_HEIGHT / 2f - 280f
        )
        pad(16f)

        val titleFont = BitmapFont()
        titleFont.data.setScale(1.5f)
        val titleStyle = Label.LabelStyle(titleFont, GameConstants.COLOR_GOLD)
        add(Label("SELECT WORLD", titleStyle)).padBottom(12f).row()

        val fontMedium = BitmapFont()
        fontMedium.data.setScale(1.0f)
        val styleWhite = Label.LabelStyle(fontMedium, Color.WHITE)
        val styleGold = Label.LabelStyle(fontMedium, GameConstants.COLOR_GOLD)
        val styleGray = Label.LabelStyle(fontMedium, Color.LIGHT_GRAY)

        val fontBtn = BitmapFont()
        fontBtn.data.setScale(0.9f)
        val btnStyle = Label.LabelStyle(fontBtn, Color.WHITE)

        val contentTable = Table()
        contentTable.pad(8f)

        val worlds = WorldConfigRepository.getWorlds()
        for (world in worlds) {
            val progress = worldProgressMap[world.worldId]
            val isUnlocked = progress?.isUnlocked ?: (world.worldId == 1)
            val isCompleted = progress?.isCompleted ?: false
            val starsEarned = worldStarsMap[world.worldId] ?: 0

            val cardTable = Table()
            cardTable.background = TextureFactory.createRoundedPanel(
                width = 390,
                height = 70,
                fillColor = if (isUnlocked) Color(0.18f, 0.28f, 0.45f, 0.9f) else Color(0.12f, 0.15f, 0.22f, 0.9f),
                borderColor = if (isCompleted) Color.GREEN else if (isUnlocked) GameConstants.COLOR_GOLD else Color.GRAY,
                borderThickness = 2
            )
            cardTable.pad(10f)

            val infoTable = Table()
            infoTable.left()

            val nameLabel = Label("W${world.worldId}: ${world.worldName}", if (isUnlocked) styleGold else styleGray)
            val levelRangeLabel = Label("Levels ${world.startLevel} - ${world.endLevel}  ★ $starsEarned / 30", styleWhite)

            infoTable.add(nameLabel).left().row()
            infoTable.add(levelRangeLabel).left()

            cardTable.add(infoTable).expandX().left().padRight(12f)

            if (isUnlocked) {
                val actionText = if (isCompleted) "VIEW" else "GO"
                val actionBtn = GameButton(
                    text = actionText,
                    bgColor = if (isCompleted) Color(0.2f, 0.65f, 0.3f, 1f) else GameConstants.COLOR_PLAY_BUTTON,
                    labelStyle = btnStyle,
                    onClick = {
                        remove()
                        onWorldSelected(world)
                    }
                )
                cardTable.add(actionBtn).size(70f, 42f)
            } else {
                val lockLabel = Label("★ Req ${world.requiredStars}", styleGray)
                cardTable.add(lockLabel).padRight(8f)
            }

            contentTable.add(cardTable).size(390f, 70f).padBottom(8f).row()
        }

        val scrollPane = ScrollPane(contentTable)
        scrollPane.setFadeScrollBars(false)

        add(scrollPane).expand().fill().padBottom(12f).row()

        val closeBtn = GameButton("CLOSE", bgColor = Color(0.5f, 0.5f, 0.5f, 1f), labelStyle = btnStyle) {
            remove()
            onClose()
        }
        add(closeBtn).size(130f, 44f)
    }
}
