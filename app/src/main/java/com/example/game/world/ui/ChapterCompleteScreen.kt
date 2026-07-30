package com.example.game.world.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.viewport.FitViewport
import com.example.core.GameConstants
import com.example.core.GameDataProvider
import com.example.core.ScreenManager
import com.example.game.BaseGameScreen
import com.example.game.HomeScreen
import com.example.game.IsleMatchGame
import com.example.game.reward.Reward
import com.example.game.reward.RewardManager
import com.example.game.reward.RewardType
import com.example.ui.GameButton
import com.example.ui.PlayerBadge
import com.example.ui.ResourceCounter
import com.example.utils.TextureFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChapterCompleteScreen(
    game: IsleMatchGame,
    private val screenManager: ScreenManager
) : BaseGameScreen(game) {

    private val stage = Stage(FitViewport(GameConstants.VIRTUAL_WIDTH, GameConstants.VIRTUAL_HEIGHT))
    private val batch = SpriteBatch()
    private val scope = CoroutineScope(Dispatchers.IO)

    private lateinit var playerBadge: PlayerBadge
    private lateinit var coinCounter: ResourceCounter
    private lateinit var gemCounter: ResourceCounter
    private lateinit var energyCounter: ResourceCounter

    init {
        setupTopHUD()
        setupContent()
        grantChapterRewards()

        stage.root.getColor().a = 0f
        stage.root.addAction(Actions.fadeIn(0.5f))
    }

    private fun setupTopHUD() {
        val topTable = Table()
        topTable.top().left()
        topTable.setFillParent(true)
        topTable.pad(16f)

        val counterFont = BitmapFont()
        counterFont.data.setScale(0.9f)
        val counterStyle = Label.LabelStyle(counterFont, Color.WHITE)

        playerBadge = PlayerBadge(1, counterStyle)
        coinCounter = ResourceCounter("coin", "0", counterStyle)
        gemCounter = ResourceCounter("gem", "0", counterStyle)
        energyCounter = ResourceCounter("energy", "0", counterStyle)

        topTable.add(playerBadge).padRight(8f)
        topTable.add(coinCounter).padRight(6f)
        topTable.add(gemCounter).padRight(6f)
        topTable.add(energyCounter)

        stage.addActor(topTable)
    }

    private fun setupContent() {
        val contentTable = Table()
        contentTable.background = TextureFactory.createRoundedPanel(
            width = 500,
            height = 560,
            fillColor = Color(0.12f, 0.18f, 0.35f, 0.98f),
            borderColor = GameConstants.COLOR_GOLD,
            borderThickness = 4
        )
        contentTable.setSize(500f, 560f)
        contentTable.setPosition(
            GameConstants.VIRTUAL_WIDTH / 2f - 250f,
            GameConstants.VIRTUAL_HEIGHT / 2f - 280f
        )
        contentTable.pad(24f)

        val titleFont = BitmapFont()
        titleFont.data.setScale(2.0f)
        val titleStyle = Label.LabelStyle(titleFont, GameConstants.COLOR_GOLD)
        contentTable.add(Label("🏆 CONGRATULATIONS! 🏆", titleStyle)).padBottom(12f).row()

        val subFont = BitmapFont()
        subFont.data.setScale(1.4f)
        val subStyle = Label.LabelStyle(subFont, Color.WHITE)
        contentTable.add(Label("CHAPTER 1 COMPLETED!", subStyle)).padBottom(16f).row()

        val fontMedium = BitmapFont()
        fontMedium.data.setScale(1.1f)
        val infoStyle = Label.LabelStyle(fontMedium, Color.WHITE)
        val goldStyle = Label.LabelStyle(fontMedium, GameConstants.COLOR_GOLD)

        contentTable.add(Label("You cleared all 100 Match-3 Levels!", infoStyle)).padBottom(6f).row()
        contentTable.add(Label("All 10 Worlds Mastered!", goldStyle)).padBottom(20f).row()

        val rewardTable = Table()
        rewardTable.background = TextureFactory.createRoundedPanel(
            width = 440,
            height = 120,
            fillColor = Color(0.15f, 0.35f, 0.22f, 0.95f),
            borderColor = Color.GREEN,
            borderThickness = 2
        )
        rewardTable.pad(12f)

        rewardTable.add(Label("GRAND CHAPTER REWARDS:", goldStyle)).padBottom(8f).row()
        rewardTable.add(
            Label("+5,000 Coins   +2,000 XP   +50 Gems", infoStyle)
        ).row()

        contentTable.add(rewardTable).padBottom(24f).row()

        val btnFont = BitmapFont()
        btnFont.data.setScale(1.1f)
        val btnStyle = Label.LabelStyle(btnFont, Color.WHITE)

        val btnTable = Table()

        val mapBtn = GameButton("WORLD MAP", bgColor = Color(0.2f, 0.5f, 0.8f, 1f), labelStyle = btnStyle) {
            screenManager.setScreen(com.example.game.match3.ui.WorldMapScreen(game, screenManager))
        }

        val homeBtn = GameButton("HOME ISLAND", bgColor = GameConstants.COLOR_PLAY_BUTTON, labelStyle = btnStyle) {
            screenManager.setScreen(HomeScreen(game, screenManager))
        }

        btnTable.add(mapBtn).size(180f, 54f).padRight(16f)
        btnTable.add(homeBtn).size(180f, 54f)

        contentTable.add(btnTable)

        stage.addActor(contentTable)
    }

    private fun grantChapterRewards() {
        scope.launch {
            RewardManager.grantRewards(
                listOf(
                    Reward(RewardType.COINS, quantity = 5000),
                    Reward(RewardType.XP, quantity = 2000),
                    Reward(RewardType.GEMS, quantity = 50)
                )
            )
        }
    }

    override fun render(delta: Float) {
        GameDataProvider.cachedPlayer.value?.let { p ->
            coinCounter.setValue(p.coins.toString())
            gemCounter.setValue(p.gems.toString())
            energyCounter.setValue("${p.energy}/${p.maxEnergy}")
            playerBadge.setLevel(p.playerLevel)
        }

        stage.act(delta)
        stage.draw()
    }

    override fun dispose() {
        batch.dispose()
        stage.dispose()
    }
}
