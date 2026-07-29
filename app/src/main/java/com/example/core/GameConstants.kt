package com.example.core

import com.badlogic.gdx.graphics.Color

object GameConstants {
    const val GAME_TITLE = "Island Quest"
    
    // Virtual resolution for UI scaling (Portrait 9:16 aspect ratio base)
    const val VIRTUAL_WIDTH = 720f
    const val VIRTUAL_HEIGHT = 1280f
    
    // Colors
    val COLOR_OCEAN = Color(0.08f, 0.42f, 0.72f, 1f)
    val COLOR_ISLAND_GREEN = Color(0.24f, 0.65f, 0.28f, 1f)
    val COLOR_ISLAND_SAND = Color(0.92f, 0.82f, 0.56f, 1f)
    val COLOR_GOLD = Color(1.0f, 0.78f, 0.15f, 1f)
    val COLOR_PURPLE_GEM = Color(0.68f, 0.22f, 0.88f, 1f)
    val COLOR_ENERGY = Color(0.2f, 0.78f, 0.98f, 1f)
    val COLOR_HUD_NAVY = Color(0.08f, 0.18f, 0.35f, 0.88f)
    val COLOR_WOOD_BEIGE = Color(0.94f, 0.88f, 0.76f, 1f)
    val COLOR_PLAY_BUTTON = Color(0.98f, 0.55f, 0.08f, 1f)
}
