package com.example.game.stats

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.viewport.FitViewport
import com.example.core.GameConstants
import com.example.core.GameDataProvider
import com.example.core.ScreenManager
import com.example.data.local.entity.PlayerStatsEntity
import com.example.game.BaseGameScreen
import com.example.game.HomeScreen
import com.example.game.IsleMatchGame
import com.example.ui.GameButton
import com.example.utils.TextureFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class StatisticsScreen(
    game: IsleMatchGame,
    private val screenManager: ScreenManager
) : BaseGameScreen(game) {

    private val viewport = FitViewport(GameConstants.VIRTUAL_WIDTH, GameConstants.VIRTUAL_HEIGHT)
    private val stage = Stage(viewport)

    private val fontTitle = BitmapFont().apply { data.setScale(1.4f) }
    private val fontMedium = BitmapFont().apply { data.setScale(1.0f) }
    private val fontSmall = BitmapFont().apply { data.setScale(0.85f) }

    private val scope = CoroutineScope(Dispatchers.Main)

    private val rootTable = Table()
    private val gridTable = Table()
    private var statsState: PlayerStatsEntity? = null

    override fun show() {
        Gdx.input.inputProcessor = stage
        setupLayout()
        observeData()
    }

    private fun observeData() {
        scope.launch {
            GameDataProvider.statsRepository.statsFlow.collectLatest { stats ->
                if (stats != null) {
                    statsState = stats
                    updateGrid()
                }
            }
        }
    }

    private fun setupLayout() {
        rootTable.setFillParent(true)
        rootTable.pad(16f)

        val titleStyle = Label.LabelStyle(fontTitle, GameConstants.COLOR_GOLD)
        rootTable.add(Label("PLAYER STATISTICS", titleStyle)).padBottom(20f).row()

        rootTable.add(gridTable).expandY().fillX().padBottom(20f).row()

        val btnStyle = Label.LabelStyle(fontMedium, Color.WHITE)
        val backBtn = GameButton("BACK", bgColor = Color(0.2f, 0.35f, 0.55f, 0.9f), labelStyle = btnStyle) {
            screenManager.setScreen(HomeScreen(game, screenManager))
        }
        rootTable.add(backBtn).size(180f, 44f)

        stage.addActor(rootTable)
    }

    private fun updateGrid() {
        val s = statsState ?: return
        gridTable.clearChildren()

        val items = listOf(
            Triple("LEVELS WON", "${s.totalLevelsWon}", "star"),
            Triple("LEVELS PLAYED", "${s.totalLevelsPlayed}", "star"),
            Triple("OBSTACLES DESTROYED", "${s.totalObstaclesDestroyed}", "hammer"),
            Triple("TILES CLEARED", "${s.totalTilesCleared}", "gem"),
            Triple("BOOSTERS USED", "${s.totalBoostersUsed}", "shuffle"),
            Triple("BUILDINGS BUILT", "${s.totalBuildingsBuilt}", "house"),
            Triple("TOTAL STARS", "${s.totalStarsEarned}", "star"),
            Triple("LONGEST STREAK", "${s.longestDailyStreak} Days", "star")
        )

        var count = 0
        items.forEach { (label, value, iconName) ->
            val box = Table()
            box.background = TextureFactory.createRoundedPanel(
                width = 200, height = 70,
                fillColor = Color(0.12f, 0.22f, 0.38f, 0.9f),
                borderColor = GameConstants.COLOR_GOLD,
                borderThickness = 2
            )
            box.pad(8f)

            val icon = TextureFactory.createIcon(iconName, 28)
            box.add(Image(icon)).size(24f).padRight(8f)

            val details = Table()
            val titleStyle = Label.LabelStyle(fontSmall, Color(0.8f, 0.9f, 1f, 1f))
            val valueStyle = Label.LabelStyle(fontMedium, Color.WHITE)

            details.add(Label(label, titleStyle)).left().row()
            details.add(Label(value, valueStyle)).left()

            box.add(details).expandX().left()

            gridTable.add(box).size(210f, 70f).pad(6f)
            count++
            if (count % 2 == 0) gridTable.row()
        }
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0.08f, 0.12f, 0.22f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        stage.act(delta)
        stage.draw()
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
    }

    override fun dispose() {
        stage.dispose()
    }
}
