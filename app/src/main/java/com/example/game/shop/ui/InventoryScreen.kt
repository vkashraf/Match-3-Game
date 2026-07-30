package com.example.game.shop.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.viewport.FitViewport
import com.example.core.GameConstants
import com.example.core.GameDataProvider
import com.example.core.ScreenManager
import com.example.data.local.entity.InventoryItemEntity
import com.example.game.BaseGameScreen
import com.example.game.HomeScreen
import com.example.game.IsleMatchGame
import com.example.game.economy.NumberFormatter
import com.example.game.match3.booster.BoosterType
import com.example.game.match3.ui.LevelMapScreen
import com.example.game.resource.ResourceManager
import com.example.game.resource.ResourceType
import com.example.ui.GameButton
import com.example.ui.ResourceCounter
import com.example.utils.TextureFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

enum class InventoryCategory {
    BOOSTERS,
    MATERIALS,
    DECORATIONS
}

class InventoryScreen(
    game: IsleMatchGame,
    private val screenManager: ScreenManager
) : BaseGameScreen(game) {

    private val stage = Stage(FitViewport(GameConstants.VIRTUAL_WIDTH, GameConstants.VIRTUAL_HEIGHT))
    private val font = BitmapFont()
    private val scope = CoroutineScope(Dispatchers.IO)

    private var activeCategory = InventoryCategory.BOOSTERS
    private var cachedInventoryList: List<InventoryItemEntity> = emptyList()

    private lateinit var coinCounter: ResourceCounter
    private lateinit var gemCounter: ResourceCounter
    private lateinit var energyCounter: ResourceCounter

    private val itemsTable = Table()

    init {
        Gdx.input.inputProcessor = stage
        font.data.setScale(1.1f)

        setupTopHUD()
        setupCategoryTabs()
        setupContentArea()

        observeData()
    }

    private fun observeData() {
        scope.launch {
            GameDataProvider.cachedPlayer.collectLatest { p ->
                if (p != null) {
                    coinCounter.setValue(p.coins.toString())
                    gemCounter.setValue(p.gems.toString())
                    energyCounter.setValue("${p.energy}/${p.maxEnergy}")
                }
            }
        }

        scope.launch {
            GameDataProvider.inventoryRepository.allItemsFlow.collectLatest { inventoryItems ->
                cachedInventoryList = inventoryItems
                launch(Dispatchers.Main) {
                    refreshContent()
                }
            }
        }
    }

    private fun setupTopHUD() {
        val topTable = Table()
        topTable.top().left()
        topTable.setFillParent(true)
        topTable.pad(16f)

        val fontSmall = BitmapFont()
        fontSmall.data.setScale(1.0f)
        val valueStyle = Label.LabelStyle(fontSmall, Color.WHITE)

        val backBtn = GameButton("BACK", bgColor = Color(0.2f, 0.3f, 0.5f, 1f), labelStyle = valueStyle) {
            screenManager.setScreen(HomeScreen(game, screenManager))
        }

        val initialPlayer = GameDataProvider.cachedPlayer.value
        val coinsStr = initialPlayer?.coins?.toString() ?: "0"
        val gemsStr = initialPlayer?.gems?.toString() ?: "0"
        val energyStr = "${initialPlayer?.energy ?: 0}/${initialPlayer?.maxEnergy ?: 5}"

        coinCounter = ResourceCounter("coin", coinsStr, valueStyle)
        gemCounter = ResourceCounter("gem", gemsStr, valueStyle)
        energyCounter = ResourceCounter("energy", energyStr, valueStyle)

        topTable.add(backBtn).size(90f, 44f).padRight(12f)
        topTable.add(coinCounter).padRight(6f)
        topTable.add(gemCounter).padRight(6f)
        topTable.add(energyCounter).expandX().left()

        stage.addActor(topTable)
    }

    private fun setupCategoryTabs() {
        val tabTable = Table()
        tabTable.setPosition(20f, GameConstants.VIRTUAL_HEIGHT - 120f)
        tabTable.setSize(GameConstants.VIRTUAL_WIDTH - 40f, 50f)

        val tabFont = BitmapFont()
        tabFont.data.setScale(0.95f)
        val tabStyle = Label.LabelStyle(tabFont, Color.WHITE)

        val categories = listOf(
            Pair(InventoryCategory.BOOSTERS, "BOOSTERS"),
            Pair(InventoryCategory.MATERIALS, "MATERIALS"),
            Pair(InventoryCategory.DECORATIONS, "DECORATIONS")
        )

        for ((cat, title) in categories) {
            val isActive = (cat == activeCategory)
            val btnColor = if (isActive) GameConstants.COLOR_GOLD else Color(0.18f, 0.25f, 0.38f, 1f)

            val catBtn = GameButton(
                text = title,
                bgColor = btnColor,
                labelStyle = tabStyle,
                onClick = {
                    activeCategory = cat
                    refreshContent()
                }
            )
            tabTable.add(catBtn).size(180f, 44f).padRight(12f)
        }

        stage.addActor(tabTable)
    }

    private fun setupContentArea() {
        val scrollPane = ScrollPane(itemsTable)
        scrollPane.setPosition(20f, 30f)
        scrollPane.setSize(GameConstants.VIRTUAL_WIDTH - 40f, GameConstants.VIRTUAL_HEIGHT - 160f)
        scrollPane.setScrollingDisabled(true, false)

        stage.addActor(scrollPane)
    }

    private fun refreshContent() {
        scope.launch(Dispatchers.IO) {
            val nameFont = BitmapFont()
            nameFont.data.setScale(1.1f)
            val nameStyle = Label.LabelStyle(nameFont, Color.WHITE)

            val descFont = BitmapFont()
            descFont.data.setScale(0.85f)
            val descStyle = Label.LabelStyle(descFont, Color(0.8f, 0.85f, 0.9f, 1f))

            val btnFont = BitmapFont()
            btnFont.data.setScale(1.0f)
            val btnStyle = Label.LabelStyle(btnFont, Color.WHITE)

            val tempTable = Table()
            tempTable.top().pad(10f)

            when (activeCategory) {
                InventoryCategory.BOOSTERS -> {
                    val qtyMap = cachedInventoryList.associate { it.itemId to it.quantity }
                    val boosters = BoosterType.entries.filter { it == BoosterType.HAMMER || it == BoosterType.SWAP || it == BoosterType.SHUFFLE || it == BoosterType.EXTRA_MOVES }

                    for (booster in boosters) {
                        val qty = qtyMap[booster.id] ?: 0

                        val cardTable = Table()
                        cardTable.background = TextureFactory.createRoundedPanel(
                            width = 580, height = 110,
                            fillColor = Color(0.12f, 0.16f, 0.24f, 0.95f),
                            borderColor = GameConstants.COLOR_GOLD,
                            borderThickness = 3,
                            cornerRadius = 16
                        )
                        cardTable.pad(12f)

                        val itemIcon = Image(TextureFactory.createIcon(booster.iconName, 54))

                        val infoTable = Table()
                        infoTable.left()
                        val nameLbl = Label("${booster.displayName} (x$qty)", nameStyle)
                        val descLbl = Label(booster.description, descStyle)
                        infoTable.add(nameLbl).left().row()
                        infoTable.add(descLbl).left().width(310f)

                        val btnText = if (qty > 0) "PLAY LEVEL" else "GET MORE"
                        val btnColor = if (qty > 0) GameConstants.COLOR_PLAY_BUTTON else GameConstants.COLOR_GOLD

                        val actionBtn = GameButton(
                            text = btnText,
                            bgColor = btnColor,
                            labelStyle = btnStyle,
                            onClick = {
                                if (qty > 0) {
                                    screenManager.setScreen(LevelMapScreen(game, screenManager))
                                } else {
                                    screenManager.setScreen(ShopScreen(game, screenManager))
                                }
                            }
                        )

                        cardTable.add(itemIcon).size(54f, 54f).padRight(12f)
                        cardTable.add(infoTable).expandX().left()
                        cardTable.add(actionBtn).size(135f, 48f)

                        tempTable.add(cardTable).size(580f, 110f).padBottom(12f).row()
                    }
                }
                InventoryCategory.MATERIALS -> {
                    val materials = listOf(
                        Triple(ResourceType.WOOD, "Wood", "wood"),
                        Triple(ResourceType.STONE, "Stone", "stone"),
                        Triple(ResourceType.METAL, "Metal", "metal"),
                        Triple(ResourceType.FOOD, "Food", "food")
                    )

                    val maxCap = ResourceManager.getStorageCapacity()

                    for ((resType, name, icon) in materials) {
                        val owned = ResourceManager.getResourceAmount(resType)

                        val cardTable = Table()
                        cardTable.background = TextureFactory.createRoundedPanel(
                            width = 580, height = 110,
                            fillColor = Color(0.12f, 0.16f, 0.24f, 0.95f),
                            borderColor = GameConstants.COLOR_GOLD,
                            borderThickness = 3,
                            cornerRadius = 16
                        )
                        cardTable.pad(12f)

                        val itemIcon = Image(TextureFactory.createIcon(icon, 54))

                        val infoTable = Table()
                        infoTable.left()
                        val nameLbl = Label("$name Storage", nameStyle)
                        val capStr = NumberFormatter.formatResourceWithCapacity(owned, maxCap)
                        val descLbl = Label("Capacity: $capStr", descStyle)
                        infoTable.add(nameLbl).left().row()
                        infoTable.add(descLbl).left().width(310f)

                        val getMoreBtn = GameButton(
                            text = "GET MORE",
                            bgColor = GameConstants.COLOR_GOLD,
                            labelStyle = btnStyle,
                            onClick = {
                                screenManager.setScreen(ShopScreen(game, screenManager))
                            }
                        )

                        cardTable.add(itemIcon).size(54f, 54f).padRight(12f)
                        cardTable.add(infoTable).expandX().left()
                        cardTable.add(getMoreBtn).size(135f, 48f)

                        tempTable.add(cardTable).size(580f, 110f).padBottom(12f).row()
                    }
                }
                InventoryCategory.DECORATIONS -> {
                    val decos = cachedInventoryList.filter { it.itemType == "DECORATION" }
                    if (decos.isEmpty()) {
                        val emptyTable = Table()
                        emptyTable.pad(40f)

                        val titleLbl = Label("NO DECORATIONS YET", Label.LabelStyle(nameFont, GameConstants.COLOR_GOLD))
                        val descLbl = Label("Visit the shop to unlock island decorations!", descStyle)

                        val shopBtn = GameButton("VISIT SHOP", bgColor = GameConstants.COLOR_PLAY_BUTTON, labelStyle = btnStyle) {
                            screenManager.setScreen(ShopScreen(game, screenManager))
                        }

                        emptyTable.add(titleLbl).padBottom(12f).row()
                        emptyTable.add(descLbl).padBottom(20f).row()
                        emptyTable.add(shopBtn).size(160f, 48f)

                        tempTable.add(emptyTable).expandX().center().padTop(60f)
                    } else {
                        for (deco in decos) {
                            val cardTable = Table()
                            cardTable.background = TextureFactory.createRoundedPanel(
                                width = 580, height = 110,
                                fillColor = Color(0.12f, 0.16f, 0.24f, 0.95f),
                                borderColor = GameConstants.COLOR_GOLD,
                                borderThickness = 3,
                                cornerRadius = 16
                            )
                            cardTable.pad(12f)

                            val itemIcon = Image(TextureFactory.createIcon("decoration", 54))

                            val infoTable = Table()
                            infoTable.left()
                            val nameLbl = Label("${deco.itemId} (x${deco.quantity})", nameStyle)
                            val descLbl = Label("Owned decoration ready for Island placement.", descStyle)
                            infoTable.add(nameLbl).left().row()
                            infoTable.add(descLbl).left().width(310f)

                            cardTable.add(itemIcon).size(54f, 54f).padRight(12f)
                            cardTable.add(infoTable).expandX().left()

                            tempTable.add(cardTable).size(580f, 110f).padBottom(12f).row()
                        }
                    }
                }
            }

            launch(Dispatchers.Main) {
                itemsTable.clear()
                itemsTable.add(tempTable)
            }
        }
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0.08f, 0.12f, 0.22f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        stage.act(delta)
        stage.draw()
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun dispose() {
        stage.dispose()
        font.dispose()
    }
}
