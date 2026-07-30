package com.example.game.match3.ui

import com.example.core.ScreenManager
import com.example.game.IsleMatchGame

/**
 * LevelMapScreen acts as a backwards-compatible alias to WorldMapScreen.
 */
class LevelMapScreen(
    game: IsleMatchGame,
    screenManager: ScreenManager
) : WorldMapScreen(game, screenManager)
