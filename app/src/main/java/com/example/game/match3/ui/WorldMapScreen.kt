package com.example.game.match3.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.viewport.FitViewport
import com.example.core.GameConstants
import com.example.core.GameDataProvider
import com.example.core.ScreenManager
import com.example.data.local.entity.LevelProgressEntity
import com.example.data.local.entity.WorldProgressEntity
import com.example.game.BaseGameScreen
import com.example.game.HomeScreen
import com.example.game.IsleMatchGame
import com.example.game.world.LevelNodeConfig
import com.example.game.world.LevelNodeGenerator
import com.example.game.world.WorldConfig
import com.example.game.world.WorldConfigRepository
import com.example.game.world.ui.LevelIntroPopup
import com.example.game.world.ui.WorldCompletePopup
import com.example.game.world.ui.WorldSelectorPopup
import com.example.ui.GameButton
import com.example.ui.ResourceCounter
import com.example.utils.TextureFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

open class WorldMapScreen(
    game: IsleMatchGame,
    private val screenManager: ScreenManager
) : BaseGameScreen(game) {

    private val camera = OrthographicCamera()
    private val viewport = FitViewport(GameConstants.VIRTUAL_WIDTH, GameConstants.VIRTUAL_HEIGHT, camera)
    private val stage = Stage(FitViewport(GameConstants.VIRTUAL_WIDTH, GameConstants.VIRTUAL_HEIGHT))
    private val batch = SpriteBatch()
    private val shapeRenderer = ShapeRenderer()
    private val font = BitmapFont()
    private val nodeFont = BitmapFont()
    private val titleFont = BitmapFont()

    private val levelProgressMap = mutableMapOf<Int, LevelProgressEntity>()
    private val worldProgressMap = mutableMapOf<Int, WorldProgressEntity>()
    private val worldStarsMap = mutableMapOf<Int, Int>()

    private val scope = CoroutineScope(Dispatchers.IO)

    // Map geometry
    private val totalLevels = WorldConfigRepository.TOTAL_LEVELS
    private val minCamY = GameConstants.VIRTUAL_HEIGHT / 2f
    private val maxCamY = 180f + (totalLevels - 1) * 130f + 250f

    private var targetCamY = minCamY
    private var currentCamY = minCamY

    // Drag gesture tracking
    private var isDragging = false
    private var lastTouchY = 0f
    private var velocityY = 0f

    private lateinit var worldSelectorBtn: GameButton
    private lateinit var coinCounter: ResourceCounter
    private lateinit var gemCounter: ResourceCounter
    private lateinit var energyCounter: ResourceCounter
    private lateinit var starCounter: ResourceCounter

    private var activePopup: Table? = null

    init {
        font.data.setScale(1.1f)
        nodeFont.data.setScale(1.2f)
        titleFont.data.setScale(1.4f)

        setupTopHUD()
        setupBottomNavigation()
        observeData()
        setupInputProcessor()
    }

    private fun setupTopHUD() {
        val topTable = Table()
        topTable.top().left()
        topTable.setFillParent(true)
        topTable.pad(16f)

        val fontSmall = BitmapFont()
        fontSmall.data.setScale(1.0f)
        val valueStyle = Label.LabelStyle(fontSmall, Color.WHITE)

        val homeBtn = GameButton("HOME", bgColor = Color(0.2f, 0.35f, 0.55f, 1f), labelStyle = valueStyle) {
            screenManager.setScreen(HomeScreen(game, screenManager))
        }

        worldSelectorBtn = GameButton(
            text = "W1: Meadow ▾",
            bgColor = GameConstants.COLOR_HUD_NAVY,
            borderColor = GameConstants.COLOR_GOLD,
            labelStyle = valueStyle,
            onClick = { showWorldSelectorPopup() }
        )

        val initialPlayer = GameDataProvider.cachedPlayer.value
        val coinsStr = initialPlayer?.coins?.toString() ?: "0"
        val gemsStr = initialPlayer?.gems?.toString() ?: "0"
        val energyStr = "${initialPlayer?.energy ?: 0}/${initialPlayer?.maxEnergy ?: 5}"
        val starStr = "${initialPlayer?.totalStars ?: 0}"

        coinCounter = ResourceCounter("coin", coinsStr, valueStyle)
        gemCounter = ResourceCounter("gem", gemsStr, valueStyle)
        energyCounter = ResourceCounter("energy", energyStr, valueStyle)
        starCounter = ResourceCounter("star", starStr, valueStyle)

        topTable.add(homeBtn).size(85f, 44f).padRight(6f)
        topTable.add(worldSelectorBtn).size(140f, 44f).padRight(8f)
        topTable.add(coinCounter).padRight(4f)
        topTable.add(gemCounter).padRight(4f)
        topTable.add(energyCounter).padRight(4f)
        topTable.add(starCounter).expandX().right()

        stage.addActor(topTable)
    }

    private fun setupBottomNavigation() {
        val bottomTable = Table()
        bottomTable.bottom().center()
        bottomTable.setFillParent(true)
        bottomTable.padBottom(16f)

        val fontMedium = BitmapFont()
        fontMedium.data.setScale(1.0f)
        val mediumStyle = Label.LabelStyle(fontMedium, Color.WHITE)

        val shopBtn = GameButton(
            text = "SHOP",
            bgColor = GameConstants.COLOR_HUD_NAVY,
            borderColor = Color(0.2f, 0.45f, 0.75f, 1f),
            labelStyle = mediumStyle,
            onClick = { screenManager.setScreen(com.example.game.shop.ui.ShopScreen(game, screenManager)) }
        )

        val homeNavBtn = GameButton(
            text = "ISLAND",
            bgColor = GameConstants.COLOR_PLAY_BUTTON,
            labelStyle = mediumStyle,
            onClick = { screenManager.setScreen(HomeScreen(game, screenManager)) }
        )

        val invBtn = GameButton(
            text = "INVENTORY",
            bgColor = GameConstants.COLOR_HUD_NAVY,
            borderColor = Color(0.2f, 0.45f, 0.75f, 1f),
            labelStyle = mediumStyle,
            onClick = { screenManager.setScreen(com.example.game.shop.ui.InventoryScreen(game, screenManager)) }
        )

        bottomTable.add(shopBtn).size(130f, 52f).padRight(12f)
        bottomTable.add(homeNavBtn).size(150f, 58f).padRight(12f)
        bottomTable.add(invBtn).size(130f, 52f)

        stage.addActor(bottomTable)
    }

    private fun observeData() {
        scope.launch {
            GameDataProvider.levelRepository.allLevelsFlow.collectLatest { levels ->
                var highestUnlocked = 1
                worldStarsMap.clear()

                for (lvl in levels) {
                    levelProgressMap[lvl.levelId] = lvl
                    if (lvl.isUnlocked && lvl.levelId > highestUnlocked) {
                        highestUnlocked = lvl.levelId
                    }

                    val worldId = ((lvl.levelId - 1) / 10) + 1
                    worldStarsMap[worldId] = (worldStarsMap[worldId] ?: 0) + lvl.stars
                }

                // Focus camera on highest unlocked level node
                val targetPos = LevelNodeGenerator.getNodePosition(highestUnlocked)
                targetCamY = targetPos.y.coerceIn(minCamY, maxCamY)

                // Update World Selector button text based on focused camera world
                val currentWorld = WorldConfigRepository.getWorldForLevel(highestUnlocked)
                Gdx.app.postRunnable {
                    worldSelectorBtn.setText("W${currentWorld.worldId}: ${currentWorld.worldName.take(8)}.. ▾")
                }
            }
        }

        scope.launch {
            GameDataProvider.worldRepository.allWorldsFlow.collectLatest { worlds ->
                for (w in worlds) {
                    worldProgressMap[w.worldId] = w
                }
            }
        }
    }

    private fun setupInputProcessor() {
        val inputAdapter = object : InputAdapter() {
            override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                if (activePopup != null) return false
                if (screenY < 120 || screenY > Gdx.graphics.height - 120) return false
                isDragging = true
                lastTouchY = screenY.toFloat()
                velocityY = 0f
                return true
            }

            override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
                if (!isDragging || activePopup != null) return false
                val deltaY = (screenY - lastTouchY) * (GameConstants.VIRTUAL_HEIGHT / Gdx.graphics.height.toFloat())
                targetCamY += deltaY
                targetCamY = targetCamY.coerceIn(minCamY, maxCamY)
                velocityY = deltaY
                lastTouchY = screenY.toFloat()
                return true
            }

            override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                if (isDragging) {
                    isDragging = false
                    if (Math.abs(velocityY) < 10f) {
                        handleMapTap(screenX, screenY)
                    }
                    return true
                }
                return false
            }
        }

        val multiplexer = InputMultiplexer()
        multiplexer.addProcessor(stage)
        multiplexer.addProcessor(inputAdapter)
        Gdx.input.inputProcessor = multiplexer
    }

    private fun handleMapTap(screenX: Int, screenY: Int) {
        if (activePopup != null) return
        val touchWorld = viewport.unproject(Vector3(screenX.toFloat(), screenY.toFloat(), 0f))
        val touch2D = Vector2(touchWorld.x, touchWorld.y)

        for (i in 1..totalLevels) {
            val nodeConfig = LevelNodeGenerator.getNodeConfig(i)
            val nodePos = Vector2(nodeConfig.positionX, nodeConfig.positionY)
            val radius = if (nodeConfig.isBossLevel) 55f else 42f

            if (touch2D.dst(nodePos) <= radius) {
                val progress = levelProgressMap[i]
                val isUnlocked = progress?.isUnlocked ?: (i == 1)
                if (isUnlocked) {
                    showLevelIntroPopup(nodeConfig, progress)
                }
                break
            }
        }
    }

    private fun showWorldSelectorPopup() {
        if (activePopup != null) return
        val popup = WorldSelectorPopup(
            worldProgressMap = worldProgressMap,
            worldStarsMap = worldStarsMap,
            onWorldSelected = { world ->
                activePopup = null
                scrollToWorld(world)
            },
            onClose = { activePopup = null }
        )
        activePopup = popup
        stage.addActor(popup)
    }

    private fun showLevelIntroPopup(nodeConfig: LevelNodeConfig, progress: LevelProgressEntity?) {
        if (activePopup != null) return
        val popup = LevelIntroPopup(
            game = game,
            screenManager = screenManager,
            nodeConfig = nodeConfig,
            levelProgress = progress,
            onClose = { activePopup = null }
        )
        activePopup = popup
        stage.addActor(popup)
    }

    private fun scrollToWorld(worldConfig: WorldConfig) {
        val startNodePos = LevelNodeGenerator.getNodePosition(worldConfig.startLevel)
        targetCamY = startNodePos.y.coerceIn(minCamY, maxCamY)
        worldSelectorBtn.setText("W${worldConfig.worldId}: ${worldConfig.worldName.take(8)}.. ▾")
    }

    override fun render(delta: Float) {
        // Smooth camera movement & inertia
        if (!isDragging) {
            targetCamY += velocityY
            velocityY *= 0.9f
            targetCamY = targetCamY.coerceIn(minCamY, maxCamY)
        }
        currentCamY = MathUtils.lerp(currentCamY, targetCamY, 0.22f)
        camera.position.set(GameConstants.VIRTUAL_WIDTH / 2f, currentCamY, 0f)
        camera.update()

        // Update HUD resource counters
        GameDataProvider.cachedPlayer.value?.let { p ->
            coinCounter.setValue(p.coins.toString())
            gemCounter.setValue(p.gems.toString())
            energyCounter.setValue("${p.energy}/${p.maxEnergy}")
            starCounter.setValue(p.totalStars.toString())
        }

        drawMapBackground()
        drawMapPathAndNodes()

        stage.act(delta)
        stage.draw()
    }

    private fun drawMapBackground() {
        shapeRenderer.projectionMatrix = camera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // Draw 10 World Zones along Y axis
        val worldHeight = 1300f
        for (worldId in 1..WorldConfigRepository.TOTAL_WORLDS) {
            val config = WorldConfigRepository.getWorld(worldId)
            val startY = 100f + (worldId - 1) * worldHeight

            // World Zone Background Gradient / Color fill
            shapeRenderer.color = config.bgBottomColor
            shapeRenderer.rect(0f, startY, GameConstants.VIRTUAL_WIDTH, worldHeight)

            // Terrain patches along nodes for this world
            shapeRenderer.color = config.islandColor
            val startLvl = config.startLevel
            val endLvl = config.endLevel
            for (lvl in startLvl..endLvl) {
                val pos = LevelNodeGenerator.getNodePosition(lvl)
                shapeRenderer.circle(pos.x, pos.y, if (lvl % 10 == 0) 110f else 85f)
            }
        }

        shapeRenderer.end()
    }

    private fun drawMapPathAndNodes() {
        shapeRenderer.projectionMatrix = camera.combined

        // 1. Path connecting level 1 to 100
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        for (i in 1 until totalLevels) {
            val p1 = LevelNodeGenerator.getNodePosition(i)
            val p2 = LevelNodeGenerator.getNodePosition(i + 1)
            val worldId = ((i - 1) / 10) + 1
            val worldConfig = WorldConfigRepository.getWorld(worldId)

            val p1Progress = levelProgressMap[i]
            val isP1Completed = p1Progress?.isCompleted ?: false

            shapeRenderer.color = if (isP1Completed) GameConstants.COLOR_GOLD else worldConfig.pathColor
            shapeRenderer.rectLine(p1.x, p1.y, p2.x, p2.y, 16f)
        }

        // 2. Render Level Nodes
        for (i in 1..totalLevels) {
            val nodeConfig = LevelNodeGenerator.getNodeConfig(i)
            val pos = Vector2(nodeConfig.positionX, nodeConfig.positionY)
            val progress = levelProgressMap[i]
            val isUnlocked = progress?.isUnlocked ?: (i == 1)
            val isCompleted = progress?.isCompleted ?: false
            val stars = progress?.stars ?: 0

            val baseRadius = when {
                nodeConfig.isBossLevel -> 48f
                nodeConfig.isCheckpoint -> 42f
                else -> 36f
            }

            val fillColor = when {
                isCompleted -> Color(0.18f, 0.75f, 0.32f, 1f)
                isUnlocked -> GameConstants.COLOR_PLAY_BUTTON
                else -> Color(0.35f, 0.38f, 0.45f, 1f)
            }

            val borderColor = when {
                stars == 3 -> GameConstants.COLOR_GOLD
                isBossLevel(nodeConfig) -> Color.RED
                isUnlocked -> Color.WHITE
                else -> Color.GRAY
            }

            // Outer border ring
            shapeRenderer.color = borderColor
            shapeRenderer.circle(pos.x, pos.y, baseRadius + 5f)

            // Fill circle
            shapeRenderer.color = fillColor
            shapeRenderer.circle(pos.x, pos.y, baseRadius)
        }
        shapeRenderer.end()

        // 3. Render Node labels, boss titles, and star ratings
        batch.projectionMatrix = camera.combined
        batch.begin()

        for (i in 1..totalLevels) {
            val nodeConfig = LevelNodeGenerator.getNodeConfig(i)
            val pos = Vector2(nodeConfig.positionX, nodeConfig.positionY)
            val progress = levelProgressMap[i]
            val isUnlocked = progress?.isUnlocked ?: (i == 1)
            val stars = progress?.stars ?: 0

            if (nodeConfig.isBossLevel) {
                nodeFont.color = Color.RED
                nodeFont.draw(batch, "👑 BOSS", pos.x - 30f, pos.y + 68f)
            } else if (nodeConfig.isCheckpoint) {
                font.color = GameConstants.COLOR_GOLD
                font.draw(batch, "🚩 CHECKPOINT", pos.x - 42f, pos.y + 56f)
            }

            val numStr = "$i"
            nodeFont.color = if (isUnlocked) Color.WHITE else Color.LIGHT_GRAY
            nodeFont.draw(batch, numStr, pos.x - (if (i >= 100) 18f else if (i >= 10) 12f else 6f), pos.y + 10f)

            if (stars > 0) {
                val starStr = "★".repeat(stars)
                font.color = Color.YELLOW
                font.draw(batch, starStr, pos.x - (stars * 7f), pos.y - 12f)
            } else if (!isUnlocked) {
                font.color = Color.LIGHT_GRAY
                font.draw(batch, "LOCK", pos.x - 16f, pos.y - 12f)
            }
        }

        batch.end()
    }

    private fun isBossLevel(nodeConfig: LevelNodeConfig): Boolean = nodeConfig.isBossLevel

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, false)
        stage.viewport.update(width, height, true)
    }

    override fun dispose() {
        stage.dispose()
        batch.dispose()
        shapeRenderer.dispose()
        font.dispose()
        nodeFont.dispose()
        titleFont.dispose()
    }
}
