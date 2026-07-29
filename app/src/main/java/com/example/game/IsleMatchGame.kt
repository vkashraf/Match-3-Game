package com.example.game

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.example.core.ScreenManager
import com.example.utils.TextureFactory

class IsleMatchGame : Game() {

    private lateinit var screenManager: ScreenManager

    override fun create() {
        Gdx.app.log("IsleMatchGame", "LibGDX Game Initialized successfully")
        screenManager = ScreenManager(this)
        screenManager.setScreen(SplashScreen(this, screenManager))
    }

    override fun render() {
        Gdx.gl.glClearColor(0.08f, 0.42f, 0.72f, 1f) // Ocean blue clear color
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        super.render()
    }

    override fun dispose() {
        screenManager.getCurrentScreen()?.dispose()
        TextureFactory.dispose()
        super.dispose()
    }
}
