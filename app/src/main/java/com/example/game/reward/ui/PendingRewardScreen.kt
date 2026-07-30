package com.example.game.reward.ui

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
import com.example.data.local.entity.PendingRewardEntity
import com.example.game.BaseGameScreen
import com.example.game.HomeScreen
import com.example.game.IsleMatchGame
import com.example.ui.GameButton
import com.example.ui.ResourceCounter
import com.example.utils.TextureFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PendingRewardScreen(
    game: IsleMatchGame,
    private val screenManager: ScreenManager
) : BaseGameScreen(game) {

    private val stage = Stage(FitViewport(GameConstants.VIRTUAL_WIDTH, GameConstants.VIRTUAL_HEIGHT))
    private val font = BitmapFont()
    private val scope = CoroutineScope(Dispatchers.IO)

    private val itemsTable = Table()

    private lateinit var coinCounter: ResourceCounter
    private lateinit var gemCounter: ResourceCounter
    private lateinit var energyCounter: ResourceCounter

    init {
        Gdx.input.inputProcessor = stage
        font.data.setScale(1.1f)

        setupTopHUD()
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
            GameDataProvider.pendingRewardRepository.pendingRewardsFlow.collectLatest { rewards ->
                launch(Dispatchers.Main) {
                    refreshList(rewards)
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

    private fun setupContentArea() {
        val titleFont = BitmapFont()
        titleFont.data.setScale(1.4f)
        val titleLbl = Label("PENDING REWARDS", Label.LabelStyle(titleFont, GameConstants.COLOR_GOLD))
        titleLbl.setPosition(GameConstants.VIRTUAL_WIDTH / 2f - 140f, GameConstants.VIRTUAL_HEIGHT - 90f)
        stage.addActor(titleLbl)

        val scrollPane = ScrollPane(itemsTable)
        scrollPane.setPosition(20f, 100f)
        scrollPane.setSize(GameConstants.VIRTUAL_WIDTH - 40f, GameConstants.VIRTUAL_HEIGHT - 200f)
        scrollPane.setScrollingDisabled(true, false)

        stage.addActor(scrollPane)

        val btnFont = BitmapFont()
        btnFont.data.setScale(1.1f)
        val btnStyle = Label.LabelStyle(btnFont, Color.WHITE)

        val claimAllBtn = GameButton("CLAIM ALL REWARDS", bgColor = GameConstants.COLOR_PLAY_BUTTON, labelStyle = btnStyle) {
            scope.launch {
                GameDataProvider.pendingRewardRepository.claimAllPendingRewards()
            }
        }
        claimAllBtn.setPosition(GameConstants.VIRTUAL_WIDTH / 2f - 130f, 30f)
        claimAllBtn.setSize(260f, 52f)

        stage.addActor(claimAllBtn)
    }

    private fun refreshList(rewards: List<PendingRewardEntity>) {
        itemsTable.clear()
        itemsTable.top().pad(10f)

        val nameFont = BitmapFont()
        nameFont.data.setScale(1.1f)
        val nameStyle = Label.LabelStyle(nameFont, Color.WHITE)

        val descFont = BitmapFont()
        descFont.data.setScale(0.85f)
        val descStyle = Label.LabelStyle(descFont, Color(0.8f, 0.85f, 0.9f, 1f))

        val btnFont = BitmapFont()
        btnFont.data.setScale(1.0f)
        val btnStyle = Label.LabelStyle(btnFont, Color.WHITE)

        if (rewards.isEmpty()) {
            val emptyTable = Table()
            emptyTable.pad(40f)

            val titleLbl = Label("NO PENDING REWARDS", Label.LabelStyle(nameFont, GameConstants.COLOR_GOLD))
            val descLbl = Label("All rewards have been claimed! Play levels and complete goals for more.", descStyle)

            emptyTable.add(titleLbl).padBottom(12f).row()
            emptyTable.add(descLbl).center()

            itemsTable.add(emptyTable).expandX().center().padTop(60f)
            return
        }

        for (item in rewards) {
            val cardTable = Table()
            cardTable.background = TextureFactory.createRoundedPanel(
                width = 580, height = 90,
                fillColor = Color(0.12f, 0.16f, 0.24f, 0.95f),
                borderColor = GameConstants.COLOR_GOLD,
                borderThickness = 2,
                cornerRadius = 14
            )
            cardTable.pad(10f)

            val itemIcon = Image(TextureFactory.createIcon(item.rewardType.lowercase(), 48))

            val infoTable = Table()
            infoTable.left()
            val nameLbl = Label("${item.amount} ${item.rewardType}", nameStyle)
            val descLbl = Label("Source: ${item.sourceType}", descStyle)
            infoTable.add(nameLbl).left().row()
            infoTable.add(descLbl).left().width(310f)

            val claimBtn = GameButton("CLAIM", bgColor = GameConstants.COLOR_GOLD, labelStyle = btnStyle) {
                scope.launch {
                    GameDataProvider.pendingRewardRepository.claimPendingReward(item.pendingRewardId)
                }
            }

            cardTable.add(itemIcon).size(48f, 48f).padRight(12f)
            cardTable.add(infoTable).expandX().left()
            cardTable.add(claimBtn).size(110f, 42f)

            itemsTable.add(cardTable).size(580f, 90f).padBottom(10f).row()
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
