package com.example.game.daily

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
import com.example.data.local.entity.DailyRewardStateEntity
import com.example.data.repository.DailyRewardData
import com.example.game.BaseGameScreen
import com.example.game.HomeScreen
import com.example.game.IsleMatchGame
import com.example.ui.GameButton
import com.example.ui.RewardPopup
import com.example.utils.TextureFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DailyRewardScreen(
    game: IsleMatchGame,
    private val screenManager: ScreenManager
) : BaseGameScreen(game) {

    private val viewport = FitViewport(GameConstants.VIRTUAL_WIDTH, GameConstants.VIRTUAL_HEIGHT)
    private val stage = Stage(viewport)

    private val fontTitle = BitmapFont().apply { data.setScale(1.4f) }
    private val fontSub = BitmapFont().apply { data.setScale(0.9f) }
    private val fontMedium = BitmapFont().apply { data.setScale(1.0f) }
    private val fontSmall = BitmapFont().apply { data.setScale(0.8f) }

    private val scope = CoroutineScope(Dispatchers.Main)

    private val rootTable = Table()
    private val cardsTable = Table()
    private var claimBtnContainer = Table()

    private var currentState: DailyRewardStateEntity? = null
    private var isClaimAvailable = false

    override fun show() {
        Gdx.input.inputProcessor = stage
        setupLayout()
        observeData()
    }

    private fun observeData() {
        scope.launch(Dispatchers.IO) {
            val repo = GameDataProvider.dailyRewardRepository
            val state = repo.getOrCreateState()
            val available = repo.isClaimAvailable()

            launch(Dispatchers.Main) {
                currentState = state
                isClaimAvailable = available
                updateUI()
            }
        }

        scope.launch {
            GameDataProvider.dailyRewardRepository.stateFlow.collectLatest { state ->
                if (state != null) {
                    val available = GameDataProvider.dailyRewardRepository.isClaimAvailable()
                    currentState = state
                    isClaimAvailable = available
                    updateUI()
                }
            }
        }
    }

    private fun setupLayout() {
        rootTable.setFillParent(true)
        rootTable.pad(16f)

        // HEADER
        val titleStyle = Label.LabelStyle(fontTitle, GameConstants.COLOR_GOLD)
        val subStyle = Label.LabelStyle(fontSub, Color(0.8f, 0.9f, 1f, 1f))

        rootTable.add(Label("DAILY REWARD", titleStyle)).padBottom(4f).row()
        rootTable.add(Label("Come back every day for bigger rewards!", subStyle)).padBottom(16f).row()

        // STREAK BADGE
        rootTable.add(createStreakPanel()).padBottom(20f).row()

        // CARDS GRID
        rootTable.add(cardsTable).expandY().fillX().padBottom(20f).row()

        // CLAIM BUTTON
        rootTable.add(claimBtnContainer).size(220f, 54f).padBottom(12f).row()

        // CLOSE BUTTON
        val btnStyle = Label.LabelStyle(fontMedium, Color.WHITE)
        val closeBtn = GameButton("BACK TO ISLAND", bgColor = Color(0.2f, 0.35f, 0.55f, 0.9f), labelStyle = btnStyle) {
            screenManager.setScreen(HomeScreen(game, screenManager))
        }
        rootTable.add(closeBtn).size(200f, 44f)

        stage.addActor(rootTable)
    }

    private fun createStreakPanel(): Table {
        val panel = Table()
        panel.background = TextureFactory.createRoundedPanel(
            width = 300, height = 50,
            fillColor = Color(0.12f, 0.22f, 0.38f, 0.9f),
            borderColor = GameConstants.COLOR_GOLD,
            borderThickness = 2
        )
        val labelStyle = Label.LabelStyle(fontMedium, GameConstants.COLOR_GOLD)
        val fireIcon = Image(TextureFactory.createIcon("star", 28))

        panel.add(fireIcon).size(24f).padRight(8f)
        val streakText = "CURRENT STREAK: ${currentState?.streakCount ?: 0} DAYS"
        panel.add(Label(streakText, labelStyle))

        return panel
    }

    private fun updateUI() {
        val state = currentState ?: return

        cardsTable.clearChildren()

        val currentCycleDay = state.currentDay
        val isClaimable = isClaimAvailable

        // 7-day cards
        DailyRewardData.REWARDS.forEach { config ->
            val card = Table()
            val dayNum = config.day

            val isCurrent = (dayNum == currentCycleDay)
            val isClaimed = if (isCurrent) !isClaimable else (dayNum < currentCycleDay)

            val fillColor = when {
                isCurrent && isClaimable -> Color(0.18f, 0.32f, 0.55f, 0.95f)
                isClaimed -> Color(0.12f, 0.18f, 0.28f, 0.8f)
                else -> Color(0.1f, 0.14f, 0.22f, 0.7f)
            }

            val borderColor = when {
                isCurrent -> GameConstants.COLOR_GOLD
                isClaimed -> Color(0.3f, 0.5f, 0.4f, 1f)
                else -> Color(0.25f, 0.35f, 0.5f, 0.6f)
            }

            card.background = TextureFactory.createRoundedPanel(
                width = 110, height = 110,
                fillColor = fillColor,
                borderColor = borderColor,
                borderThickness = if (isCurrent) 3 else 2
            )
            card.pad(6f)

            val dayTitleStyle = Label.LabelStyle(fontSmall, if (isCurrent) GameConstants.COLOR_GOLD else Color.WHITE)
            card.add(Label("DAY $dayNum", dayTitleStyle)).padBottom(4f).row()

            // Display primary reward icon
            val primaryReward = config.rewards.first()
            val iconDrawable = when (primaryReward.type) {
                com.example.game.reward.RewardType.COINS -> TextureFactory.createIcon("coin", 32)
                com.example.game.reward.RewardType.GEMS -> TextureFactory.createIcon("gem", 32)
                com.example.game.reward.RewardType.ENERGY -> TextureFactory.createIcon("energy", 32)
                com.example.game.reward.RewardType.BOOSTER -> TextureFactory.createIcon(primaryReward.itemId?.lowercase() ?: "hammer", 32)
                com.example.game.reward.RewardType.XP -> TextureFactory.createIcon("star", 32)
                else -> TextureFactory.createIcon(primaryReward.type.name.lowercase(), 32)
            }

            card.add(Image(iconDrawable)).size(32f).padBottom(4f).row()

            val rewardQtyText = primaryReward.getDisplayName()
            val qtyStyle = Label.LabelStyle(fontSmall, Color(0.9f, 0.95f, 1f, 1f))
            card.add(Label(rewardQtyText, qtyStyle)).row()

            if (isClaimed) {
                val checkLabel = Label("✔ CLAIMED", Label.LabelStyle(fontSmall, Color(0.3f, 0.9f, 0.4f, 1f)))
                card.add(checkLabel).padTop(2f)
            }

            cardsTable.add(card).size(110f, 110f).pad(6f)
            if (dayNum == 4) cardsTable.row() // Wrap 4 cards per row
        }

        // CLAIM BUTTON
        claimBtnContainer.clearChildren()
        val btnStyle = Label.LabelStyle(fontMedium, Color.WHITE)

        if (isClaimAvailable) {
            val claimBtn = GameButton("CLAIM TODAY", bgColor = GameConstants.COLOR_GOLD, labelStyle = btnStyle) {
                performClaim()
            }
            claimBtnContainer.add(claimBtn).fill()
        } else {
            val claimedBtn = GameButton("CLAIMED TODAY", bgColor = Color(0.4f, 0.4f, 0.4f, 0.8f), labelStyle = btnStyle) {
                // Already claimed
            }
            claimBtnContainer.add(claimedBtn).fill()
        }
    }

    private fun performClaim() {
        scope.launch(Dispatchers.IO) {
            val repo = GameDataProvider.dailyRewardRepository
            val (success, rewards) = repo.claimReward()

            if (success) {
                launch(Dispatchers.Main) {
                    RewardPopup.show(stage, "DAILY REWARD CLAIMED!", rewards)
                    isClaimAvailable = false
                    updateUI()
                }
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
        viewport.update(width, height, true)
    }

    override fun dispose() {
        stage.dispose()
    }
}
