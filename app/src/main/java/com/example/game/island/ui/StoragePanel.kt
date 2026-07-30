package com.example.game.island.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.example.core.GameConstants
import com.example.game.resource.ResourceManager
import com.example.game.resource.ResourceType
import com.example.ui.GameButton
import com.example.utils.TextureFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StoragePanel(
    private val stage: Stage,
    private val font: BitmapFont,
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
            fillColor = Color(0f, 0f, 0f, 0.65f),
            borderColor = Color.CLEAR,
            borderThickness = 0
        ))
        overlay.setFillParent(true)

        val cardTable = Table()
        cardTable.background = TextureFactory.createRoundedPanel(
            width = 520, height = 620,
            fillColor = Color(0.12f, 0.16f, 0.24f, 0.96f),
            borderColor = GameConstants.COLOR_GOLD,
            borderThickness = 4,
            cornerRadius = 24
        )
        cardTable.pad(20f)

        val titleFont = BitmapFont()
        titleFont.data.setScale(1.4f)
        val titleStyle = Label.LabelStyle(titleFont, GameConstants.COLOR_GOLD)

        val itemFont = BitmapFont()
        itemFont.data.setScale(1.0f)
        val itemStyle = Label.LabelStyle(itemFont, Color.WHITE)

        val subStyle = Label.LabelStyle(itemFont, Color(0.8f, 0.85f, 0.9f, 1f))

        val titleLabel = Label("ISLAND RESOURCE STORAGE", titleStyle)
        cardTable.add(titleLabel).padBottom(16f).colspan(2).row()

        val listTable = Table()

        scope.launch {
            val capacity = withContext(Dispatchers.IO) { ResourceManager.getStorageCapacity() }
            val wood = withContext(Dispatchers.IO) { ResourceManager.getMaterialCount(ResourceType.WOOD) }
            val stone = withContext(Dispatchers.IO) { ResourceManager.getMaterialCount(ResourceType.STONE) }
            val metal = withContext(Dispatchers.IO) { ResourceManager.getMaterialCount(ResourceType.METAL) }
            val food = withContext(Dispatchers.IO) { ResourceManager.getMaterialCount(ResourceType.FOOD) }

            val resources = listOf(
                Triple("WOOD", "Wood Timber", wood),
                Triple("STONE", "Quarry Stone", stone),
                Triple("METAL", "Refined Metal", metal),
                Triple("FOOD", "Harvest Food", food)
            )

            for ((resId, name, count) in resources) {
                val iconImg = Image(TextureFactory.createIcon(resId.lowercase(), 48))
                val infoTable = Table()
                infoTable.left()

                val nameLbl = Label(name, itemStyle)
                val countLbl = Label("$count / $capacity Storage", subStyle)

                infoTable.add(nameLbl).left().row()
                infoTable.add(countLbl).left()

                val fillFraction = (count.toFloat() / capacity.toFloat()).coerceIn(0f, 1f)
                val barImg = Image(TextureFactory.createRoundedPanel(
                    width = 120, height = 16,
                    fillColor = if (fillFraction >= 0.95f) Color.RED else Color(0.2f, 0.8f, 0.35f, 1f),
                    borderColor = Color.DARK_GRAY,
                    borderThickness = 1
                ))

                listTable.add(iconImg).size(48f, 48f).padRight(12f)
                listTable.add(infoTable).expandX().left()
                listTable.add(barImg).size(120f, 16f).padLeft(8f).row()
                listTable.add(Image(TextureFactory.createRoundedPanel(
                    width = 460, height = 2,
                    fillColor = Color(0.25f, 0.3f, 0.4f, 0.5f),
                    borderColor = Color.CLEAR,
                    borderThickness = 0
                ))).colspan(3).height(2f).padTop(8f).padBottom(8f).row()
            }
        }

        cardTable.add(listTable).expand().fill().padBottom(16f).colspan(2).row()

        val closeBtn = GameButton(
            text = "CLOSE",
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
