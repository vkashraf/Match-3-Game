package com.example.game.island.renderer

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.example.core.GameConstants
import com.example.data.local.entity.IslandZoneEntity

class IslandRenderer(
    private val shapeRenderer: ShapeRenderer,
    private val batch: SpriteBatch,
    private val font: BitmapFont
) {

    private var cloudX1 = 50f
    private var cloudX2 = 450f

    fun renderBackground(delta: Float, zonesMap: Map<Int, IslandZoneEntity>) {
        cloudX1 = (cloudX1 + delta * 12f) % (GameConstants.VIRTUAL_WIDTH * 1.5f + 200f)
        cloudX2 = (cloudX2 + delta * 8f) % (GameConstants.VIRTUAL_WIDTH * 1.5f + 200f)

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // 1. Blue Ocean background
        shapeRenderer.color = GameConstants.COLOR_OCEAN
        shapeRenderer.rect(0f, 0f, GameConstants.VIRTUAL_WIDTH * 1.5f, GameConstants.VIRTUAL_HEIGHT * 1.5f)

        // 2. Beach / Sand Outline
        shapeRenderer.color = GameConstants.COLOR_ISLAND_SAND
        shapeRenderer.circle(380f, 720f, 330f)
        shapeRenderer.circle(280f, 580f, 270f)
        shapeRenderer.circle(550f, 880f, 220f)
        shapeRenderer.circle(460f, 500f, 220f)
        shapeRenderer.circle(580f, 420f, 200f)
        shapeRenderer.circle(700f, 680f, 220f)

        // 3. Green Island Base
        shapeRenderer.color = GameConstants.COLOR_ISLAND_GREEN
        shapeRenderer.circle(380f, 720f, 315f)
        shapeRenderer.circle(280f, 580f, 255f)
        shapeRenderer.circle(550f, 880f, 205f)
        shapeRenderer.circle(460f, 500f, 205f)
        shapeRenderer.circle(580f, 420f, 185f)
        shapeRenderer.circle(700f, 680f, 205f)

        // 4. Cobblestone paths connecting zones
        shapeRenderer.color = Color(0.82f, 0.72f, 0.58f, 1f)
        shapeRenderer.rectLine(200f, 620f, 380f, 740f, 16f)
        shapeRenderer.rectLine(380f, 740f, 550f, 880f, 16f)
        shapeRenderer.rectLine(380f, 740f, 460f, 500f, 16f)
        shapeRenderer.rectLine(460f, 500f, 580f, 420f, 16f)
        shapeRenderer.rectLine(550f, 880f, 700f, 680f, 16f)
        shapeRenderer.rectLine(580f, 420f, 700f, 680f, 16f)

        // 5. Decorative Trees & Rocks
        shapeRenderer.color = Color(0.12f, 0.48f, 0.18f, 1f)
        shapeRenderer.circle(120f, 820f, 35f)
        shapeRenderer.circle(260f, 950f, 40f)
        shapeRenderer.circle(620f, 960f, 38f)
        shapeRenderer.circle(360f, 420f, 32f)
        shapeRenderer.circle(760f, 520f, 36f)

        // 6. Zone Lock Fog overlay for locked zones
        for (zone in zonesMap.values) {
            if (!zone.isUnlocked) {
                val center = getZoneCenter(zone.zoneId)
                shapeRenderer.color = Color(0.2f, 0.25f, 0.35f, 0.55f)
                shapeRenderer.circle(center.first, center.second, 180f)
            }
        }

        // 7. Floating clouds
        shapeRenderer.color = Color(1f, 1f, 1f, 0.35f)
        shapeRenderer.circle(cloudX1 - 100f, 1100f, 45f)
        shapeRenderer.circle(cloudX1 - 70f, 1110f, 55f)
        shapeRenderer.circle(cloudX1 - 30f, 1100f, 40f)

        shapeRenderer.circle(cloudX2 - 100f, 400f, 35f)
        shapeRenderer.circle(cloudX2 - 70f, 410f, 45f)
        shapeRenderer.circle(cloudX2 - 30f, 400f, 30f)

        shapeRenderer.end()
    }

    private fun getZoneCenter(zoneId: Int): Pair<Float, Float> {
        return when (zoneId) {
            1 -> Pair(380f, 740f)
            2 -> Pair(200f, 620f)
            3 -> Pair(550f, 880f)
            4 -> Pair(520f, 460f)
            5 -> Pair(220f, 360f)
            6 -> Pair(700f, 680f)
            else -> Pair(380f, 740f)
        }
    }
}
