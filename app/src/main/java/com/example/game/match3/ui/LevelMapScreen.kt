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
import com.example.game.BaseGameScreen
import com.example.game.HomeScreen
import com.example.game.IsleMatchGame
import com.example.game.match3.level.LevelConfigRepository
import com.example.ui.GameButton
import com.example.ui.ResourceCounter
import com.example.utils.TextureFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LevelMapScreen(
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

    private val levelProgressMap = mutableMapOf<Int, LevelProgressEntity>()
    private val scope = CoroutineScope(Dispatchers.IO)

    // Map geometry
    private val totalLevels = 100
    private val nodeSpacingY = 130f
    private val minCamY = GameConstants.VIRTUAL_HEIGHT / 2f
    private val maxCamY = 150f + (totalLevels - 1) * nodeSpacingY + 200f

    private var targetCamY = minCamY
    private var currentCamY = minCamY

    // Drag gesture tracking
    private var isDragging = false
    private var lastTouchY = 0f
    private var velocityY = 0f

    private lateinit var coinCounter: ResourceCounter
    private lateinit var gemCounter: ResourceCounter
    private lateinit var energyCounter: ResourceCounter
    private lateinit var starCounter: ResourceCounter

    init {
        font.data.setScale(1.1f)
        nodeFont.data.setScale(1.2f)

        setupTopHUD()
        observeLevelProgress()
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

        val backBtn = GameButton("BACK", bgColor = Color(0.2f, 0.3f, 0.5f, 1f), labelStyle = valueStyle) {
            screenManager.setScreen(HomeScreen(game, screenManager))
        }

        val initialPlayer = GameDataProvider.cachedPlayer.value
        val coinsStr = initialPlayer?.coins?.toString() ?: "0"
        val gemsStr = initialPlayer?.gems?.toString() ?: "0"
        val energyStr = "${initialPlayer?.energy ?: 0}/${initialPlayer?.maxEnergy ?: 5}"
        val starStr = "${initialPlayer?.totalStars ?: 0}"

        coinCounter = ResourceCounter("coin", coinsStr, valueStyle)
        gemCounter = ResourceCounter("gem", gemsStr, valueStyle)
        energyCounter = ResourceCounter("energy", energyStr, valueStyle)
        starCounter = ResourceCounter("star", starStr, valueStyle)

        topTable.add(backBtn).size(90f, 44f).padRight(12f)
        topTable.add(coinCounter).padRight(6f)
        topTable.add(gemCounter).padRight(6f)
        topTable.add(energyCounter).padRight(6f)
        topTable.add(starCounter).expandX().right()

        stage.addActor(topTable)
    }

    private fun observeLevelProgress() {
        scope.launch {
            GameDataProvider.levelRepository.allLevelsFlow.collect { levels ->
                var highestUnlocked = 1
                for (lvl in levels) {
                    levelProgressMap[lvl.levelId] = lvl
                    if (lvl.isUnlocked && lvl.levelId > highestUnlocked) {
                        highestUnlocked = lvl.levelId
                    }
                }
                // Focus camera on highest unlocked level
                val targetY = getNodePosition(highestUnlocked).y
                targetCamY = targetY.coerceIn(minCamY, maxCamY)
            }
        }
    }

    private fun getNodePosition(levelId: Int): Vector2 {
        val y = 150f + (levelId - 1) * nodeSpacingY
        val x = GameConstants.VIRTUAL_WIDTH / 2f + MathUtils.sin((levelId * 0.45f)) * 200f
        return Vector2(x, y)
    }

    private fun setupInputProcessor() {
        val inputAdapter = object : InputAdapter() {
            override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                if (screenY < 120) return false // Pass top HUD clicks to stage
                isDragging = true
                lastTouchY = screenY.toFloat()
                velocityY = 0f
                return true
            }

            override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
                if (!isDragging) return false
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
        val touchWorld = viewport.unproject(Vector3(screenX.toFloat(), screenY.toFloat(), 0f))
        val touch2D = Vector2(touchWorld.x, touchWorld.y)
        for (i in 1..totalLevels) {
            val nodePos = getNodePosition(i)
            if (touch2D.dst(nodePos) <= 45f) {
                onNodeTapped(i)
                break
            }
        }
    }

    private fun onNodeTapped(levelId: Int) {
        val config = LevelConfigRepository.getLevelConfig(levelId)
        val progress = levelProgressMap[levelId]
        val popup = LevelPreviewPopup(game, screenManager, config, progress) {
            // Closed
        }
        stage.addActor(popup)
    }

    override fun render(delta: Float) {
        // Smooth camera movement & inertia
        if (!isDragging) {
            targetCamY += velocityY
            velocityY *= 0.9f
            targetCamY = targetCamY.coerceIn(minCamY, maxCamY)
        }
        currentCamY = MathUtils.lerp(currentCamY, targetCamY, 0.2f)
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

        // Ocean background
        shapeRenderer.color = GameConstants.COLOR_OCEAN
        shapeRenderer.rect(0f, currentCamY - GameConstants.VIRTUAL_HEIGHT / 2f, GameConstants.VIRTUAL_WIDTH, GameConstants.VIRTUAL_HEIGHT)

        // Draw island patches along path
        shapeRenderer.color = GameConstants.COLOR_ISLAND_SAND
        for (i in 1..totalLevels step 3) {
            val pos = getNodePosition(i)
            shapeRenderer.circle(pos.x, pos.y, 140f)
        }

        shapeRenderer.color = GameConstants.COLOR_ISLAND_GREEN
        for (i in 1..totalLevels step 3) {
            val pos = getNodePosition(i)
            shapeRenderer.circle(pos.x, pos.y, 125f)
        }

        shapeRenderer.end()
    }

    private fun drawMapPathAndNodes() {
        shapeRenderer.projectionMatrix = camera.combined

        // 1. Path lines
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color(0.9f, 0.82f, 0.65f, 1f)
        for (i in 1 until totalLevels) {
            val p1 = getNodePosition(i)
            val p2 = getNodePosition(i + 1)
            shapeRenderer.rectLine(p1.x, p1.y, p2.x, p2.y, 16f)
        }

        // 2. Nodes
        for (i in 1..totalLevels) {
            val pos = getNodePosition(i)
            val progress = levelProgressMap[i]
            val isUnlocked = progress?.isUnlocked ?: (i == 1)
            val isCompleted = progress?.isCompleted ?: false

            val color = when {
                isCompleted -> Color(0.2f, 0.8f, 0.3f, 1f)
                isUnlocked -> GameConstants.COLOR_GOLD
                else -> Color(0.35f, 0.35f, 0.4f, 1f)
            }

            shapeRenderer.color = color
            shapeRenderer.circle(pos.x, pos.y, 36f)

            // Outer ring
            shapeRenderer.color = Color.WHITE
            shapeRenderer.circle(pos.x, pos.y, 40f)
            shapeRenderer.color = color
            shapeRenderer.circle(pos.x, pos.y, 34f)
        }
        shapeRenderer.end()

        // 3. Node labels & stars
        batch.projectionMatrix = camera.combined
        batch.begin()
        for (i in 1..totalLevels) {
            val pos = getNodePosition(i)
            val progress = levelProgressMap[i]
            val isUnlocked = progress?.isUnlocked ?: (i == 1)
            val stars = progress?.stars ?: 0

            val numStr = "$i"
            val numColor = if (isUnlocked) Color.WHITE else Color.LIGHT_GRAY
            nodeFont.color = numColor
            nodeFont.draw(batch, numStr, pos.x - 8f, pos.y + 10f)

            if (stars > 0) {
                val starStr = "★".repeat(stars)
                font.color = Color.YELLOW
                font.draw(batch, starStr, pos.x - 18f, pos.y - 12f)
            } else if (!isUnlocked) {
                font.color = Color.GRAY
                font.draw(batch, "LOCK", pos.x - 16f, pos.y - 12f)
            }
        }
        batch.end()
    }

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
    }
}
