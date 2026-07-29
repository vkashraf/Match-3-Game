package com.example.game.island.camera

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector2
import com.example.core.GameConstants

class IslandCameraController(
    private val camera: OrthographicCamera,
    val minX: Float = 0f,
    val maxX: Float = GameConstants.VIRTUAL_WIDTH * 1.5f,
    val minY: Float = 0f,
    val maxY: Float = GameConstants.VIRTUAL_HEIGHT * 1.5f,
    val minZoom: Float = 0.7f,
    val maxZoom: Float = 1.4f
) {

    private val targetPos = Vector2(camera.position.x, camera.position.y)
    private var targetZoom = camera.zoom

    fun update(delta: Float) {
        // Smooth lerp camera movement
        camera.position.x += (targetPos.x - camera.position.x) * (delta * 12f)
        camera.position.y += (targetPos.y - camera.position.y) * (delta * 12f)
        camera.zoom += (targetZoom - camera.zoom) * (delta * 10f)

        clampCamera()
        camera.update()
    }

    fun pan(deltaX: Float, deltaY: Float) {
        targetPos.x -= deltaX * camera.zoom
        targetPos.y += deltaY * camera.zoom
        clampTarget()
    }

    fun zoom(amount: Float) {
        targetZoom = (targetZoom + amount).coerceIn(minZoom, maxZoom)
        clampTarget()
    }

    fun focusOn(x: Float, y: Float, zoom: Float = 0.9f) {
        targetPos.set(x, y)
        targetZoom = zoom.coerceIn(minZoom, maxZoom)
        clampTarget()
    }

    private fun clampTarget() {
        val halfW = (GameConstants.VIRTUAL_WIDTH * targetZoom) / 2f
        val halfH = (GameConstants.VIRTUAL_HEIGHT * targetZoom) / 2f

        targetPos.x = targetPos.x.coerceIn(minX + halfW - 200f, maxX - halfW + 200f)
        targetPos.y = targetPos.y.coerceIn(minY + halfH - 200f, maxY - halfH + 200f)
    }

    private fun clampCamera() {
        val halfW = (GameConstants.VIRTUAL_WIDTH * camera.zoom) / 2f
        val halfH = (GameConstants.VIRTUAL_HEIGHT * camera.zoom) / 2f

        camera.position.x = camera.position.x.coerceIn(minX + halfW - 200f, maxX - halfW + 200f)
        camera.position.y = camera.position.y.coerceIn(minY + halfH - 200f, maxY - halfH + 200f)
    }
}
