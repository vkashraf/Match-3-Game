package com.example.game.island.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.example.core.GameConstants
import com.example.core.GameDataProvider
import com.example.game.island.building.ProductionManager
import com.example.game.resource.ResourceManager
import com.example.game.resource.ResourceType
import com.example.ui.GameButton
import com.example.utils.TextureFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OfflineSummaryPopup(
    private val stage: Stage,
    private val font: BitmapFont,
    private val onClose: () -> Unit
) {

    private val containerTable = Table()
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        checkAndShowSummary()
    }

    private fun checkAndShowSummary() {
        scope.launch {
            val buildings = GameDataProvider.buildingRepository.getAllBuildings()
            val totals = mutableMapOf<ResourceType, Long>()
            val currentTime = System.currentTimeMillis()

            for (b in buildings) {
                if (b.isBuilt && !b.isConstructing) {
                    val pending = ProductionManager.calculatePendingAmount(b, currentTime)
                    if (pending > 0) {
                        val type = ProductionManager.getProductionType(b)
                        totals[type] = (totals[type] ?: 0L) + pending
                    }
                }
            }

            if (totals.isEmpty() || totals.values.all { it == 0L }) {
                withContext(Dispatchers.Main) {
                    onClose()
                }
                return@launch
            }

            withContext(Dispatchers.Main) {
                setupUI(totals)
            }
        }
    }

    private fun setupUI(totals: Map<ResourceType, Long>) {
        containerTable.clear()
        containerTable.setFillParent(true)

        val overlay = Image(TextureFactory.createRoundedPanel(
            width = 10, height = 10,
            fillColor = Color(0f, 0f, 0f, 0.7f),
            borderColor = Color.CLEAR,
            borderThickness = 0
        ))
        overlay.setFillParent(true)

        val cardTable = Table()
        cardTable.background = TextureFactory.createRoundedPanel(
            width = 460, height = 480,
            fillColor = Color(0.12f, 0.16f, 0.24f, 0.98f),
            borderColor = GameConstants.COLOR_GOLD,
            borderThickness = 4,
            cornerRadius = 24
        )
        cardTable.pad(20f)

        val titleFont = BitmapFont()
        titleFont.data.setScale(1.6f)
        val titleStyle = Label.LabelStyle(titleFont, GameConstants.COLOR_GOLD)

        val textFont = BitmapFont()
        textFont.data.setScale(1.1f)
        val textStyle = Label.LabelStyle(textFont, Color.WHITE)

        val subStyle = Label.LabelStyle(textFont, Color(0.8f, 0.85f, 0.9f, 1f))

        cardTable.add(Label("WELCOME BACK!", titleStyle)).padBottom(8f).row()
        cardTable.add(Label("Your island produced resources while away:", subStyle)).padBottom(20f).row()

        val listTable = Table()
        for ((type, amount) in totals) {
            val resIcon = Image(TextureFactory.createIcon(type.iconName, 36))
            val amountLabel = Label("+ $amount ${type.displayName}", textStyle)
            listTable.add(resIcon).size(36f, 36f).padRight(12f)
            listTable.add(amountLabel).left().padBottom(8f).row()
        }

        cardTable.add(listTable).padBottom(24f).row()

        val btnFont = BitmapFont()
        btnFont.data.setScale(1.1f)
        val btnStyle = Label.LabelStyle(btnFont, Color.WHITE)

        val collectBtn = GameButton(
            text = "COLLECT ALL",
            bgColor = GameConstants.COLOR_ISLAND_GREEN,
            labelStyle = btnStyle,
            onClick = {
                collectAllAndClose(totals)
            }
        )

        cardTable.add(collectBtn).size(240f, 56f).row()

        containerTable.addActor(overlay)
        containerTable.add(cardTable)

        stage.addActor(containerTable)
    }

    private fun collectAllAndClose(totals: Map<ResourceType, Long>) {
        scope.launch {
            val buildings = GameDataProvider.buildingRepository.getAllBuildings()
            val now = System.currentTimeMillis()

            for (b in buildings) {
                if (b.isBuilt && !b.isConstructing) {
                    val pending = ProductionManager.calculatePendingAmount(b, now)
                    if (pending > 0) {
                        val type = ProductionManager.getProductionType(b)
                        ResourceManager.addResource(type, pending)
                        GameDataProvider.buildingRepository.updateBuilding(b.copy(lastCollectedAt = now))
                    }
                }
            }

            withContext(Dispatchers.Main) {
                containerTable.remove()
                onClose()
            }
        }
    }
}
