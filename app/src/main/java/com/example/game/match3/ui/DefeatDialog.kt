package com.example.game.match3.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.example.core.GameConstants
import com.example.core.ScreenManager
import com.example.game.IsleMatchGame
import com.example.game.match3.Match3Screen
import com.example.game.match3.level.LevelController
import com.example.ui.GameButton
import com.example.utils.TextureFactory

class DefeatDialog(
    private val game: IsleMatchGame,
    private val screenManager: ScreenManager,
    private val levelController: LevelController
) : Table() {

    init {
        background = TextureFactory.createRoundedPanel(
            width = 400,
            height = 360,
            fillColor = Color(0.25f, 0.12f, 0.15f, 0.96f),
            borderColor = Color(0.85f, 0.25f, 0.25f, 1f),
            borderThickness = 4
        )
        setSize(400f, 360f)
        setPosition(GameConstants.VIRTUAL_WIDTH / 2f - 200f, GameConstants.VIRTUAL_HEIGHT / 2f - 180f)
        pad(20f)

        val titleFont = BitmapFont()
        titleFont.data.setScale(1.8f)
        val titleStyle = Label.LabelStyle(titleFont, Color(0.95f, 0.35f, 0.35f, 1f))
        add(Label("LEVEL FAILED", titleStyle)).padBottom(16f).row()

        val fontMedium = BitmapFont()
        fontMedium.data.setScale(1.1f)
        val infoStyle = Label.LabelStyle(fontMedium, Color.WHITE)

        val goalsTable = Table()
        goalsTable.add(Label("Goals Remaining:", infoStyle)).padBottom(10f).row()

        for (goal in levelController.goalManager.goals) {
            val progressText = "${goal.goalType}: ${goal.currentAmount}/${goal.targetAmount}"
            val style = if (goal.isCompleted) {
                Label.LabelStyle(fontMedium, Color.GREEN)
            } else {
                Label.LabelStyle(fontMedium, Color(1f, 0.6f, 0.6f, 1f))
            }
            goalsTable.add(Label(progressText, style)).padBottom(4f).row()
        }
        add(goalsTable).padBottom(20f).row()

        val btnFont = BitmapFont()
        btnFont.data.setScale(1.1f)
        val btnStyle = Label.LabelStyle(btnFont, Color.WHITE)

        val btnTable = Table()
        val retryBtn = GameButton("RETRY", bgColor = Color(0.85f, 0.45f, 0.1f, 1f), labelStyle = btnStyle) {
            remove()
            screenManager.setScreen(Match3Screen(game, screenManager, levelController.levelConfig.levelId))
        }

        val mapBtn = GameButton("MAP", bgColor = Color(0.5f, 0.5f, 0.5f, 1f), labelStyle = btnStyle) {
            remove()
            screenManager.setScreen(LevelMapScreen(game, screenManager))
        }

        btnTable.add(retryBtn).size(150f, 50f).padRight(16f)
        btnTable.add(mapBtn).size(120f, 50f)

        add(btnTable)
    }
}
