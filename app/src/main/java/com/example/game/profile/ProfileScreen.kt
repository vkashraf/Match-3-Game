package com.example.game.profile

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
import com.example.data.local.entity.PlayerEntity
import com.example.data.repository.PlayerLevelConfig
import com.example.game.BaseGameScreen
import com.example.game.HomeScreen
import com.example.game.IsleMatchGame
import com.example.game.daily.DailyRewardScreen
import com.example.game.mission.MissionScreen
import com.example.game.stats.StatisticsScreen
import com.example.ui.GameButton
import com.example.utils.TextureFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProfileScreen(
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
    private val profileCard = Table()
    private var currentPlayer: PlayerEntity? = null

    override fun show() {
        Gdx.input.inputProcessor = stage
        setupLayout()
        observeData()
    }

    private fun observeData() {
        scope.launch {
            GameDataProvider.cachedPlayer.collectLatest { player ->
                if (player != null) {
                    currentPlayer = player
                    updateProfileCard()
                }
            }
        }
    }

    private fun setupLayout() {
        rootTable.setFillParent(true)
        rootTable.pad(20f)

        val titleStyle = Label.LabelStyle(fontTitle, GameConstants.COLOR_GOLD)
        rootTable.add(Label("PLAYER PROFILE", titleStyle)).padBottom(20f).row()

        // PROFILE HEADER CARD
        rootTable.add(profileCard).size(420f, 150f).padBottom(24f).row()

        // BUTTONS MENU
        val menuTable = Table()
        val btnStyle = Label.LabelStyle(fontMedium, Color.WHITE)

        val statsBtn = GameButton("STATISTICS", iconDrawable = TextureFactory.createIcon("star", 28), bgColor = Color(0.18f, 0.32f, 0.52f, 0.95f), labelStyle = btnStyle) {
            screenManager.setScreen(StatisticsScreen(game, screenManager))
        }

        val achievementsBtn = GameButton("ACHIEVEMENTS", iconDrawable = TextureFactory.createIcon("gem", 28), bgColor = Color(0.18f, 0.32f, 0.52f, 0.95f), labelStyle = btnStyle) {
            screenManager.setScreen(MissionScreen(game, screenManager, activeTab = "ACHIEVEMENTS"))
        }

        val dailyBtn = GameButton("DAILY REWARDS", iconDrawable = TextureFactory.createIcon("coin", 28), bgColor = Color(0.18f, 0.32f, 0.52f, 0.95f), labelStyle = btnStyle) {
            screenManager.setScreen(DailyRewardScreen(game, screenManager))
        }

        val backBtn = GameButton("BACK TO ISLAND", bgColor = Color(0.25f, 0.35f, 0.5f, 0.9f), labelStyle = btnStyle) {
            screenManager.setScreen(HomeScreen(game, screenManager))
        }

        menuTable.add(statsBtn).size(340f, 48f).padBottom(10f).row()
        menuTable.add(achievementsBtn).size(340f, 48f).padBottom(10f).row()
        menuTable.add(dailyBtn).size(340f, 48f).padBottom(16f).row()
        menuTable.add(backBtn).size(260f, 44f)

        rootTable.add(menuTable).expandY().top()

        stage.addActor(rootTable)
    }

    private fun updateProfileCard() {
        val player = currentPlayer ?: return
        profileCard.clearChildren()

        profileCard.background = TextureFactory.createRoundedPanel(
            width = 420, height = 150,
            fillColor = Color(0.12f, 0.22f, 0.38f, 0.95f),
            borderColor = GameConstants.COLOR_GOLD,
            borderThickness = 3
        )
        profileCard.pad(16f)

        // Avatar
        val avatar = Image(TextureFactory.createIcon("star", 54))
        profileCard.add(avatar).size(54f).padRight(16f)

        // Info
        val infoTable = Table()
        val nameStyle = Label.LabelStyle(fontMedium, GameConstants.COLOR_GOLD)
        val detailStyle = Label.LabelStyle(fontSmall, Color.WHITE)

        infoTable.add(Label(player.playerName, nameStyle)).left().row()
        infoTable.add(Label("Level ${player.playerLevel}", detailStyle)).left().row()

        val reqXp = PlayerLevelConfig.getRequiredXpForLevel(player.playerLevel + 1)
        infoTable.add(Label("XP: ${player.xp} / $reqXp", detailStyle)).left().row()

        val starsText = "Total Stars: ${player.totalStars}"
        infoTable.add(Label(starsText, detailStyle)).left()

        profileCard.add(infoTable).expandX().left()
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
