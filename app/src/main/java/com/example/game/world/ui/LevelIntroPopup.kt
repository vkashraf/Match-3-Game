package com.example.game.world.ui

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
import com.example.game.match3.level.LevelConfigRepository
import com.example.game.world.LevelNodeConfig
import com.example.game.world.WorldConfigRepository
import com.example.ui.GameButton
import com.example.utils.TextureFactory

class LevelIntroPopup(
    private val game: IsleMatchGame,
    private val screenManager: ScreenManager,
    private val nodeConfig: LevelNodeConfig,
    private val levelProgress: LevelProgressEntity?,
    private val onClose: () -> Unit
) : Table() {

    init {
        val isBoss = nodeConfig.isBossLevel
        val worldConfig = WorldConfigRepository.getWorld(nodeConfig.worldId)
        val levelConfig = LevelConfigRepository.getLevelConfig(nodeConfig.levelId)

        background = TextureFactory.createRoundedPanel(
            width = 420,
            height = 500,
            fillColor = if (isBoss) Color(0.28f, 0.12f, 0.15f, 0.98f) else Color(0.12f, 0.2f, 0.35f, 0.98f),
            borderColor = if (isBoss) Color.RED else GameConstants.COLOR_GOLD,
            borderThickness = 4
        )
        setSize(420f, 500f)
        setPosition(
            GameConstants.VIRTUAL_WIDTH / 2f - 210f,
            GameConstants.VIRTUAL_HEIGHT / 2f - 250f
        )
        pad(20f)

        val titleFont = BitmapFont()
        titleFont.data.setScale(1.8f)
        val titleStyle = Label.LabelStyle(titleFont, if (isBoss) Color.RED else GameConstants.COLOR_GOLD)

        val titleText = if (isBoss) "★ BOSS LEVEL ${nodeConfig.levelId} ★" else "LEVEL ${nodeConfig.levelId}"
        add(Label(titleText, titleStyle)).padBottom(4f).row()

        val subFont = BitmapFont()
        subFont.data.setScale(1.1f)
        val subStyle = Label.LabelStyle(subFont, Color.WHITE)
        add(Label("${worldConfig.worldName}  •  ${levelConfig.difficulty.name}", subStyle)).padBottom(16f).row()

        // Goals table
        val fontMedium = BitmapFont()
        fontMedium.data.setScale(1.0f)
        val goalStyle = Label.LabelStyle(fontMedium, Color.WHITE)
        val goldStyle = Label.LabelStyle(fontMedium, GameConstants.COLOR_GOLD)

        val goalsTable = Table()
        goalsTable.background = TextureFactory.createRoundedPanel(
            width = 360,
            height = 110,
            fillColor = Color(0.15f, 0.25f, 0.4f, 0.8f),
            borderColor = Color(0.3f, 0.5f, 0.75f, 0.8f),
            borderThickness = 2
        )
        goalsTable.pad(10f)

        goalsTable.add(Label("TARGET GOALS:", goldStyle)).left().padBottom(6f).row()
        for (goal in levelConfig.goals) {
            val cleanName = goal.goalType.replace("_", " ")
            goalsTable.add(Label("• Collect ${goal.targetAmount} $cleanName", goalStyle)).left().row()
        }
        goalsTable.add(Label("Moves Allowed: ${levelConfig.moves}", subStyle)).left().padTop(4f).row()

        add(goalsTable).padBottom(16f).row()

        // Stars & Best Score
        val starsEarned = levelProgress?.stars ?: 0
        val starsStr = when (starsEarned) {
            3 -> "★ ★ ★"
            2 -> "★ ★ ☆"
            1 -> "★ ☆ ☆"
            else -> "☆ ☆ ☆"
        }
        val starFont = BitmapFont()
        starFont.data.setScale(1.8f)
        val starStyle = Label.LabelStyle(starFont, Color.YELLOW)

        add(Label(starsStr, starStyle)).padBottom(4f).row()

        val bestScore = levelProgress?.bestScore ?: 0
        add(Label("Best Score: $bestScore", subStyle)).padBottom(20f).row()

        // Action buttons
        val btnFont = BitmapFont()
        btnFont.data.setScale(1.2f)
        val btnStyle = Label.LabelStyle(btnFont, Color.WHITE)

        val buttonTable = Table()

        val playBtn = GameButton(
            text = "PLAY (-1 ⚡)",
            bgColor = GameConstants.COLOR_PLAY_BUTTON,
            borderColor = Color(0.85f, 0.45f, 0.1f, 1f),
            labelStyle = btnStyle,
            onClick = {
                val currentPlayer = GameDataProvider.cachedPlayer.value
                val energy = currentPlayer?.energy ?: 0
                if (energy >= 1) {
                    GameDataProvider.spendEnergy(1)
                    remove()
                    screenManager.setScreen(Match3Screen(game, screenManager, nodeConfig.levelId))
                } else {
                    // Show energy insufficient prompt
                    val noEnergyLabel = Label("NOT ENOUGH ENERGY!", Label.LabelStyle(fontMedium, Color.RED))
                    goalsTable.clear()
                    goalsTable.add(noEnergyLabel).row()
                    goalsTable.add(Label("Visit Shop or wait for regeneration", subStyle))
                }
            }
        )

        val cancelBtn = GameButton(
            text = "BACK",
            bgColor = Color(0.45f, 0.45f, 0.5f, 1f),
            labelStyle = btnStyle,
            onClick = {
                remove()
                onClose()
            }
        )

        buttonTable.add(cancelBtn).size(120f, 52f).padRight(12f)
        buttonTable.add(playBtn).size(200f, 52f)

        add(buttonTable)
    }
}
