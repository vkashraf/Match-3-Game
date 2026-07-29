package com.example.game.match3

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.example.core.GameConstants
import com.example.core.ScreenManager
import com.example.game.BaseGameScreen
import com.example.game.IsleMatchGame
import com.example.game.match3.goal.GoalManager
import com.example.game.match3.hud.Match3Hud
import com.example.game.match3.input.BoardInputController
import com.example.game.match3.level.LevelConfigRepository
import com.example.game.match3.level.LevelController
import com.example.game.match3.level.LevelGameState
import com.example.game.match3.level.MoveCounter
import com.example.game.match3.match.MatchResolver
import com.example.game.match3.score.ScoreManager
import com.example.game.match3.swap.SwapManager
import com.example.game.match3.ui.DefeatDialog
import com.example.game.match3.ui.PauseDialog
import com.example.game.match3.ui.VictoryDialog
import com.example.utils.TextureFactory

class Match3Screen(
    game: IsleMatchGame,
    private val screenManager: ScreenManager,
    val levelId: Int = 1
) : BaseGameScreen(game) {

    private val camera = OrthographicCamera()
    private val viewport: Viewport = FitViewport(GameConstants.VIRTUAL_WIDTH, GameConstants.VIRTUAL_HEIGHT, camera)
    private val batch = SpriteBatch()
    private val stage = Stage(viewport, batch)

    val levelConfig = LevelConfigRepository.getLevelConfig(levelId)

    private val boardModel = BoardModel(8, 8)
    private val boardGenerator = BoardGenerator()
    private val swapManager = SwapManager(boardModel)
    private val boardRenderer = BoardRenderer(boardModel)

    private val moveCounter = MoveCounter(levelConfig.moves)
    private val scoreManager = ScoreManager(0)
    private val goals = levelConfig.goals
    private val goalManager = GoalManager(goals)

    val levelController = LevelController(
        levelConfig = levelConfig,
        boardModel = boardModel,
        scoreManager = scoreManager,
        goalManager = goalManager,
        moveCounter = moveCounter
    )

    // Board position and sizing calculations
    private val boardSize = 480f
    private val tileSize = boardSize / 8f // 60f
    private val boardX = (GameConstants.VIRTUAL_WIDTH - boardSize) / 2f
    private val boardY = 220f

    private val matchResolver = MatchResolver(
        boardModel = boardModel,
        swapManager = swapManager,
        moveCounter = moveCounter,
        scoreManager = scoreManager,
        goalManager = goalManager,
        tileSize = tileSize
    )

    private lateinit var inputController: BoardInputController
    private lateinit var match3Hud: Match3Hud

    private var activeDialogTable: Table? = null

    init {
        // Generate initial 8x8 board without matches
        boardGenerator.generateInitialBoard(boardModel)

        // Setup input controller
        inputController = BoardInputController(
            boardModel = boardModel,
            camera = camera,
            getBoardX = { boardX },
            getBoardY = { boardY },
            getTileSize = { tileSize },
            onSwapRequested = { r1, c1, r2, c2 ->
                if (levelController.gameState == LevelGameState.READY || levelController.gameState == LevelGameState.PLAYER_MOVING) {
                    matchResolver.requestSwap(r1, c1, r2, c2)
                }
            }
        )

        val multiplexer = InputMultiplexer()
        multiplexer.addProcessor(stage)
        multiplexer.addProcessor(inputController)
        Gdx.input.inputProcessor = multiplexer

        // Setup HUD
        match3Hud = Match3Hud(
            stage = stage,
            moveCounter = moveCounter,
            scoreManager = scoreManager,
            goals = goals,
            onSettingsClick = { showPauseDialog() }
        )

        stage.root.getColor().a = 0f
        stage.root.addAction(Actions.fadeIn(0.3f))
    }

    private fun showPauseDialog() {
        if (activeDialogTable != null) return
        levelController.pause()
        val pauseDialog = PauseDialog(
            game = game,
            screenManager = screenManager,
            levelController = levelController,
            stage = stage,
            font = BitmapFont(),
            onDismiss = { activeDialogTable = null }
        )
        activeDialogTable = pauseDialog
        stage.addActor(pauseDialog)
    }

    private fun showVictoryDialog() {
        if (activeDialogTable != null) return
        val victoryDialog = VictoryDialog(
            game = game,
            screenManager = screenManager,
            levelController = levelController
        )
        activeDialogTable = victoryDialog
        stage.addActor(victoryDialog)
    }

    private fun showDefeatDialog() {
        if (activeDialogTable != null) return
        val defeatDialog = DefeatDialog(
            game = game,
            screenManager = screenManager,
            levelController = levelController
        )
        activeDialogTable = defeatDialog
        stage.addActor(defeatDialog)
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0.08f, 0.2f, 0.38f, 1f) // Rich ocean blue
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        val isPausedOrEnded = levelController.gameState == LevelGameState.PAUSED ||
                levelController.gameState == LevelGameState.VICTORY ||
                levelController.gameState == LevelGameState.DEFEAT

        inputController.isInputLocked = matchResolver.isBusy || isPausedOrEnded

        // Update match resolver pipeline
        if (!isPausedOrEnded) {
            matchResolver.update(delta, boardX, boardY, camera)
            levelController.checkGameStatus(matchResolver.isBusy)
        }

        if (levelController.gameState == LevelGameState.VICTORY && activeDialogTable == null) {
            showVictoryDialog()
        } else if (levelController.gameState == LevelGameState.DEFEAT && activeDialogTable == null) {
            showDefeatDialog()
        }

        // Update HUD labels
        match3Hud.update()

        viewport.apply()
        batch.projectionMatrix = camera.combined
        batch.begin()

        // Draw background decoration / ocean gradient glow
        val bgDrawable = TextureFactory.createRoundedPanel(
            width = GameConstants.VIRTUAL_WIDTH.toInt(),
            height = GameConstants.VIRTUAL_HEIGHT.toInt(),
            fillColor = Color(0.08f, 0.18f, 0.36f, 1f),
            borderColor = Color(0.12f, 0.26f, 0.48f, 1f),
            borderThickness = 0
        )
        bgDrawable.draw(batch, 0f, 0f, GameConstants.VIRTUAL_WIDTH, GameConstants.VIRTUAL_HEIGHT)

        // Render 8x8 Board, Tiles & Floating Score Popups
        boardRenderer.render(
            batch = batch,
            boardX = boardX,
            boardY = boardY,
            boardWidth = boardSize,
            boardHeight = boardSize,
            tileSize = tileSize,
            floatingTexts = matchResolver.floatingTexts
        )

        matchResolver.particleEffectManager.render(batch)

        batch.end()

        stage.act(delta)
        stage.draw()
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
    }

    override fun dispose() {
        batch.dispose()
        stage.dispose()
    }
}
