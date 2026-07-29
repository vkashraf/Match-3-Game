package com.example.game.match3.effect

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.MathUtils
import com.example.utils.TextureFactory

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var color: Color,
    var size: Float,
    var alpha: Float = 1f,
    var elapsed: Float = 0f,
    var lifetime: Float = 0.5f
) {
    val isDead: Boolean get() = elapsed >= lifetime

    fun update(delta: Float) {
        elapsed += delta
        x += vx * delta
        y += vy * delta
        vy -= 120f * delta // gravity
        alpha = (1f - elapsed / lifetime).coerceIn(0f, 1f)
    }
}

class ParticleEffectManager {
    private val particles = mutableListOf<Particle>()

    fun spawnExplosion(x: Float, y: Float, color: Color = Color.GOLD, count: Int = 12) {
        for (i in 0 until count) {
            val angle = MathUtils.random(0f, 6.283185f)
            val speed = MathUtils.random(60f, 240f)
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = MathUtils.cos(angle) * speed,
                    vy = MathUtils.sin(angle) * speed,
                    color = color.cpy(),
                    size = MathUtils.random(6f, 12f),
                    lifetime = MathUtils.random(0.3f, 0.6f)
                )
            )
        }
    }

    fun spawnRocketTrail(x: Float, y: Float, isHorizontal: Boolean) {
        for (i in 0..3) {
            val vx = if (isHorizontal) MathUtils.random(-120f, 120f) else MathUtils.random(-20f, 20f)
            val vy = if (!isHorizontal) MathUtils.random(-120f, 120f) else MathUtils.random(-20f, 20f)
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = vx,
                    vy = vy,
                    color = Color(1f, 0.85f, 0.2f, 1f),
                    size = MathUtils.random(4f, 8f),
                    lifetime = 0.3f
                )
            )
        }
    }

    fun update(delta: Float) {
        val it = particles.iterator()
        while (it.hasNext()) {
            val p = it.next()
            p.update(delta)
            if (p.isDead) it.remove()
        }
    }

    fun render(batch: Batch) {
        if (particles.isEmpty()) return

        val pixelTexture = TextureFactory.getOrCreateTexture("p_pixel", 2, 2) { pixmap ->
            pixmap.setColor(Color.WHITE)
            pixmap.fill()
        }
        for (p in particles) {
            batch.setColor(p.color.r, p.color.g, p.color.b, p.alpha)
            batch.draw(pixelTexture, p.x - p.size / 2f, p.y - p.size / 2f, p.size, p.size)
        }
        batch.setColor(1f, 1f, 1f, 1f)
    }
}
