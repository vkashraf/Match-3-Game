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
import com.example.game.BaseGameScreen
import com.example.game.HomeScreen
import com.example.game.IsleMatchGame
import com.example.game.shop.config.EconomyConfig
import com.example.game.shop.model.PriceType
import com.example.game.shop.model.ShopBundleConfig
import com.example.game.shop.model.ShopItemConfig
import com.example.ui.GameButton
import com.example.ui.ResourceCounter
import com.example.utils.TextureFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

enum class ShopCategory {
    FEATURED,
    BOOSTERS,
    MATERIALS,
    ENERGY,
    BUNDLES
}

class ShopScreen(
    game: IsleMatchGame,
    private val screenManager: ScreenManager
) : BaseGameScreen(game) {

    private val stage = Stage(FitViewport(GameConstants.VIRTUAL_WIDTH, GameConstants.VIRTUAL_HEIGHT))
    private val font = BitmapFont()
    private val scope = CoroutineScope(Dispatchers.IO)

    private var activeCategory = ShopCategory.BOOSTERS

    private lateinit var coinCounter: ResourceCounter
    private lateinit var gemCounter: ResourceCounter
    private lateinit var energyCounter: ResourceCounter

    private val itemsTable = Table()
    private var activePopupTable: Table? = null

    init {
        Gdx.input.inputProcessor = stage
        font.data.setScale(1.1f)

        setupTopHUD()
        setupCategoryTabs()
        setupContentArea()

        observePlayerData()
    }

    private fun observePlayerData() {
        scope.launch {
            GameDataProvider.cachedPlayer.collectLatest { p ->
                if (p != null) {
                    coinCounter.setValue(p.coins.toString())
                    gemCounter.setValue(p.gems.toString())
                    energyCounter.setValue("${p.energy}/${p.maxEnergy}")
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
        tabTable.setPosition(20f, GameConstants.VIRTUAL_HEIGHT - 130f)
        tabTable.setSize(GameConstants.VIRTUAL_WIDTH - 40f, 50f)

        val tabFont = BitmapFont()
        tabFont.data.setScale(0.9f)
        val tabStyle = Label.LabelStyle(tabFont, Color.WHITE)

        val categories = listOf(
            Pair(ShopCategory.FEATURED, "FEATURED"),
            Pair(ShopCategory.BOOSTERS, "BOOSTERS"),
            Pair(ShopCategory.MATERIALS, "MATERIALS"),
            Pair(ShopCategory.ENERGY, "ENERGY"),
            Pair(ShopCategory.BUNDLES, "BUNDLES")
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
                    refreshItems()
                }
            )
            tabTable.add(catBtn).size(110f, 44f).padRight(4f)
        }

        stage.addActor(tabTable)
    }

    private fun setupContentArea() {
        val scrollPane = ScrollPane(itemsTable)
        scrollPane.setPosition(20f, 30f)
        scrollPane.setSize(GameConstants.VIRTUAL_WIDTH - 40f, GameConstants.VIRTUAL_HEIGHT - 180f)
        scrollPane.setScrollingDisabled(true, false)

        stage.addActor(scrollPane)
        refreshItems()
    }

    private fun refreshItems() {
        itemsTable.clear()
        itemsTable.top().pad(10f)

        val playerLevel = GameDataProvider.cachedPlayer.value?.playerLevel ?: 1

        val nameFont = BitmapFont()
        nameFont.data.setScale(1.1f)
        val nameStyle = Label.LabelStyle(nameFont, Color.WHITE)

        val descFont = BitmapFont()
        descFont.data.setScale(0.85f)
        val descStyle = Label.LabelStyle(descFont, Color(0.8f, 0.85f, 0.9f, 1f))

        val btnFont = BitmapFont()
        btnFont.data.setScale(1.0f)
        val btnStyle = Label.LabelStyle(btnFont, Color.WHITE)

        when (activeCategory) {
            ShopCategory.FEATURED -> {
                val featuredItems = EconomyConfig.getShopItems().take(3)
                for (item in featuredItems) {
                    renderItemCard(item, playerLevel, nameStyle, descStyle, btnStyle)
                }
            }
            ShopCategory.BOOSTERS -> {
                val items = EconomyConfig.getShopItems().filter { it.itemType == "BOOSTER" }
                for (item in items) {
                    renderItemCard(item, playerLevel, nameStyle, descStyle, btnStyle)
                }
            }
            ShopCategory.MATERIALS -> {
                val items = EconomyConfig.getShopItems().filter { it.itemType == "MATERIAL" }
                for (item in items) {
                    renderItemCard(item, playerLevel, nameStyle, descStyle, btnStyle)
                }
            }
            ShopCategory.ENERGY -> {
                val items = EconomyConfig.getShopItems().filter { it.itemType == "ENERGY" }
                for (item in items) {
                    renderItemCard(item, playerLevel, nameStyle, descStyle, btnStyle)
                }
            }
            ShopCategory.BUNDLES -> {
                val bundles = EconomyConfig.getShopBundles()
                for (bundle in bundles) {
                    renderBundleCard(bundle, playerLevel, nameStyle, descStyle, btnStyle)
                }
            }
        }
    }

    private fun renderItemCard(
        item: ShopItemConfig,
        playerLevel: Int,
        nameStyle: Label.LabelStyle,
        descStyle: Label.LabelStyle,
        btnStyle: Label.LabelStyle
    ) {
        val cardTable = Table()
        cardTable.background = TextureFactory.createRoundedPanel(
            width = 580, height = 110,
            fillColor = Color(0.12f, 0.16f, 0.24f, 0.95f),
            borderColor = GameConstants.COLOR_GOLD,
            borderThickness = 3,
            cornerRadius = 16
        )
        cardTable.pad(12f)

        val itemIcon = Image(TextureFactory.createIcon(item.iconName, 54))

        val infoTable = Table()
        infoTable.left()
        val nameLbl = Label(item.displayName, nameStyle)
        val descLbl = Label(item.description, descStyle)
        infoTable.add(nameLbl).left().row()
        infoTable.add(descLbl).left().width(320f)

        val isUnlocked = playerLevel >= item.unlockLevel
        val priceIcon = when (item.priceType) {
            PriceType.COINS -> TextureFactory.createIcon("coin", 24)
            PriceType.GEMS -> TextureFactory.createIcon("gem", 24)
            PriceType.ENERGY -> TextureFactory.createIcon("energy", 24)
            PriceType.FREE -> null
        }

        val btnText = if (isUnlocked) {
            if (item.priceType == PriceType.FREE) "FREE" else "${item.priceAmount}"
        } else {
            "Lv. ${item.unlockLevel}"
        }

        val btnColor = if (isUnlocked) GameConstants.COLOR_PLAY_BUTTON else Color(0.4f, 0.4f, 0.45f, 1f)

        val buyBtn = GameButton(
            text = btnText,
            iconDrawable = if (isUnlocked) priceIcon else null,
            bgColor = btnColor,
            labelStyle = btnStyle,
            onClick = {
                if (isUnlocked) {
                    showPurchaseConfirmation(item)
                }
            }
        )

        cardTable.add(itemIcon).size(54f, 54f).padRight(12f)
        cardTable.add(infoTable).expandX().left()
        cardTable.add(buyBtn).size(130f, 48f)

        itemsTable.add(cardTable).size(580f, 110f).padBottom(12f).row()
    }

    private fun renderBundleCard(
        bundle: ShopBundleConfig,
        playerLevel: Int,
        nameStyle: Label.LabelStyle,
        descStyle: Label.LabelStyle,
        btnStyle: Label.LabelStyle
    ) {
        val cardTable = Table()
        cardTable.background = TextureFactory.createRoundedPanel(
            width = 580, height = 110,
            fillColor = Color(0.12f, 0.16f, 0.24f, 0.95f),
            borderColor = GameConstants.COLOR_GOLD,
            borderThickness = 3,
            cornerRadius = 16
        )
        cardTable.pad(12f)

        val iconName = when (bundle.itemId) {
            "HAMMER" -> "hammer"
            "SWAP" -> "swap"
            "SHUFFLE" -> "shuffle"
            "EXTRA_MOVES" -> "extra_moves"
            else -> "gift"
        }
        val itemIcon = Image(TextureFactory.createIcon(iconName, 54))

        val infoTable = Table()
        infoTable.left()
        val nameLbl = Label(bundle.displayName, nameStyle)
        val totalQty = bundle.quantity + bundle.bonusQuantity
        val descLbl = Label("Pack of $totalQty items (${bundle.quantity} + ${bundle.bonusQuantity} Bonus!)", descStyle)
        infoTable.add(nameLbl).left().row()
        infoTable.add(descLbl).left().width(320f)

        val priceIcon = when (bundle.priceType) {
            PriceType.COINS -> TextureFactory.createIcon("coin", 24)
            PriceType.GEMS -> TextureFactory.createIcon("gem", 24)
            PriceType.FREE -> null
            PriceType.ENERGY -> TextureFactory.createIcon("energy", 24)
        }

        val buyBtn = GameButton(
            text = "${bundle.priceAmount}",
            iconDrawable = priceIcon,
            bgColor = GameConstants.COLOR_PLAY_BUTTON,
            labelStyle = btnStyle,
            onClick = {
                showBundleConfirmation(bundle)
            }
        )

        cardTable.add(itemIcon).size(54f, 54f).padRight(12f)
        cardTable.add(infoTable).expandX().left()
        cardTable.add(buyBtn).size(130f, 48f)

        itemsTable.add(cardTable).size(580f, 110f).padBottom(12f).row()
    }

    private fun showPurchaseConfirmation(item: ShopItemConfig) {
        if (activePopupTable != null) return

        val popTable = Table()
        popTable.background = TextureFactory.createRoundedPanel(
            width = 440, height = 280,
            fillColor = Color(0.1f, 0.15f, 0.25f, 0.96f),
            borderColor = GameConstants.COLOR_GOLD,
            borderThickness = 4
        )
        popTable.setSize(440f, 280f)
        popTable.setPosition(GameConstants.VIRTUAL_WIDTH / 2f - 220f, GameConstants.VIRTUAL_HEIGHT / 2f - 140f)
        popTable.pad(18f)

        val titleFont = BitmapFont()
        titleFont.data.setScale(1.3f)
        val titleLbl = Label("CONFIRM PURCHASE", Label.LabelStyle(titleFont, GameConstants.COLOR_GOLD))

        val msgFont = BitmapFont()
        msgFont.data.setScale(1.0f)
        val msgLbl = Label("Buy ${item.displayName} for ${item.priceAmount} ${item.priceType}?", Label.LabelStyle(msgFont, Color.WHITE))

        val btnFont = BitmapFont()
        btnFont.data.setScale(1.0f)
        val btnStyle = Label.LabelStyle(btnFont, Color.WHITE)

        val confirmBtn = GameButton("BUY NOW", bgColor = GameConstants.COLOR_PLAY_BUTTON, labelStyle = btnStyle) {
            popTable.remove()
            activePopupTable = null
            executeItemPurchase(item)
        }

        val cancelBtn = GameButton("CANCEL", bgColor = Color(0.4f, 0.4f, 0.48f, 1f), labelStyle = btnStyle) {
            popTable.remove()
            activePopupTable = null
        }

        popTable.add(titleLbl).padBottom(16f).colspan(2).row()
        popTable.add(msgLbl).padBottom(24f).colspan(2).row()
        popTable.add(cancelBtn).size(150f, 45f).padRight(12f)
        popTable.add(confirmBtn).size(160f, 45f)

        activePopupTable = popTable
        stage.addActor(popTable)
    }

    private fun showBundleConfirmation(bundle: ShopBundleConfig) {
        if (activePopupTable != null) return

        val popTable = Table()
        popTable.background = TextureFactory.createRoundedPanel(
            width = 440, height = 280,
            fillColor = Color(0.1f, 0.15f, 0.25f, 0.96f),
            borderColor = GameConstants.COLOR_GOLD,
            borderThickness = 4
        )
        popTable.setSize(440f, 280f)
        popTable.setPosition(GameConstants.VIRTUAL_WIDTH / 2f - 220f, GameConstants.VIRTUAL_HEIGHT / 2f - 140f)
        popTable.pad(18f)

        val titleFont = BitmapFont()
        titleFont.data.setScale(1.3f)
        val titleLbl = Label("CONFIRM BUNDLE", Label.LabelStyle(titleFont, GameConstants.COLOR_GOLD))

        val msgFont = BitmapFont()
        msgFont.data.setScale(1.0f)
        val msgLbl = Label("Buy ${bundle.displayName} for ${bundle.priceAmount} ${bundle.priceType}?", Label.LabelStyle(msgFont, Color.WHITE))

        val btnFont = BitmapFont()
        btnFont.data.setScale(1.0f)
        val btnStyle = Label.LabelStyle(btnFont, Color.WHITE)

        val confirmBtn = GameButton("BUY NOW", bgColor = GameConstants.COLOR_PLAY_BUTTON, labelStyle = btnStyle) {
            popTable.remove()
            activePopupTable = null
            executeBundlePurchase(bundle)
        }

        val cancelBtn = GameButton("CANCEL", bgColor = Color(0.4f, 0.4f, 0.48f, 1f), labelStyle = btnStyle) {
            popTable.remove()
            activePopupTable = null
        }

        popTable.add(titleLbl).padBottom(16f).colspan(2).row()
        popTable.add(msgLbl).padBottom(24f).colspan(2).row()
        popTable.add(cancelBtn).size(150f, 45f).padRight(12f)
        popTable.add(confirmBtn).size(160f, 45f)

        activePopupTable = popTable
        stage.addActor(popTable)
    }

    private fun executeItemPurchase(item: ShopItemConfig) {
        scope.launch {
            val result = GameDataProvider.shopRepository.purchaseItem(item)
            launch(Dispatchers.Main) {
                showToast(result.message)
            }
        }
    }

    private fun executeBundlePurchase(bundle: ShopBundleConfig) {
        scope.launch {
            val result = GameDataProvider.shopRepository.purchaseBundle(bundle)
            launch(Dispatchers.Main) {
                showToast(result.message)
            }
        }
    }

    private fun showToast(msg: String) {
        val toastTable = Table()
        toastTable.background = TextureFactory.createRoundedPanel(
            width = 380, height = 70,
            fillColor = Color(0.12f, 0.18f, 0.28f, 0.95f),
            borderColor = GameConstants.COLOR_GOLD,
            borderThickness = 2
        )
        toastTable.setSize(380f, 70f)
        toastTable.setPosition(GameConstants.VIRTUAL_WIDTH / 2f - 190f, 120f)

        val msgFont = BitmapFont()
        msgFont.data.setScale(1.0f)
        val msgLbl = Label(msg, Label.LabelStyle(msgFont, Color.WHITE))
        toastTable.add(msgLbl).center()

        stage.addActor(toastTable)

        stage.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence(
            com.badlogic.gdx.scenes.scene2d.actions.Actions.delay(1.8f),
            com.badlogic.gdx.scenes.scene2d.actions.Actions.run { toastTable.remove() }
        ))
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
