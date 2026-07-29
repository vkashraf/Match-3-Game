package com.example.core

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.example.game.IsleMatchGame

class ScreenManager(private val game: IsleMatchGame) {

    private var currentScreen: Screen? = null

    fun setScreen(newScreen: Screen) {
        val oldScreen = currentScreen
        currentScreen = newScreen
        game.screen = newScreen
        oldScreen?.dispose()
    }

    fun getCurrentScreen(): Screen? = currentScreen
}
