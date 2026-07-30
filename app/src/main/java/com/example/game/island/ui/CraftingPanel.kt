package com.example.game.island.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.example.core.GameConstants
import com.example.core.GameDataProvider
import com.example.game.resource.ResourceManager
import com.example.ui.GameButton
import com.example.utils.TextureFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CraftingPanel(
    private val stage: Stage,
    private val font: BitmapFont,
    private val onClose: () -> Unit
) {

    private val containerTable = Table()
    private val scope = CoroutineScope(Dispatchers.Main)

    private data class Recipe(
        val boosterId: String,
        val displayName: String,
        val woodCost: Int,
        val stoneCost: Int,
        val metalCost: Int,
        val foodCost: Int
    )

    private val recipes = listOf(
        Recipe("HAMMER", "Hammer Booster", woodCost = 30, stoneCost = 15, metalCost = 0, foodCost = 0),
        Recipe("SWAP", "Hand Swap", woodCost = 0, stoneCost = 20, metalCost = 10, foodCost = 0),
        Recipe("SHUFFLE", "Board Shuffle", woodCost = 25, stoneCost = 0, metalCost = 0, foodCost = 15),
        Recipe("EXTRA_MOVES", "+5 Extra Moves", woodCost = 40, stoneCost = 0, metalCost = 20, foodCost = 0)
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

        val titleLabel = Label("BOOSTER WORKSHOP", titleStyle)
        cardTable.add(titleLabel).padBottom(16f).colspan(2).row()

        val listTable = Table()

        for (recipe in recipes) {
            val iconImg = Image(TextureFactory.createIcon(recipe.boosterId.lowercase(), 48))
            val infoTable = Table()
            infoTable.left()

            val nameLbl = Label(recipe.displayName, itemStyle)

            val reqs = mutableListOf<String>()
            if (recipe.woodCost > 0) reqs.add("${recipe.woodCost} Wood")
            if (recipe.stoneCost > 0) reqs.add("${recipe.stoneCost} Stone")
            if (recipe.metalCost > 0) reqs.add("${recipe.metalCost} Metal")
            if (recipe.foodCost > 0) reqs.add("${recipe.foodCost} Food")

            val descLbl = Label("Cost: ${reqs.joinToString(", ")}", subStyle)

            infoTable.add(nameLbl).left().row()
            infoTable.add(descLbl).left()

            val craftBtn = GameButton(
                text = "CRAFT",
                bgColor = GameConstants.COLOR_PLAY_BUTTON,
                labelStyle = btnStyle,
                onClick = {
                    scope.launch {
                        val affordable = withContext(Dispatchers.IO) {
                            ResourceManager.canAfford(
                                wood = recipe.woodCost,
                                stone = recipe.stoneCost,
                                metal = recipe.metalCost,
                                food = recipe.foodCost
                            )
                        }
                        if (affordable) {
                            withContext(Dispatchers.IO) {
                                ResourceManager.spendResources(
                                    wood = recipe.woodCost,
                                    stone = recipe.stoneCost,
                                    metal = recipe.metalCost,
                                    food = recipe.foodCost
                                )
                                GameDataProvider.inventoryRepository.addItem(recipe.boosterId, 1, "BOOSTER")
                            }
                            dismiss()
                            onClose()
                        }
                    }
                }
            )

            listTable.add(iconImg).size(48f, 48f).padRight(12f)
            listTable.add(infoTable).expandX().left()
            listTable.add(craftBtn).size(105f, 42f).padLeft(8f).row()
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
