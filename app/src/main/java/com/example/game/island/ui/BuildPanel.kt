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
import com.example.game.island.building.BuildingConfigRepository
import com.example.game.island.building.BuildingManager
import com.example.ui.GameButton
import com.example.utils.TextureFactory

class BuildPanel(
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
            width = 580, height = 700,
            fillColor = Color(0.12f, 0.16f, 0.24f, 0.96f),
            borderColor = GameConstants.COLOR_GOLD,
            borderThickness = 4,
            cornerRadius = 24
        )
        cardTable.pad(18f)

        val titleFont = BitmapFont()
        titleFont.data.setScale(1.5f)
        val titleStyle = Label.LabelStyle(titleFont, GameConstants.COLOR_GOLD)

        val titleLabel = Label("CONSTRUCT NEW BUILDING", titleStyle)
        cardTable.add(titleLabel).padBottom(16f).colspan(2).row()

        val itemFont = BitmapFont()
        itemFont.data.setScale(1.0f)
        val nameStyle = Label.LabelStyle(itemFont, Color.WHITE)

        val subFont = BitmapFont()
        subFont.data.setScale(0.85f)
        val subStyle = Label.LabelStyle(subFont, Color(0.8f, 0.85f, 0.9f, 1f))

        val btnFont = BitmapFont()
        btnFont.data.setScale(0.95f)
        val btnStyle = Label.LabelStyle(btnFont, Color.WHITE)

        val player = GameDataProvider.cachedPlayer.value
        val playerLevel = player?.playerLevel ?: 1
        val playerCoins = player?.coins ?: 0L

        val configs = BuildingConfigRepository.getAllConfigs()

        val listTable = Table()
        for (config in configs) {
            val cost = config.costForLevel(1)
            val buildImg = Image(TextureFactory.createBuildingTexture(config.buildingType.typeId, 70, 70))

            val infoTable = Table()
            infoTable.left()
            val nameLbl = Label(config.buildingType.displayName, nameStyle)
            val prodLbl = Label("+${config.baseProductionPerHour} Coins/hr • ${config.baseConstructionDurationSecs}s", subStyle)
            val reqLbl = Label("Cost: $cost Coins (Req Lv.${config.requiredPlayerLevel})", subStyle)

            infoTable.add(nameLbl).left().row()
            infoTable.add(prodLbl).left().row()
            infoTable.add(reqLbl).left()

            val canBuild = playerLevel >= config.requiredPlayerLevel && playerCoins >= cost
            val btnColor = if (canBuild) GameConstants.COLOR_PLAY_BUTTON else Color(0.4f, 0.4f, 0.45f, 1f)

            val buildBtn = GameButton(
                text = if (canBuild) "BUILD" else "LOCKED",
                bgColor = btnColor,
                labelStyle = btnStyle,
                onClick = {
                    if (canBuild) {
                        BuildingManager.constructBuilding(
                            plot = plot,
                            type = config.buildingType,
                            onSuccess = { dismiss(); onClose() },
                            onError = { }
                        )
                    }
                }
            )

            listTable.add(buildImg).size(70f, 70f).padRight(12f)
            listTable.add(infoTable).expandX().left()
            listTable.add(buildBtn).size(105f, 42f).padLeft(8f).row()
            listTable.add(Image(TextureFactory.createRoundedPanel(
                width = 500, height = 2,
                fillColor = Color(0.25f, 0.3f, 0.4f, 0.5f),
                borderColor = Color.CLEAR,
                borderThickness = 0
            ))).colspan(3).height(2f).padTop(6f).padBottom(6f).row()
        }

        cardTable.add(listTable).expand().fill().padBottom(16f).colspan(2).row()

        val closeBtn = GameButton(
            text = "CANCEL",
            bgColor = Color(0.35f, 0.4f, 0.48f, 1f),
            labelStyle = Label.LabelStyle(font, Color.WHITE),
            onClick = { dismiss(); onClose() }
        )
        cardTable.add(closeBtn).size(180f, 48f).colspan(2)

        containerTable.add(cardTable).center()
        stage.addActor(overlay)
        stage.addActor(containerTable)
    }

    private fun dismiss() {
        containerTable.remove()
    }
}
