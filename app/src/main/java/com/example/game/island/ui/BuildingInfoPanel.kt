package com.example.game.island.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.example.core.GameConstants
import com.example.data.local.entity.BuildingEntity
import com.example.game.island.building.BuildingConfigRepository
import com.example.game.island.building.BuildingManager
import com.example.game.island.building.BuildingType
import com.example.game.island.building.ConstructionManager
import com.example.game.island.building.ProductionManager
import com.example.ui.GameButton
import com.example.utils.TextureFactory

class BuildingInfoPanel(
    private val stage: Stage,
    private val font: BitmapFont,
    private val building: BuildingEntity,
    private val onClose: () -> Unit
) {

    private val containerTable = Table()
    private val config = BuildingConfigRepository.getConfig(BuildingType.fromId(building.buildingType))

    init {
        setupUI()
    }

    private fun setupUI() {
        containerTable.clear()
        containerTable.setFillParent(true)

        val overlay = Image(TextureFactory.createRoundedPanel(
            width = 10, height = 10,
            fillColor = Color(0f, 0f, 0f, 0.6f),
            borderColor = Color.CLEAR,
            borderThickness = 0
        ))
        overlay.setFillParent(true)

        val cardTable = Table()
        cardTable.background = TextureFactory.createRoundedPanel(
            width = 460, height = 520,
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

        // Title & Building Image
        val titleLabel = Label(config.buildingType.displayName, titleStyle)
        val levelLabel = Label("Level ${building.level} / ${config.maxLevel}", subStyle)

        val buildingImg = Image(TextureFactory.createBuildingTexture(building.buildingType, 110, 110))

        cardTable.add(titleLabel).padBottom(4f).row()
        cardTable.add(levelLabel).padBottom(12f).row()
        cardTable.add(buildingImg).size(110f, 110f).padBottom(16f).row()

        val pendingCoins = ProductionManager.calculatePendingCoins(building)
        val currentRate = config.productionForLevel(building.level)
        val nextRate = config.productionForLevel(building.level + 1)
        val upgradeCost = config.costForLevel(building.level + 1)

        if (building.isConstructing) {
            // Construction Progress UI
            val remainingSecs = ConstructionManager.getRemainingSeconds(building)
            val progressFraction = ConstructionManager.getProgressFraction(building)
            val remainingText = ConstructionManager.formatRemainingTime(remainingSecs)
            val gemCost = ConstructionManager.getInstantFinishGemCost(building)

            val statusLabel = Label("Under Construction...", Label.LabelStyle(font, Color(0.9f, 0.8f, 0.2f, 1f)))
            val timerLabel = Label("Time Left: $remainingText", textStyle)

            cardTable.add(statusLabel).padBottom(6f).row()
            cardTable.add(timerLabel).padBottom(16f).row()

            val btnFont = BitmapFont()
            btnFont.data.setScale(1.1f)
            val btnStyle = Label.LabelStyle(btnFont, Color.WHITE)

            val finishGemsBtn = GameButton(
                text = "FINISH ($gemCost Gems)",
                iconDrawable = TextureFactory.createIcon("gem", 28),
                bgColor = Color(0.18f, 0.55f, 0.25f, 1f),
                labelStyle = btnStyle,
                onClick = {
                    BuildingManager.finishInstantWithGems(
                        building = building,
                        onSuccess = { dismiss(); onClose() },
                        onError = { /* handled by UI */ }
                    )
                }
            )

            cardTable.add(finishGemsBtn).size(260f, 55f).padBottom(12f).row()
        } else {
            // Normal Built Info UI
            val rateLabel = Label("Production: +$currentRate Coins/hr", textStyle)
            val capLabel = Label("Max Storage Cap: ${ProductionManager.getMaxStorageCapacity(building)} Coins", subStyle)

            cardTable.add(rateLabel).padBottom(4f).row()
            cardTable.add(capLabel).padBottom(16f).row()

            val btnFont = BitmapFont()
            btnFont.data.setScale(1.1f)
            val btnStyle = Label.LabelStyle(btnFont, Color.WHITE)

            // Collect Button
            if (pendingCoins > 0) {
                val collectBtn = GameButton(
                    text = "COLLECT (+$pendingCoins Coins)",
                    iconDrawable = TextureFactory.createIcon("coin", 28),
                    bgColor = Color(0.15f, 0.6f, 0.3f, 1f),
                    labelStyle = btnStyle,
                    onClick = {
                        BuildingManager.collectProduction(
                            building = building,
                            onSuccess = { dismiss(); onClose() },
                            onError = { }
                        )
                    }
                )
                cardTable.add(collectBtn).size(300f, 55f).padBottom(12f).row()
            }

            // Upgrade Button
            if (building.level < config.maxLevel) {
                val nextLabel = Label("Next Level Rate: +$nextRate Coins/hr", subStyle)
                cardTable.add(nextLabel).padBottom(8f).row()

                val upgradeBtn = GameButton(
                    text = "UPGRADE ($upgradeCost Coins)",
                    iconDrawable = TextureFactory.createIcon("coin", 28),
                    bgColor = GameConstants.COLOR_PLAY_BUTTON,
                    borderColor = Color(0.75f, 0.35f, 0.05f, 1f),
                    labelStyle = btnStyle,
                    onClick = {
                        BuildingManager.upgradeBuilding(
                            building = building,
                            onSuccess = { dismiss(); onClose() },
                            onError = { }
                        )
                    }
                )
                cardTable.add(upgradeBtn).size(300f, 55f).padBottom(12f).row()
            } else {
                val maxLabel = Label("MAX LEVEL REACHED", Label.LabelStyle(font, GameConstants.COLOR_GOLD))
                cardTable.add(maxLabel).padBottom(12f).row()
            }
        }

        // Close Button
        val closeBtn = GameButton(
            text = "CLOSE",
            bgColor = Color(0.35f, 0.4f, 0.48f, 1f),
            labelStyle = Label.LabelStyle(font, Color.WHITE),
            onClick = { dismiss(); onClose() }
        )
        cardTable.add(closeBtn).size(160f, 45f)

        containerTable.add(cardTable).center()
        stage.addActor(overlay)
        stage.addActor(containerTable)
    }

    private fun dismiss() {
        containerTable.remove()
    }
}
