package com.example.game.match3.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.example.core.GameConstants
import com.example.core.GameDataProvider
import com.example.core.ScreenManager
import com.example.data.local.entity.LevelProgressEntity
import com.example.game.IsleMatchGame
import com.example.game.match3.Match3Screen
import com.example.game.match3.level.LevelConfig
import com.example.ui.GameButton
import com.example.utils.TextureFactory

class LevelPreviewPopup(
    private val game: IsleMatchGame,
    private val screenManager: ScreenManager,
    private val config: LevelConfig,
    private val progress: LevelProgressEntity?,
    private val onClose: () -> Unit
) : Table() {

    init {
        background = TextureFactory.createRoundedPanel(
            width = 420,
            height = 440,
            fillColor = Color(0.12f, 0.2f, 0.35f, 0.96f),
            borderColor = GameConstants.COLOR_GOLD,
            borderThickness = 4
        )
        setSize(420f, 440f)
        setPosition(GameConstants.VIRTUAL_WIDTH / 2f - 210f, GameConstants.VIRTUAL_HEIGHT / 2f - 220f)
        pad(20f)

        val titleFont = BitmapFont()
        titleFont.data.setScale(1.6f)
        val titleStyle = Label.LabelStyle(titleFont, GameConstants.COLOR_GOLD)
        add(Label("LEVEL ${config.levelId}", titleStyle)).padBottom(4f).row()

        val subFont = BitmapFont()
        subFont.data.setScale(1.0f)
        val diffColor = when (config.difficulty) {
            com.example.game.match3.level.Difficulty.EASY -> Color.GREEN
            com.example.game.match3.level.Difficulty.NORMAL -> Color.CYAN
            com.example.game.match3.level.Difficulty.HARD -> Color.ORANGE
            com.example.game.match3.level.Difficulty.VERY_HARD -> Color.FIREBRICK
            com.example.game.match3.level.Difficulty.EXPERT -> Color.MAGENTA
        }
        val diffStyle = Label.LabelStyle(subFont, diffColor)
        add(Label("Difficulty: ${config.difficulty}", diffStyle)).padBottom(16f).row()

        val fontMedium = BitmapFont()
        fontMedium.data.setScale(1.1f)
        val infoStyle = Label.LabelStyle(fontMedium, Color.WHITE)

        // Goals
        val goalsTable = Table()
        goalsTable.add(Label("Goals:", infoStyle)).padBottom(6f).row()
        for (goal in config.goals) {
            goalsTable.add(Label("• ${goal.goalType}: ${goal.targetAmount}", infoStyle)).padBottom(4f).row()
        }
        add(goalsTable).padBottom(12f).row()

        // Moves & Rewards
        val detailsTable = Table()
        detailsTable.add(Label("Moves: ${config.moves}", infoStyle)).padBottom(4f).row()
        detailsTable.add(Label("Rewards: +${config.coinReward} Coins, +${config.xpReward} XP", infoStyle)).padBottom(4f).row()

        if (progress?.isCompleted == true) {
            val starStr = "★".repeat(progress.stars) + "☆".repeat(3 - progress.stars)
            val starStyle = Label.LabelStyle(fontMedium, Color.YELLOW)
            detailsTable.add(Label("Best: $starStr  (Score: ${progress.bestScore})", starStyle)).padBottom(4f).row()
        }
        add(detailsTable).padBottom(20f).row()

        // Buttons
        val btnFont = BitmapFont()
        btnFont.data.setScale(1.1f)
        val btnStyle = Label.LabelStyle(btnFont, Color.WHITE)

        val isUnlocked = progress?.isUnlocked ?: (config.levelId == 1)

        if (isUnlocked) {
            val playBtn = GameButton("PLAY (-1 Energy)", bgColor = Color(0.18f, 0.65f, 0.25f, 1f), labelStyle = btnStyle) {
                val player = GameDataProvider.cachedPlayer.value
                val currentEnergy = player?.energy ?: 0
                if (currentEnergy >= 1) {
                    GameDataProvider.spendEnergy(1)
                    remove()
                    screenManager.setScreen(Match3Screen(game, screenManager, config.levelId))
                } else {
                    showEnergyAlert()
                }
            }

            val closeBtn = GameButton("CLOSE", bgColor = Color(0.6f, 0.2f, 0.2f, 1f), labelStyle = btnStyle) {
                remove()
                onClose()
            }

            val btnTable = Table()
            btnTable.add(playBtn).size(200f, 52f).padRight(12f)
            btnTable.add(closeBtn).size(110f, 52f)
            add(btnTable)
        } else {
            val lockedStyle = Label.LabelStyle(fontMedium, Color(0.9f, 0.4f, 0.4f, 1f))
            add(Label("Complete Level ${config.levelId - 1} to unlock!", lockedStyle)).padBottom(12f).row()

            val closeBtn = GameButton("CLOSE", bgColor = Color(0.5f, 0.5f, 0.5f, 1f), labelStyle = btnStyle) {
                remove()
                onClose()
            }
            add(closeBtn).size(140f, 50f)
        }
    }

    private fun showEnergyAlert() {
        val alertTable = Table()
        alertTable.background = TextureFactory.createRoundedPanel(
            width = 300,
            height = 180,
            fillColor = Color(0.2f, 0.1f, 0.1f, 0.98f),
            borderColor = Color.RED,
            borderThickness = 3
        )
        alertTable.setSize(300f, 180f)
        alertTable.setPosition(GameConstants.VIRTUAL_WIDTH / 2f - 150f, GameConstants.VIRTUAL_HEIGHT / 2f - 90f)
        alertTable.pad(16f)

        val fontMedium = BitmapFont()
        fontMedium.data.setScale(1.1f)
        val alertStyle = Label.LabelStyle(fontMedium, Color.WHITE)

        alertTable.add(Label("Not Enough Energy!", alertStyle)).padBottom(20f).row()

        val btnStyle = Label.LabelStyle(fontMedium, Color.WHITE)
        val okBtn = GameButton("OK", bgColor = Color(0.7f, 0.2f, 0.2f, 1f), labelStyle = btnStyle) {
            alertTable.remove()
        }
        alertTable.add(okBtn).size(100f, 40f)

        stage?.addActor(alertTable)
    }
}
