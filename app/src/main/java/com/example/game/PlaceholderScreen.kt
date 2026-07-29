package com.example.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.viewport.FitViewport
import com.example.core.GameConstants
import com.example.core.ScreenManager
import com.example.ui.GameButton
import com.example.utils.TextureFactory

class PlaceholderScreen(
    game: IsleMatchGame,
    private val screenManager: ScreenManager,
    private val titleText: String
) : BaseGameScreen(game) {

    private val stage = Stage(FitViewport(GameConstants.VIRTUAL_WIDTH, GameConstants.VIRTUAL_HEIGHT))
    private val font = BitmapFont()

    init {
        Gdx.input.inputProcessor = stage
        font.data.setScale(2f)

        val titleStyle = Label.LabelStyle(font, GameConstants.COLOR_GOLD)
        val titleLabel = Label(titleText, titleStyle)

        val subStyle = Label.LabelStyle(font, Color.WHITE)
        val subLabel = Label("Feature Coming Soon in Next Phase", subStyle)

        val cardPanel = TextureFactory.createRoundedPanel(
            width = 460,
            height = 280,
            fillColor = GameConstants.COLOR_HUD_NAVY,
            borderColor = GameConstants.COLOR_GOLD,
            borderThickness = 5
        )

        val cardTable = Table()
        cardTable.background = cardPanel
        cardTable.add(titleLabel).padBottom(16f).row()
        cardTable.add(subLabel).padBottom(30f).row()

        val buttonStyle = Label.LabelStyle(font, Color.WHITE)
        val backButton = GameButton(
            text = "BACK",
            bgColor = Color(0.85f, 0.25f, 0.25f, 1f),
            borderColor = Color(0.6f, 0.15f, 0.15f, 1f),
            labelStyle = buttonStyle,
            onClick = {
                screenManager.setScreen(HomeScreen(game, screenManager))
            }
        )

        cardTable.add(backButton).size(180f, 60f)

        val mainTable = Table()
        mainTable.setFillParent(true)
        mainTable.add(cardTable)

        stage.addActor(mainTable)
    }

    override fun render(delta: Float) {
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
