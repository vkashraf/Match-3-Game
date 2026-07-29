package com.example.manager

object SoundManager {
    var isMusicEnabled: Boolean = true
    var isSoundEnabled: Boolean = true

    fun playSound(soundName: String) {
        if (!isSoundEnabled) return
        // Sound playback implementation placeholder for sound assets
    }

    fun playMusic(musicName: String) {
        if (!isMusicEnabled) return
        // Music playback implementation placeholder
    }
}
