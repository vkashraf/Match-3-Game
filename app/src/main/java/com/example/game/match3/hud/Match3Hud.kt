package com.example.game.match3.hud

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.example.core.GameConstants
import com.example.game.match3.level.LevelGoal
import com.example.game.match3.level.MoveCounter
import com.example.game.match3.score.ScoreManager
import com.example.ui.GameButton
import com.example.utils.TextureFactory

class Match3Hud(
    private val stage: Stage,
    private val moveCounter: MoveCounter,
    private val scoreManager: ScoreManager,
    private val goals: List<LevelGoal>,
    private val onSettingsClick: () -> Unit
) {

    private val font: BitmapFont = BitmapFont().apply { data.setScale(1.1f) }
    private val fontSmall: BitmapFont = BitmapFont().apply { data.setScale(0.85f) }

    private val titleStyle = Label.LabelStyle(fontSmall, Color(0.8f, 0.9f, 1f, 1f))
    private val valueStyle = Label.LabelStyle(font, Color.WHITE)

    private val movesLabel = Label(moveCounter.movesRemaining.toString(), valueStyle)
    private val scoreLabel = Label(scoreManager.currentScore.toString(), valueStyle)

    init {
        setupTopBar()
        setupBottomBar()
    }

    private fun setupTopBar() {
        val topTable = Table()
        topTable.top()
        topTable.setSize(GameConstants.VIRTUAL_WIDTH, 140f)
        topTable.setPosition(0f, GameConstants.VIRTUAL_HEIGHT - 150f)
        topTable.pad(12f)

        // MOVES Panel
        val movesBox = Table()
        movesBox.background = TextureFactory.createRoundedPanel(
            width = 110,
            height = 70,
            fillColor = Color(0.12f, 0.22f, 0.38f, 0.9f),
            borderColor = GameConstants.COLOR_GOLD,
            borderThickness = 2
        )
        movesBox.add(Label("MOVES", titleStyle)).padTop(4f).row()
        movesBox.add(movesLabel).padBottom(4f)

        // GOALS Panel
        val goalsBox = Table()
        goalsBox.background = TextureFactory.createRoundedPanel(
            width = 220,
            height = 70,
            fillColor = Color(0.12f, 0.22f, 0.38f, 0.9f),
            borderColor = GameConstants.COLOR_GOLD,
            borderThickness = 2
        )
        goalsBox.add(Label("TARGETS", titleStyle)).colspan(goals.size).padTop(4f).row()

        goals.forEach { goal ->
            val goalItem = Table()
            val icon = when (goal.goalType.uppercase()) {
                "BLUE_DROP" -> TextureFactory.createTileTexture("BLUE_DROP", 28)
                "GREEN_LEAF" -> TextureFactory.createTileTexture("GREEN_LEAF", 28)
                else -> TextureFactory.createIcon("coin", 28)
            }
            goalItem.add(Image(icon)).size(24f).padRight(4f)
            goalItem.add(Label("${goal.currentAmount}/${goal.targetAmount}", valueStyle))
            goalsBox.add(goalItem).pad(4f)
        }

        // SCORE Panel
        val scoreBox = Table()
        scoreBox.background = TextureFactory.createRoundedPanel(
            width = 110,
            height = 70,
            fillColor = Color(0.12f, 0.22f, 0.38f, 0.9f),
            borderColor = GameConstants.COLOR_GOLD,
            borderThickness = 2
        )
        scoreBox.add(Label("SCORE", titleStyle)).padTop(4f).row()
        scoreBox.add(scoreLabel).padBottom(4f)

        topTable.add(movesBox).size(110f, 70f).padRight(12f)
        topTable.add(goalsBox).size(220f, 70f).padRight(12f)
        topTable.add(scoreBox).size(110f, 70f)

        topTable.row().padTop(8f)

        // STAR PROGRESS BAR PLACEHOLDER
        val starBar = Table()
        starBar.background = TextureFactory.createRoundedPanel(
            width = 460,
            height = 24,
            fillColor = Color(0.08f, 0.12f, 0.22f, 0.8f),
            borderColor = Color(0.3f, 0.5f, 0.7f, 1f),
            borderThickness = 2,
            cornerRadius = 12
        )

        val star1 = Image(TextureFactory.createTileTexture("YELLOW_STAR", 20))
        val star2 = Image(TextureFactory.createTileTexture("YELLOW_STAR", 20))
        val star3 = Image(TextureFactory.createTileTexture("YELLOW_STAR", 20))

        starBar.add(Label("STARS:", titleStyle)).padLeft(12f).padRight(12f)
        starBar.add(star1).size(18f).padRight(60f)
        starBar.add(star2).size(18f).padRight(60f)
        starBar.add(star3).size(18f)

        topTable.add(starBar).colspan(3).size(460f, 26f)

        stage.addActor(topTable)
    }

    private fun setupBottomBar() {
        val bottomTable = Table()
        bottomTable.bottom()
        bottomTable.setSize(GameConstants.VIRTUAL_WIDTH, 90f)
        bottomTable.setPosition(0f, 15f)

        // Boosters
        val hammerBtn = GameButton("", iconDrawable = TextureFactory.createBoosterIcon("hammer", 36), bgColor = Color(0.15f, 0.25f, 0.4f, 0.9f), labelStyle = valueStyle) {}
        val rocketBtn = GameButton("", iconDrawable = TextureFactory.createBoosterIcon("rocket", 36), bgColor = Color(0.15f, 0.25f, 0.4f, 0.9f), labelStyle = valueStyle) {}
        val rainbowBtn = GameButton("", iconDrawable = TextureFactory.createBoosterIcon("rainbow", 36), bgColor = Color(0.15f, 0.25f, 0.4f, 0.9f), labelStyle = valueStyle) {}

        val settingsBtn = GameButton("", iconDrawable = TextureFactory.createIcon("menu", 32), bgColor = Color(0.2f, 0.35f, 0.55f, 0.9f), labelStyle = valueStyle) {
            onSettingsClick()
        }

        bottomTable.add(hammerBtn).size(60f, 60f).padRight(16f)
        bottomTable.add(rocketBtn).size(60f, 60f).padRight(16f)
        bottomTable.add(rainbowBtn).size(60f, 60f).padRight(40f)
        bottomTable.add(settingsBtn).size(60f, 60f)

        stage.addActor(bottomTable)
    }

    fun update() {
        movesLabel.setText(moveCounter.movesRemaining.toString())
        scoreLabel.setText(scoreManager.currentScore.toString())
    }
}
