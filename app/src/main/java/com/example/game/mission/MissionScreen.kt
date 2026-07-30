package com.example.game.mission

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
import com.example.data.local.entity.AchievementProgressEntity
import com.example.data.local.entity.MissionProgressEntity
import com.example.data.repository.AchievementData
import com.example.data.repository.MissionData
import com.example.game.BaseGameScreen
import com.example.game.HomeScreen
import com.example.game.IsleMatchGame
import com.example.game.reward.RewardType
import com.example.ui.GameButton
import com.example.ui.RewardPopup
import com.example.utils.TextureFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MissionScreen(
    game: IsleMatchGame,
    private val screenManager: ScreenManager,
    private var activeTab: String = "DAILY" // "DAILY" or "ACHIEVEMENTS"
) : BaseGameScreen(game) {

    private val viewport = FitViewport(GameConstants.VIRTUAL_WIDTH, GameConstants.VIRTUAL_HEIGHT)
    private val stage = Stage(viewport)

    private val fontTitle = BitmapFont().apply { data.setScale(1.3f) }
    private val fontMedium = BitmapFont().apply { data.setScale(1.0f) }
    private val fontSmall = BitmapFont().apply { data.setScale(0.85f) }

    private val scope = CoroutineScope(Dispatchers.Main)

    private val rootTable = Table()
    private val contentTable = Table()
    private var dailyMissions: List<MissionProgressEntity> = emptyList()
    private var achievements: List<AchievementProgressEntity> = emptyList()

    override fun show() {
        Gdx.input.inputProcessor = stage
        setupLayout()
        observeData()
    }

    private fun observeData() {
        scope.launch {
            GameDataProvider.missionRepository.allMissionsFlow.collectLatest { list ->
                dailyMissions = list
                if (activeTab == "DAILY") updateContent()
            }
        }

        scope.launch {
            GameDataProvider.achievementRepository.allAchievementsFlow.collectLatest { list ->
                achievements = list
                if (activeTab == "ACHIEVEMENTS") updateContent()
            }
        }
    }

    private fun setupLayout() {
        rootTable.setFillParent(true)
        rootTable.pad(16f)

        // HEADER
        val titleStyle = Label.LabelStyle(fontTitle, GameConstants.COLOR_GOLD)
        rootTable.add(Label("MISSIONS & ACHIEVEMENTS", titleStyle)).padBottom(12f).row()

        // TAB BUTTONS
        val tabsTable = Table()
        val btnStyle = Label.LabelStyle(fontMedium, Color.WHITE)

        val dailyTabBtn = GameButton(
            "DAILY MISSIONS",
            bgColor = if (activeTab == "DAILY") GameConstants.COLOR_GOLD else Color(0.2f, 0.3f, 0.45f, 0.9f),
            labelStyle = btnStyle
        ) {
            activeTab = "DAILY"
            setupLayout()
            updateContent()
        }

        val achieveTabBtn = GameButton(
            "ACHIEVEMENTS",
            bgColor = if (activeTab == "ACHIEVEMENTS") GameConstants.COLOR_GOLD else Color(0.2f, 0.3f, 0.45f, 0.9f),
            labelStyle = btnStyle
        ) {
            activeTab = "ACHIEVEMENTS"
            setupLayout()
            updateContent()
        }

        tabsTable.add(dailyTabBtn).size(180f, 44f).padRight(12f)
        tabsTable.add(achieveTabBtn).size(180f, 44f)

        rootTable.add(tabsTable).padBottom(16f).row()

        // CONTENT SCROLL PANE
        val scrollPane = ScrollPane(contentTable)
        scrollPane.setScrollingDisabled(true, false)
        rootTable.add(scrollPane).expand().fill().padBottom(16f).row()

        // CLOSE BUTTON
        val closeBtn = GameButton("BACK TO ISLAND", bgColor = Color(0.2f, 0.35f, 0.55f, 0.9f), labelStyle = btnStyle) {
            screenManager.setScreen(HomeScreen(game, screenManager))
        }
        rootTable.add(closeBtn).size(200f, 44f)

        stage.addActor(rootTable)
    }

    private fun updateContent() {
        contentTable.clearChildren()

        if (activeTab == "DAILY") {
            renderDailyMissions()
        } else {
            renderAchievements()
        }
    }

    private fun renderDailyMissions() {
        if (dailyMissions.isEmpty()) {
            val emptyStyle = Label.LabelStyle(fontMedium, Color.WHITE)
            contentTable.add(Label("Loading Daily Missions...", emptyStyle)).center()
            return
        }

        dailyMissions.forEach { mission ->
            val config = MissionData.getConfig(mission.missionId) ?: return@forEach
            val card = Table()

            val isDone = mission.isCompleted
            val isClaimed = mission.isClaimed

            val fillColor = when {
                isClaimed -> Color(0.1f, 0.16f, 0.25f, 0.7f)
                isDone -> Color(0.15f, 0.28f, 0.22f, 0.9f)
                else -> Color(0.12f, 0.2f, 0.32f, 0.85f)
            }

            val borderColor = when {
                isClaimed -> Color(0.25f, 0.35f, 0.45f, 0.6f)
                isDone -> Color(0.3f, 0.8f, 0.4f, 1f)
                else -> GameConstants.COLOR_GOLD
            }

            card.background = TextureFactory.createRoundedPanel(
                width = 440, height = 90,
                fillColor = fillColor,
                borderColor = borderColor,
                borderThickness = 2
            )
            card.pad(12f)

            // Left Icon
            val icon = TextureFactory.createIcon("star", 36)
            card.add(Image(icon)).size(36f).padRight(12f)

            // Center details
            val details = Table()
            val titleStyle = Label.LabelStyle(fontMedium, if (isDone) Color(0.4f, 1f, 0.5f, 1f) else GameConstants.COLOR_GOLD)
            val subStyle = Label.LabelStyle(fontSmall, Color(0.85f, 0.9f, 1f, 1f))

            details.add(Label(config.title, titleStyle)).left().row()
            details.add(Label(config.description, subStyle)).left().row()

            val progressText = "${mission.currentProgress} / ${mission.target}"
            details.add(Label("Progress: $progressText", subStyle)).left()

            card.add(details).expandX().left()

            // Right Action Button / Status
            val btnStyle = Label.LabelStyle(fontSmall, Color.WHITE)
            when {
                isClaimed -> {
                    val statusLabel = Label("✔ CLAIMED", Label.LabelStyle(fontSmall, Color(0.4f, 0.9f, 0.5f, 1f)))
                    card.add(statusLabel).padLeft(8f)
                }
                isDone -> {
                    val claimBtn = GameButton("CLAIM", bgColor = GameConstants.COLOR_GOLD, labelStyle = btnStyle) {
                        claimMission(mission.missionId)
                    }
                    card.add(claimBtn).size(90f, 38f).padLeft(8f)
                }
                else -> {
                    val statusLabel = Label("IN PROGRESS", Label.LabelStyle(fontSmall, Color(0.7f, 0.8f, 0.9f, 1f)))
                    card.add(statusLabel).padLeft(8f)
                }
            }

            contentTable.add(card).size(440f, 90f).padBottom(10f).row()
        }
    }

    private fun renderAchievements() {
        if (achievements.isEmpty()) {
            val emptyStyle = Label.LabelStyle(fontMedium, Color.WHITE)
            contentTable.add(Label("Loading Achievements...", emptyStyle)).center()
            return
        }

        achievements.forEach { achievement ->
            val config = AchievementData.getConfig(achievement.achievementId) ?: return@forEach
            val card = Table()

            val isDone = achievement.isCompleted
            val isClaimed = achievement.isRewardClaimed

            val fillColor = when {
                isClaimed -> Color(0.1f, 0.16f, 0.25f, 0.7f)
                isDone -> Color(0.18f, 0.3f, 0.22f, 0.95f)
                else -> Color(0.12f, 0.2f, 0.32f, 0.85f)
            }

            val borderColor = when {
                isClaimed -> Color(0.25f, 0.35f, 0.45f, 0.6f)
                isDone -> Color(0.3f, 0.8f, 0.4f, 1f)
                else -> Color(0.3f, 0.5f, 0.7f, 0.8f)
            }

            card.background = TextureFactory.createRoundedPanel(
                width = 440, height = 90,
                fillColor = fillColor,
                borderColor = borderColor,
                borderThickness = 2
            )
            card.pad(12f)

            // Left Icon
            val icon = TextureFactory.createIcon("gem", 36)
            card.add(Image(icon)).size(36f).padRight(12f)

            // Details
            val details = Table()
            val titleStyle = Label.LabelStyle(fontMedium, if (isDone) Color(0.4f, 1f, 0.5f, 1f) else GameConstants.COLOR_GOLD)
            val subStyle = Label.LabelStyle(fontSmall, Color(0.85f, 0.9f, 1f, 1f))

            details.add(Label(config.title, titleStyle)).left().row()
            details.add(Label(config.description, subStyle)).left().row()

            val progressText = "${achievement.currentProgress} / ${achievement.target}"
            details.add(Label("Progress: $progressText", subStyle)).left()

            card.add(details).expandX().left()

            // Right Action / Status
            val btnStyle = Label.LabelStyle(fontSmall, Color.WHITE)
            when {
                isClaimed -> {
                    val statusLabel = Label("✔ CLAIMED", Label.LabelStyle(fontSmall, Color(0.4f, 0.9f, 0.5f, 1f)))
                    card.add(statusLabel).padLeft(8f)
                }
                isDone -> {
                    val claimBtn = GameButton("CLAIM", bgColor = GameConstants.COLOR_GOLD, labelStyle = btnStyle) {
                        claimAchievement(achievement.achievementId)
                    }
                    card.add(claimBtn).size(90f, 38f).padLeft(8f)
                }
                else -> {
                    val statusLabel = Label("LOCKED", Label.LabelStyle(fontSmall, Color(0.6f, 0.7f, 0.8f, 0.8f)))
                    card.add(statusLabel).padLeft(8f)
                }
            }

            contentTable.add(card).size(440f, 90f).padBottom(10f).row()
        }
    }

    private fun claimMission(missionId: String) {
        scope.launch(Dispatchers.IO) {
            val repo = GameDataProvider.missionRepository
            val (success, rewards) = repo.claimMission(missionId)
            if (success) {
                launch(Dispatchers.Main) {
                    RewardPopup.show(stage, "MISSION COMPLETED!", rewards)
                }
            }
        }
    }

    private fun claimAchievement(achievementId: String) {
        scope.launch(Dispatchers.IO) {
            val repo = GameDataProvider.achievementRepository
            val (success, rewards) = repo.claimAchievement(achievementId)
            if (success) {
                launch(Dispatchers.Main) {
                    RewardPopup.show(stage, "ACHIEVEMENT UNLOCKED!", rewards)
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
