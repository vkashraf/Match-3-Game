package com.example.game.match3.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.example.core.GameConstants
import com.example.core.ScreenManager
import com.example.game.IsleMatchGame
import com.example.game.match3.Match3Screen
import com.example.game.match3.level.LevelController
import com.example.ui.GameButton
import com.example.utils.TextureFactory

class PauseDialog(
    private val game: IsleMatchGame,
    private val screenManager: ScreenManager,
    private val levelController: LevelController,
    private val stage: Stage,
    private val font: BitmapFont,
    private val onDismiss: () -> Unit
) : Table() {

    init {
        background = TextureFactory.createRoundedPanel(
            width = 360,
            height = 320,
            fillColor = Color(0.1f, 0.15f, 0.28f, 0.95f),
            borderColor = GameConstants.COLOR_GOLD,
            borderThickness = 4
        )
        setSize(360f, 320f)
        setPosition(GameConstants.VIRTUAL_WIDTH / 2f - 180f, GameConstants.VIRTUAL_HEIGHT / 2f - 160f)
        pad(20f)

        val titleFont = BitmapFont()
        titleFont.data.setScale(1.5f)
        val titleStyle = Label.LabelStyle(titleFont, GameConstants.COLOR_GOLD)
        add(Label("PAUSED", titleStyle)).padBottom(20f).row()

        val btnFont = BitmapFont()
        btnFont.data.setScale(1.1f)
        val btnStyle = Label.LabelStyle(btnFont, Color.WHITE)

        val resumeBtn = GameButton("RESUME", bgColor = Color(0.15f, 0.6f, 0.25f, 1f), labelStyle = btnStyle) {
            levelController.resume()
            remove()
            onDismiss()
        }

        val restartBtn = GameButton("RESTART", bgColor = Color(0.85f, 0.5f, 0.1f, 1f), labelStyle = btnStyle) {
            remove()
            screenManager.setScreen(Match3Screen(game, screenManager, levelController.levelConfig.levelId))
        }

        val quitBtn = GameButton("QUIT", bgColor = Color(0.75f, 0.2f, 0.2f, 1f), labelStyle = btnStyle) {
            remove()
            screenManager.setScreen(LevelMapScreen(game, screenManager))
        }

        add(resumeBtn).size(220f, 50f).padBottom(12f).row()
        add(restartBtn).size(220f, 50f).padBottom(12f).row()
        add(quitBtn).size(220f, 50f)
    }
}
