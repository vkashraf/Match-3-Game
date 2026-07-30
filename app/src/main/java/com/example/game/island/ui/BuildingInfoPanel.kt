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
            width = 480, height = 560,
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
        val descLabel = Label(config.description, subStyle)

        val buildingImg = Image(TextureFactory.createBuildingTexture(building.buildingType, 110, 110))

        cardTable.add(titleLabel).padBottom(2f).row()
        cardTable.add(levelLabel).padBottom(4f).row()
        cardTable.add(descLabel).padBottom(10f).row()
        cardTable.add(buildingImg).size(110f, 110f).padBottom(12f).row()

        val pendingAmount = ProductionManager.calculatePendingAmount(building)
        val resType = ProductionManager.getProductionType(building)
        val currentRate = config.productionForLevel(building.level)
        val nextRate = config.productionForLevel(building.level + 1)

        val nextCoins = config.costCoinsForLevel(building.level + 1)
        val nextWood = config.costWoodForLevel(building.level + 1)
        val nextStone = config.costStoneForLevel(building.level + 1)
        val nextMetal = config.costMetalForLevel(building.level + 1)

        if (building.isConstructing) {
            // Construction Progress UI
            val remainingSecs = ConstructionManager.getRemainingSeconds(building)
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
                        onError = { }
                    )
                }
            )

            cardTable.add(finishGemsBtn).size(260f, 52f).padBottom(12f).row()
        } else {
            // Normal Built Info UI
            if (currentRate > 0) {
                val rateLabel = Label("Production: +$currentRate ${resType.displayName}/hr", textStyle)
                cardTable.add(rateLabel).padBottom(4f).row()
            }

            val btnFont = BitmapFont()
            btnFont.data.setScale(1.0f)
            val btnStyle = Label.LabelStyle(btnFont, Color.WHITE)

            // Collect Button
            if (pendingAmount > 0) {
                val collectBtn = GameButton(
                    text = "COLLECT (+$pendingAmount ${resType.displayName})",
                    iconDrawable = TextureFactory.createIcon(resType.iconName, 26),
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
                cardTable.add(collectBtn).size(320f, 52f).padBottom(10f).row()
            }

            // Quick Special Actions (STORAGE, MARKET, WORKSHOP)
            when (config.buildingType) {
                BuildingType.STORAGE -> {
                    val storageBtn = GameButton(
                        text = "VIEW STORAGE",
                        bgColor = Color(0.2f, 0.45f, 0.75f, 1f),
                        labelStyle = btnStyle,
                        onClick = {
                            dismiss()
                            StoragePanel(stage, font, onClose)
                        }
                    )
                    cardTable.add(storageBtn).size(280f, 46f).padBottom(8f).row()
                }
                BuildingType.MARKET -> {
                    val marketBtn = GameButton(
                        text = "OPEN MARKET",
                        bgColor = Color(0.8f, 0.5f, 0.1f, 1f),
                        labelStyle = btnStyle,
                        onClick = {
                            dismiss()
                            MarketPanel(stage, font, onClose)
                        }
                    )
                    cardTable.add(marketBtn).size(280f, 46f).padBottom(8f).row()
                }
                BuildingType.WORKSHOP -> {
                    val workshopBtn = GameButton(
                        text = "CRAFT BOOSTERS",
                        bgColor = Color(0.2f, 0.65f, 0.5f, 1f),
                        labelStyle = btnStyle,
                        onClick = {
                            dismiss()
                            CraftingPanel(stage, font, onClose)
                        }
                    )
                    cardTable.add(workshopBtn).size(280f, 46f).padBottom(8f).row()
                }
                else -> {}
            }

            // Upgrade Button
            if (building.level < config.maxLevel) {
                val upgradeCosts = mutableListOf<String>()
                if (nextCoins > 0) upgradeCosts.add("$nextCoins Coins")
                if (nextWood > 0) upgradeCosts.add("$nextWood Wood")
                if (nextStone > 0) upgradeCosts.add("$nextStone Stone")
                if (nextMetal > 0) upgradeCosts.add("$nextMetal Metal")

                val upgradeCostStr = upgradeCosts.joinToString(", ")

                val upgradeBtn = GameButton(
                    text = "UPGRADE ($upgradeCostStr)",
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
                cardTable.add(upgradeBtn).size(320f, 52f).padBottom(10f).row()
            } else {
                val maxLabel = Label("MAX LEVEL REACHED", Label.LabelStyle(font, GameConstants.COLOR_GOLD))
                cardTable.add(maxLabel).padBottom(10f).row()
            }
        }

        // Close Button
        val closeBtn = GameButton(
            text = "CLOSE",
            bgColor = Color(0.35f, 0.4f, 0.48f, 1f),
            labelStyle = Label.LabelStyle(font, Color.WHITE),
            onClick = { dismiss(); onClose() }
        )
        cardTable.add(closeBtn).size(160f, 42f)

        containerTable.add(cardTable).center()
        stage.addActor(overlay)
        stage.addActor(containerTable)
    }

    private fun dismiss() {
        containerTable.remove()
    }
}
