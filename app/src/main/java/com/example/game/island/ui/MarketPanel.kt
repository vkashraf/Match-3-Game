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

class MarketPanel(
    private val stage: Stage,
    private val font: BitmapFont,
    private val onClose: () -> Unit
) {

    private val containerTable = Table()
    private val scope = CoroutineScope(Dispatchers.Main)

    private data class TradeOffer(
        val title: String,
        val costType: ResourceType,
        val costAmount: Int,
        val rewardType: ResourceType,
        val rewardAmount: Int
    )

    private val offers = listOf(
        TradeOffer("Sell Timber", ResourceType.WOOD, 50, ResourceType.COINS, 250),
        TradeOffer("Sell Quarry Stone", ResourceType.STONE, 40, ResourceType.COINS, 300),
        TradeOffer("Sell Farm Crops", ResourceType.FOOD, 50, ResourceType.COINS, 350),
        TradeOffer("Trade Metal Ore", ResourceType.METAL, 30, ResourceType.GEMS, 15)
    )

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
            width = 540, height = 640,
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

        val btnFont = BitmapFont()
        btnFont.data.setScale(0.9f)
        val btnStyle = Label.LabelStyle(btnFont, Color.WHITE)

        val titleLabel = Label("ISLAND TRADE MARKET", titleStyle)
        cardTable.add(titleLabel).padBottom(16f).colspan(2).row()

        val listTable = Table()

        for (offer in offers) {
            val iconImg = Image(TextureFactory.createIcon(offer.costType.iconName, 48))
            val infoTable = Table()
            infoTable.left()

            val nameLbl = Label(offer.title, itemStyle)
            val descLbl = Label("${offer.costAmount} ${offer.costType.displayName} ➔ +${offer.rewardAmount} ${offer.rewardType.displayName}", subStyle)

            infoTable.add(nameLbl).left().row()
            infoTable.add(descLbl).left()

            val tradeBtn = GameButton(
                text = "EXCHANGE",
                bgColor = GameConstants.COLOR_PLAY_BUTTON,
                labelStyle = btnStyle,
                onClick = {
                    scope.launch {
                        val currentMat = withContext(Dispatchers.IO) { ResourceManager.getMaterialCount(offer.costType) }
                        if (currentMat >= offer.costAmount) {
                            withContext(Dispatchers.IO) {
                                ResourceManager.spendResources(
                                    wood = if (offer.costType == ResourceType.WOOD) offer.costAmount else 0,
                                    stone = if (offer.costType == ResourceType.STONE) offer.costAmount else 0,
                                    metal = if (offer.costType == ResourceType.METAL) offer.costAmount else 0,
                                    food = if (offer.costType == ResourceType.FOOD) offer.costAmount else 0
                                )
                                ResourceManager.addResource(offer.rewardType, offer.rewardAmount, ignoreCapacity = true)
                            }
                            dismiss()
                            onClose()
                        }
                    }
                }
            )

            listTable.add(iconImg).size(48f, 48f).padRight(12f)
            listTable.add(infoTable).expandX().left()
            listTable.add(tradeBtn).size(120f, 42f).padLeft(8f).row()
            listTable.add(Image(TextureFactory.createRoundedPanel(
                width = 480, height = 2,
                fillColor = Color(0.25f, 0.3f, 0.4f, 0.5f),
                borderColor = Color.CLEAR,
                borderThickness = 0
            ))).colspan(3).height(2f).padTop(6f).padBottom(6f).row()
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
