package com.example.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.viewport.FitViewport
import com.example.core.GameConstants
import com.example.core.ScreenManager
import com.example.utils.TextureFactory

class SplashScreen(
    game: IsleMatchGame,
    private val screenManager: ScreenManager
) : BaseGameScreen(game) {

    private val stage = Stage(FitViewport(GameConstants.VIRTUAL_WIDTH, GameConstants.VIRTUAL_HEIGHT))
    private val batch = SpriteBatch()
    private val font = BitmapFont()
    private var progress = 0f

    init {
        font.data.setScale(2f)

        val titleStyle = Label.LabelStyle(font, GameConstants.COLOR_GOLD)
        val titleLabel = Label(GameConstants.GAME_TITLE, titleStyle)

        val subStyle = Label.LabelStyle(font, Color.WHITE)
        val subLabel = Label("Loading Island Adventure...", subStyle)

        val logoPanel = TextureFactory.createRoundedPanel(
            width = 380,
            height = 140,
            fillColor = GameConstants.COLOR_HUD_NAVY,
            borderColor = GameConstants.COLOR_GOLD,
            borderThickness = 6
        )

        val logoTable = Table()
        logoTable.background = logoPanel
        logoTable.add(titleLabel).padBottom(10f).row()
        logoTable.add(subLabel)

        val mainTable = Table()
        mainTable.setFillParent(true)
        mainTable.add(logoTable).padBottom(60f).row()

        stage.addActor(mainTable)
        stage.getRoot().getColor().a = 0f
        stage.getRoot().addAction(Actions.fadeIn(0.8f))
    }

    override fun render(delta: Float) {
        progress += delta * 0.8f

        stage.act(delta)
        stage.draw()

        if (progress >= 1.5f && stage.root.actions.size == 0) {
            stage.root.addAction(
                Actions.sequence(
                    Actions.fadeOut(0.5f),
                    Actions.run {
                        screenManager.setScreen(HomeScreen(game, screenManager))
                    }
                )
            )
        }
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun dispose() {
        stage.dispose()
        batch.dispose()
        font.dispose()
    }
}
