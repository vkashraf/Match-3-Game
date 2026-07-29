package com.example.game.match3.effect

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2

class ScreenShakeController {
    private var shakeTime = 0f
    private var shakeDuration = 0f
    private var intensity = 0f
    private var isShaking = false

    fun shake(duration: Float = 0.3f, intensity: Float = 8f) {
        this.shakeDuration = duration
        this.shakeTime = duration
        this.intensity = intensity
        this.isShaking = true
    }

    fun update(delta: Float, camera: OrthographicCamera, defaultCamX: Float, defaultCamY: Float) {
        if (!isShaking) return

        shakeTime -= delta
        if (shakeTime > 0) {
            val currentIntensity = intensity * (shakeTime / shakeDuration)
            val offsetX = MathUtils.random(-currentIntensity, currentIntensity)
            val offsetY = MathUtils.random(-currentIntensity, currentIntensity)
            camera.position.set(defaultCamX + offsetX, defaultCamY + offsetY, 0f)
        } else {
            camera.position.set(defaultCamX, defaultCamY, 0f)
            isShaking = false
        }
        camera.update()
    }
}
