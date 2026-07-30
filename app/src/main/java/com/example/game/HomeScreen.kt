package com.example.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.viewport.FitViewport
import com.example.core.GameConstants
import com.example.core.GameDataProvider
import com.example.core.ScreenManager
import com.example.data.local.entity.BuildingEntity
import com.example.data.local.entity.BuildingPlotEntity
import com.example.data.local.entity.DecorationEntity
import com.example.data.local.entity.IslandZoneEntity
import com.example.game.island.building.ConstructionManager
import com.example.game.island.camera.IslandCameraController
import com.example.game.island.rating.IslandRatingManager
import com.example.game.island.renderer.BuildingRenderer
import com.example.game.island.renderer.IslandRenderer
import com.example.game.island.ui.BuildPanel
import com.example.game.island.ui.BuildingInfoPanel
import com.example.game.island.ui.EditModePanel
import com.example.game.island.ui.LockedPlotPopup
import com.example.game.island.ui.OfflineSummaryPopup
import com.example.game.island.ui.ZoneUnlockPopup
import com.example.ui.FeatureButton
import com.example.ui.GameButton
import com.example.ui.PlayerBadge
import com.example.ui.ResourceCounter
import com.example.utils.TextureFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeScreen(
    game: IsleMatchGame,
    private val screenManager: ScreenManager
) : BaseGameScreen(game) {

    private val stage = Stage(FitViewport(GameConstants.VIRTUAL_WIDTH, GameConstants.VIRTUAL_HEIGHT))
    private val batch = SpriteBatch()
    private val shapeRenderer = ShapeRenderer()
    private val font = BitmapFont()

    private val islandCamera = OrthographicCamera(GameConstants.VIRTUAL_WIDTH, GameConstants.VIRTUAL_HEIGHT).apply {
        position.set(GameConstants.VIRTUAL_WIDTH / 2f + 100f, GameConstants.VIRTUAL_HEIGHT / 2f + 100f, 0f)
        update()
    }

    private val cameraController = IslandCameraController(islandCamera)
    private val islandRenderer = IslandRenderer(shapeRenderer, batch, font)
    private val buildingRenderer = BuildingRenderer(batch, shapeRenderer, font)

    private val scope = CoroutineScope(Dispatchers.IO)
    private var plotsList = listOf<BuildingPlotEntity>()
    private var zonesMap = mapOf<Int, IslandZoneEntity>()
    private var buildingsMap = mapOf<String, BuildingEntity>()
    private var decorationsList = listOf<DecorationEntity>()

    private lateinit var playerBadge: PlayerBadge
    private lateinit var coinCounter: ResourceCounter
    private lateinit var gemCounter: ResourceCounter
    private lateinit var energyCounter: ResourceCounter
    private lateinit var ratingLabel: Label
    private var debugOverlayTable: Table? = null

    private var activePopupOpen = false
    private var isEditMode = false

    init {
        font.data.setScale(1.2f)

        setupInput()
        setupTopHUD()
        setupLeftSideButtons()
        setupBottomNavigation()
        setupDebugOverlay()

        observeIslandData()
        observeBadges()
        checkOfflineProduction()

        stage.root.getColor().a = 0f
        stage.root.addAction(Actions.fadeIn(0.4f))
    }

    private fun checkOfflineProduction() {
        activePopupOpen = true
        OfflineSummaryPopup(stage, font) {
            activePopupOpen = false
        }
    }

    private fun observeBadges() {
        com.example.game.badge.BadgeManager.refreshBadges()
        scope.launch {
            com.example.game.badge.BadgeManager.badgeState.collectLatest { state ->
                // Badges updated
            }
        }
    }

    private fun observeIslandData() {
        scope.launch {
            GameDataProvider.islandRepository.allPlotsFlow.collectLatest { plots ->
                plotsList = plots
                updateRating()
            }
        }
        scope.launch {
            GameDataProvider.islandRepository.allZonesFlow.collectLatest { zones ->
                zonesMap = zones.associateBy { it.zoneId }
                updateRating()
            }
        }
        scope.launch {
            GameDataProvider.buildingRepository.allBuildingsFlow.collectLatest { buildings ->
                val bMap = buildings.associateBy { it.buildingId }
                val now = System.currentTimeMillis()
                for (building in buildings) {
                    val finished = ConstructionManager.checkAndFinishConstruction(building, now)
                    if (finished != null) {
                        GameDataProvider.buildingRepository.updateBuilding(finished)
                    }
                }
                buildingsMap = bMap
                updateRating()
            }
        }
        scope.launch {
            GameDataProvider.decorationRepository.allDecorationsFlow.collectLatest { decs ->
                decorationsList = decs
                updateRating()
            }
        }
    }

    private fun updateRating() {
        val score = IslandRatingManager.calculateBeautyScore(
            buildingsMap.values.toList(),
            decorationsList,
            zonesMap.values.toList()
        )
        stage.root?.let {
            if (::ratingLabel.isInitialized) {
                ratingLabel.setText("⭐ $score")
            }
        }
    }

    private fun setupInput() {
        val gestureProcessor = object : InputAdapter() {
            private var lastTouch = Vector2()
            private var isDragging = false

            override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                lastTouch.set(screenX.toFloat(), screenY.toFloat())
                isDragging = false
                return false
            }

            override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
                if (activePopupOpen) return false
                val deltaX = screenX - lastTouch.x
                val deltaY = screenY - lastTouch.y

                if (deltaX * deltaX + deltaY * deltaY > 16f) {
                    isDragging = true
                }

                cameraController.pan(deltaX, deltaY)
                lastTouch.set(screenX.toFloat(), screenY.toFloat())
                return true
            }

            override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                if (activePopupOpen || isDragging) return false

                val touchVector = stage.viewport.unproject(Vector2(screenX.toFloat(), screenY.toFloat()))
                val clickedPlot = buildingRenderer.getPlotAt(touchVector.x, touchVector.y, plotsList)

                if (clickedPlot != null) {
                    onPlotClicked(clickedPlot)
                    return true
                }
                return false
            }

            override fun scrolled(amountX: Float, amountY: Float): Boolean {
                if (!activePopupOpen) {
                    cameraController.zoom(amountY * 0.1f)
                    return true
                }
                return false
            }
        }

        val multiplexer = InputMultiplexer()
        multiplexer.addProcessor(stage)
        multiplexer.addProcessor(gestureProcessor)
        Gdx.input.inputProcessor = multiplexer
    }

    private fun onPlotClicked(plot: BuildingPlotEntity) {
        if (activePopupOpen) return

        val zone = zonesMap[plot.zoneId]
        if (zone != null && !zone.isUnlocked) {
            activePopupOpen = true
            ZoneUnlockPopup(stage, font, zone) {
                activePopupOpen = false
            }
            return
        }

        if (!plot.isUnlocked) {
            activePopupOpen = true
            LockedPlotPopup(stage, font, plot) {
                activePopupOpen = false
            }
            return
        }

        val building = plot.buildingId?.let { buildingsMap[it] }
        if (building != null) {
            activePopupOpen = true
            BuildingInfoPanel(stage, font, building) {
                activePopupOpen = false
            }
        } else {
            activePopupOpen = true
            BuildPanel(stage, font, plot) {
                activePopupOpen = false
            }
        }
    }

    private fun setupTopHUD() {
        val topTable = Table()
        topTable.top().left()
        topTable.setFillParent(true)
        topTable.pad(16f)

        val fontSmall = BitmapFont()
        fontSmall.data.setScale(1.1f)
        val valueStyle = Label.LabelStyle(fontSmall, Color.WHITE)
        val ratingStyle = Label.LabelStyle(fontSmall, GameConstants.COLOR_GOLD)

        val initialPlayer = GameDataProvider.cachedPlayer.value
        val initialCoins = initialPlayer?.coins?.toString() ?: "1000"
        val initialGems = initialPlayer?.gems?.toString() ?: "50"
        val initialEnergy = "${initialPlayer?.energy ?: 5}/${initialPlayer?.maxEnergy ?: 5}"
        val initialLevel = initialPlayer?.playerLevel ?: 1

        playerBadge = PlayerBadge(playerLevel = initialLevel, levelStyle = valueStyle)
        playerBadge.addListener(object : com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            override fun clicked(event: com.badlogic.gdx.scenes.scene2d.InputEvent?, x: Float, y: Float) {
                screenManager.setScreen(com.example.game.profile.ProfileScreen(game, screenManager))
            }
        })
        coinCounter = ResourceCounter("coin", initialCoins, valueStyle)
        gemCounter = ResourceCounter("gem", initialGems, valueStyle)
        energyCounter = ResourceCounter("energy", initialEnergy, valueStyle)
        ratingLabel = Label("⭐ 100", ratingStyle)

        val menuIcon = TextureFactory.createIcon("menu", 36)
        val menuButtonStyle = Label.LabelStyle(font, Color.WHITE)
        val menuButton = GameButton(
            iconDrawable = menuIcon,
            bgColor = GameConstants.COLOR_HUD_NAVY,
            borderColor = Color(0.25f, 0.5f, 0.8f, 1f),
            labelStyle = menuButtonStyle,
            onClick = {
                screenManager.setScreen(PlaceholderScreen(game, screenManager, "Settings"))
            }
        )

        topTable.add(playerBadge).padRight(6f)
        topTable.add(coinCounter).padRight(4f)
        topTable.add(gemCounter).padRight(4f)
        topTable.add(energyCounter).padRight(4f)
        topTable.add(ratingLabel).padRight(6f)
        topTable.add(menuButton).size(48f, 48f).expandX().right()

        stage.addActor(topTable)
    }

    private fun setupDebugOverlay() {
        val debugTable = Table()
        debugTable.background = TextureFactory.createRoundedPanel(
            width = 300,
            height = 200,
            fillColor = Color(0.1f, 0.15f, 0.25f, 0.92f),
            borderColor = GameConstants.COLOR_GOLD,
            borderThickness = 2
        )
        debugTable.setSize(480f, 140f)
        debugTable.setPosition(GameConstants.VIRTUAL_WIDTH / 2f - 240f, 160f)
        debugTable.pad(12f)
        debugTable.isVisible = false

        val btnFont = BitmapFont()
        btnFont.data.setScale(0.9f)
        val btnStyle = Label.LabelStyle(btnFont, Color.WHITE)

        val addCoinBtn = GameButton("+500 Coins", bgColor = Color(0.18f, 0.55f, 0.25f, 1f), labelStyle = btnStyle) {
            GameDataProvider.addCoins(500)
        }
        val subCoinBtn = GameButton("-100 Coins", bgColor = Color(0.7f, 0.2f, 0.2f, 1f), labelStyle = btnStyle) {
            GameDataProvider.spendCoins(100)
        }
        val addGemBtn = GameButton("+100 Gems", bgColor = Color(0.2f, 0.4f, 0.7f, 1f), labelStyle = btnStyle) {
            GameDataProvider.addGems(100)
        }
        val addEnergyBtn = GameButton("+1 Energy", bgColor = Color(0.6f, 0.3f, 0.7f, 1f), labelStyle = btnStyle) {
            GameDataProvider.addEnergy(1)
        }
        val addXpBtn = GameButton("+100 XP", bgColor = Color(0.8f, 0.5f, 0.1f, 1f), labelStyle = btnStyle) {
            GameDataProvider.addXp(100)
        }
        val addStarBtn = GameButton("+5 Stars", bgColor = Color(0.9f, 0.7f, 0.1f, 1f), labelStyle = btnStyle) {
            GameDataProvider.addStars(5)
        }

        debugTable.add(addCoinBtn).size(110f, 40f).pad(4f)
        debugTable.add(subCoinBtn).size(110f, 40f).pad(4f)
        debugTable.add(addGemBtn).size(110f, 40f).pad(4f).row()
        debugTable.add(addEnergyBtn).size(110f, 40f).pad(4f)
        debugTable.add(addXpBtn).size(110f, 40f).pad(4f)
        debugTable.add(addStarBtn).size(110f, 40f).pad(4f)

        debugOverlayTable = debugTable
        stage.addActor(debugTable)
    }

    private fun setupLeftSideButtons() {
        val leftTable = Table()
        leftTable.left().center()
        leftTable.setFillParent(true)
        leftTable.padLeft(16f).padTop(120f)

        val fontSmall = BitmapFont()
        fontSmall.data.setScale(0.9f)
        val labelStyle = Label.LabelStyle(fontSmall, Color.WHITE)

        val badgeFont = BitmapFont()
        badgeFont.data.setScale(0.8f)
        val badgeStyle = Label.LabelStyle(badgeFont, Color.WHITE)

        val dailyBtn = FeatureButton("gift", "Daily", "", labelStyle, badgeStyle) {
            screenManager.setScreen(com.example.game.daily.DailyRewardScreen(game, screenManager))
        }

        val missionsBtn = FeatureButton("clipboard", "Missions", "", labelStyle, badgeStyle) {
            screenManager.setScreen(com.example.game.mission.MissionScreen(game, screenManager, activeTab = "DAILY"))
        }

        val eventsBtn = FeatureButton("events", "Achievements", "", labelStyle, badgeStyle) {
            screenManager.setScreen(com.example.game.mission.MissionScreen(game, screenManager, activeTab = "ACHIEVEMENTS"))
        }

        val storageBtn = FeatureButton("inventory", "Storage", "", labelStyle, badgeStyle) {
            if (!activePopupOpen) {
                activePopupOpen = true
                com.example.game.island.ui.StoragePanel(stage, font) {
                    activePopupOpen = false
                }
            }
        }

        val editBtn = FeatureButton("shop", "Edit Mode", "", labelStyle, badgeStyle) {
            if (!activePopupOpen) {
                activePopupOpen = true
                isEditMode = true
                EditModePanel(stage, font, onRotateSelected = {}, onRemoveSelected = {}, onDone = {
                    activePopupOpen = false
                    isEditMode = false
                })
            }
        }

        val debugBtn = FeatureButton("menu", "Debug", "", labelStyle, badgeStyle) {
            debugOverlayTable?.let { it.isVisible = !it.isVisible }
        }

        leftTable.add(dailyBtn).size(105f, 50f).padBottom(8f).row()
        leftTable.add(missionsBtn).size(105f, 50f).padBottom(8f).row()
        leftTable.add(eventsBtn).size(105f, 50f).padBottom(8f).row()
        leftTable.add(storageBtn).size(105f, 50f).padBottom(8f).row()
        leftTable.add(editBtn).size(105f, 50f).padBottom(8f).row()
        leftTable.add(debugBtn).size(105f, 50f)

        stage.addActor(leftTable)
    }

    private fun setupBottomNavigation() {
        val bottomTable = Table()
        bottomTable.bottom().center()
        bottomTable.setFillParent(true)
        bottomTable.padBottom(24f)

        val fontBig = BitmapFont()
        fontBig.data.setScale(1.8f)
        val playStyle = Label.LabelStyle(fontBig, Color.WHITE)

        val fontMedium = BitmapFont()
        fontMedium.data.setScale(1.1f)
        val mediumStyle = Label.LabelStyle(fontMedium, Color.WHITE)

        val shopBtn = GameButton(
            text = "SHOP",
            iconDrawable = TextureFactory.createIcon("shop", 32),
            bgColor = GameConstants.COLOR_HUD_NAVY,
            borderColor = Color(0.2f, 0.45f, 0.75f, 1f),
            labelStyle = mediumStyle,
            onClick = {
                screenManager.setScreen(com.example.game.shop.ui.ShopScreen(game, screenManager))
            }
        )

        val playBtn = GameButton(
            text = "PLAY",
            bgColor = GameConstants.COLOR_PLAY_BUTTON,
            borderColor = Color(0.75f, 0.35f, 0.05f, 1f),
            labelStyle = playStyle,
            onClick = {
                screenManager.setScreen(com.example.game.match3.ui.LevelMapScreen(game, screenManager))
            }
        )

        val inventoryBtn = GameButton(
            text = "INVENTORY",
            iconDrawable = TextureFactory.createIcon("inventory", 32),
            bgColor = GameConstants.COLOR_HUD_NAVY,
            borderColor = Color(0.2f, 0.45f, 0.75f, 1f),
            labelStyle = mediumStyle,
            onClick = {
                screenManager.setScreen(com.example.game.shop.ui.InventoryScreen(game, screenManager))
            }
        )

        bottomTable.add(shopBtn).size(150f, 65f).padRight(16f)
        bottomTable.add(playBtn).size(220f, 85f).padRight(16f)
        bottomTable.add(inventoryBtn).size(160f, 65f)

        stage.addActor(bottomTable)
    }

    override fun render(delta: Float) {
        cameraController.update(delta)

        GameDataProvider.cachedPlayer.value?.let { p ->
            coinCounter.setValue(p.coins.toString())
            gemCounter.setValue(p.gems.toString())
            energyCounter.setValue("${p.energy}/${p.maxEnergy}")
            playerBadge.setLevel(p.playerLevel)
        }

        val combinedMatrix = stage.viewport.camera.combined
        batch.projectionMatrix = combinedMatrix
        shapeRenderer.projectionMatrix = combinedMatrix

        islandRenderer.renderBackground(delta, zonesMap)
        buildingRenderer.renderPlotsAndBuildings(plotsList, buildingsMap, decorationsList)

        stage.act(delta)
        stage.draw()
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun dispose() {
        stage.dispose()
        batch.dispose()
        shapeRenderer.dispose()
        font.dispose()
    }
}
