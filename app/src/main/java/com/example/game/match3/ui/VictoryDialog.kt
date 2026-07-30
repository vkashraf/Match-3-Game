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

class VictoryDialog(
    private val game: IsleMatchGame,
    private val screenManager: ScreenManager,
    private val levelController: LevelController
) : Table() {

    init {
        background = TextureFactory.createRoundedPanel(
            width = 420,
            height = 420,
            fillColor = Color(0.12f, 0.22f, 0.35f, 0.96f),
            borderColor = GameConstants.COLOR_GOLD,
            borderThickness = 4
        )
        setSize(420f, 420f)
        setPosition(GameConstants.VIRTUAL_WIDTH / 2f - 210f, GameConstants.VIRTUAL_HEIGHT / 2f - 210f)
        pad(20f)

        val titleFont = BitmapFont()
        titleFont.data.setScale(1.8f)
        val titleStyle = Label.LabelStyle(titleFont, GameConstants.COLOR_GOLD)
        add(Label("LEVEL COMPLETE!", titleStyle)).padBottom(12f).row()

        // Stars display
        val starsStr = when (levelController.finalStarsEarned) {
            3 -> "★ ★ ★"
            2 -> "★ ★ ☆"
            1 -> "★ ☆ ☆"
            else -> "☆ ☆ ☆"
        }
        val starFont = BitmapFont()
        starFont.data.setScale(2.2f)
        val starStyle = Label.LabelStyle(starFont, Color.YELLOW)
        add(Label(starsStr, starStyle)).padBottom(16f).row()

        // Info table
        val fontMedium = BitmapFont()
        fontMedium.data.setScale(1.1f)
        val infoStyle = Label.LabelStyle(fontMedium, Color.WHITE)

        val infoTable = Table()
        infoTable.add(Label("Score: ${levelController.scoreManager.currentScore}", infoStyle)).padBottom(6f).row()
        infoTable.add(Label("Moves Left: ${levelController.moveCounter.movesRemaining}", infoStyle)).padBottom(6f).row()
        infoTable.add(Label("Coins: +${levelController.finalCoinsEarned}", infoStyle)).padBottom(6f).row()
        infoTable.add(Label("XP: +${levelController.finalXpEarned}", infoStyle)).padBottom(16f).row()
        add(infoTable).row()

        // Action Buttons
        val btnFont = BitmapFont()
        btnFont.data.setScale(1.0f)
        val btnStyle = Label.LabelStyle(btnFont, Color.WHITE)

        val btnTable = Table()
        // Post game events for missions/achievements/stats
        val currentLevelId = levelController.levelConfig.levelId
        com.example.core.event.GameEventBus.postEvent(
            com.example.core.event.GameEvent(
                type = com.example.core.event.GameEventType.LEVEL_COMPLETED,
                levelId = currentLevelId
            )
        )
        if (levelController.finalStarsEarned > 0) {
            com.example.core.event.GameEventBus.postEvent(
                com.example.core.event.GameEvent(
                    type = com.example.core.event.GameEventType.STAR_EARNED,
                    amount = levelController.finalStarsEarned
                )
            )
        }
        if (levelController.finalCoinsEarned > 0) {
            com.example.core.event.GameEventBus.postEvent(
                com.example.core.event.GameEvent(
                    type = com.example.core.event.GameEventType.COINS_EARNED,
                    amount = levelController.finalCoinsEarned
                )
            )
        }

        if (currentLevelId < 100) {
            val nextBtn = GameButton("NEXT LEVEL", bgColor = Color(0.15f, 0.65f, 0.25f, 1f), labelStyle = btnStyle) {
                remove()
                screenManager.setScreen(Match3Screen(game, screenManager, currentLevelId + 1))
            }
            btnTable.add(nextBtn).size(130f, 50f).padRight(8f)
        } else {
            val chapterBtn = GameButton("CHAPTER COMPLETE", bgColor = Color(0.85f, 0.65f, 0.1f, 1f), labelStyle = btnStyle) {
                remove()
                screenManager.setScreen(com.example.game.world.ui.ChapterCompleteScreen(game, screenManager))
            }
            btnTable.add(chapterBtn).size(160f, 50f).padRight(8f)
        }

        val replayBtn = GameButton("REPLAY", bgColor = Color(0.2f, 0.5f, 0.8f, 1f), labelStyle = btnStyle) {
            remove()
            screenManager.setScreen(Match3Screen(game, screenManager, currentLevelId))
        }

        val mapBtn = GameButton("MAP", bgColor = Color(0.75f, 0.45f, 0.1f, 1f), labelStyle = btnStyle) {
            remove()
            screenManager.setScreen(LevelMapScreen(game, screenManager))
        }

        btnTable.add(replayBtn).size(110f, 50f).padRight(8f)
        btnTable.add(mapBtn).size(90f, 50f)

        add(btnTable)
    }
}
