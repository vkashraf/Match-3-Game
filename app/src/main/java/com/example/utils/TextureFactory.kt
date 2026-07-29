package com.example.utils

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable

object TextureFactory {

    private val textureCache = mutableMapOf<String, Texture>()

    fun getOrCreateTexture(key: String, width: Int, height: Int, drawBlock: (Pixmap) -> Unit): Texture {
        return textureCache.getOrPut(key) {
            val pixmap = Pixmap(width, height, Pixmap.Format.RGBA8888)
            drawBlock(pixmap)
            val texture = Texture(pixmap)
            pixmap.dispose()
            texture
        }
    }

    /**
     * Creates a rounded rectangle texture with a border and glossy gradient.
     */
    fun createRoundedPanel(
        width: Int,
        height: Int,
        fillColor: Color,
        borderColor: Color,
        borderThickness: Int = 4,
        cornerRadius: Int = 16
    ): TextureRegionDrawable {
        val key = "panel_${width}_${height}_${fillColor}_${borderColor}_$borderThickness"
        val texture = getOrCreateTexture(key, width, height) { pixmap ->
            pixmap.blending = Pixmap.Blending.SourceOver
            pixmap.setColor(0f, 0f, 0f, 0f)
            pixmap.fill()

            // Draw shadow/border
            pixmap.setColor(borderColor)
            pixmap.fillRectangle(0, 0, width, height)

            // Inner fill
            pixmap.setColor(fillColor)
            pixmap.fillRectangle(
                borderThickness,
                borderThickness,
                width - borderThickness * 2,
                height - borderThickness * 2
            )

            // Glossy highlight line on top
            pixmap.setColor(1f, 1f, 1f, 0.25f)
            pixmap.fillRectangle(
                borderThickness + 2,
                borderThickness + 2,
                width - (borderThickness + 2) * 2,
                (height - borderThickness * 2) / 3
            )
        }
        return TextureRegionDrawable(TextureRegion(texture))
    }

    /**
     * Creates a glossy circle texture (e.g. for icons or avatar frame).
     */
    fun createCircleTexture(size: Int, fillColor: Color, borderColor: Color): TextureRegionDrawable {
        val key = "circle_${size}_${fillColor}_$borderColor"
        val texture = getOrCreateTexture(key, size, size) { pixmap ->
            pixmap.blending = Pixmap.Blending.SourceOver
            pixmap.setColor(0f, 0f, 0f, 0f)
            pixmap.fill()

            val radius = size / 2
            pixmap.setColor(borderColor)
            pixmap.fillCircle(radius, radius, radius)

            pixmap.setColor(fillColor)
            pixmap.fillCircle(radius, radius, radius - 4)

            // Highlight arc / reflection
            pixmap.setColor(1f, 1f, 1f, 0.3f)
            pixmap.fillCircle(radius, radius / 2, radius / 3)
        }
        return TextureRegionDrawable(TextureRegion(texture))
    }

    /**
     * Creates procedural icon drawables for Coins, Gems, Energy, Play, etc.
     */
    fun createIcon(type: String, size: Int = 48): TextureRegionDrawable {
        val key = "icon_${type}_$size"
        val texture = getOrCreateTexture(key, size, size) { pixmap ->
            pixmap.blending = Pixmap.Blending.SourceOver
            pixmap.setColor(0f, 0f, 0f, 0f)
            pixmap.fill()

            val center = size / 2
            when (type.lowercase()) {
                "coin" -> {
                    // Outer gold
                    pixmap.setColor(1.0f, 0.78f, 0.15f, 1f)
                    pixmap.fillCircle(center, center, center - 2)
                    // Inner rim
                    pixmap.setColor(0.85f, 0.6f, 0.1f, 1f)
                    pixmap.drawCircle(center, center, center - 6)
                    // Star/Symbol center
                    pixmap.setColor(1f, 0.95f, 0.5f, 1f)
                    pixmap.fillRectangle(center - 3, center - 8, 6, 16)
                    pixmap.fillRectangle(center - 8, center - 3, 16, 6)
                }
                "gem" -> {
                    // Diamond shape
                    pixmap.setColor(0.78f, 0.25f, 0.95f, 1f)
                    pixmap.fillCircle(center, center, center - 4)
                    pixmap.setColor(0.9f, 0.5f, 1f, 1f)
                    pixmap.fillRectangle(center - 6, center - 6, 12, 12)
                }
                "energy" -> {
                    // Lightning bolt
                    pixmap.setColor(0.2f, 0.82f, 1.0f, 1f)
                    pixmap.fillCircle(center, center, center - 3)
                    pixmap.setColor(1f, 1f, 1f, 1f)
                    pixmap.fillRectangle(center - 2, center - 10, 8, 12)
                    pixmap.fillRectangle(center - 6, center, 8, 12)
                }
                "gift" -> {
                    pixmap.setColor(0.9f, 0.2f, 0.3f, 1f)
                    pixmap.fillRectangle(4, 12, size - 8, size - 16)
                    pixmap.setColor(1.0f, 0.8f, 0.2f, 1f)
                    pixmap.fillRectangle(center - 3, 12, 6, size - 16)
                    pixmap.fillRectangle(4, center - 3, size - 8, 6)
                }
                "clipboard" -> {
                    pixmap.setColor(0.6f, 0.4f, 0.2f, 1f)
                    pixmap.fillRectangle(6, 6, size - 12, size - 12)
                    pixmap.setColor(0.95f, 0.95f, 0.9f, 1f)
                    pixmap.fillRectangle(10, 10, size - 20, size - 16)
                    pixmap.setColor(0.2f, 0.2f, 0.2f, 1f)
                    pixmap.fillRectangle(14, 16, size - 28, 4)
                    pixmap.fillRectangle(14, 24, size - 28, 4)
                }
                "events" -> {
                    pixmap.setColor(1.0f, 0.6f, 0.1f, 1f)
                    pixmap.fillCircle(center, center, center - 4)
                    pixmap.setColor(1.0f, 0.9f, 0.3f, 1f)
                    pixmap.fillCircle(center, center, center - 10)
                }
                "shop" -> {
                    pixmap.setColor(0.2f, 0.6f, 0.9f, 1f)
                    pixmap.fillRectangle(4, 16, size - 8, size - 20)
                    pixmap.setColor(0.9f, 0.3f, 0.3f, 1f)
                    pixmap.fillRectangle(2, 8, size - 4, 8)
                }
                "inventory" -> {
                    pixmap.setColor(0.55f, 0.35f, 0.2f, 1f)
                    pixmap.fillRectangle(6, 12, size - 12, size - 18)
                    pixmap.setColor(0.85f, 0.7f, 0.3f, 1f)
                    pixmap.fillRectangle(center - 4, 18, 8, 8)
                }
                "menu" -> {
                    pixmap.setColor(1f, 1f, 1f, 1f)
                    pixmap.fillRectangle(10, 12, size - 20, 5)
                    pixmap.fillRectangle(10, 21, size - 20, 5)
                    pixmap.fillRectangle(10, 30, size - 20, 5)
                }
                else -> {
                    pixmap.setColor(0.8f, 0.8f, 0.8f, 1f)
                    pixmap.fillCircle(center, center, center - 4)
                }
            }
        }
        return TextureRegionDrawable(TextureRegion(texture))
    }

    /**
     * Generates custom building artwork textures for all building types and construction/plots.
     */
    fun createBuildingTexture(type: String, width: Int = 120, height: Int = 120): TextureRegionDrawable {
        val key = "building_${type.lowercase()}_${width}_$height"
        val texture = getOrCreateTexture(key, width, height) { pixmap ->
            pixmap.blending = Pixmap.Blending.SourceOver
            pixmap.setColor(0f, 0f, 0f, 0f)
            pixmap.fill()

            val cx = width / 2
            when (type.lowercase()) {
                "house" -> {
                    // Base wall
                    pixmap.setColor(0.88f, 0.78f, 0.65f, 1f)
                    pixmap.fillRectangle(cx - 35, height - 60, 70, 45)
                    // Blue roof
                    pixmap.setColor(0.15f, 0.42f, 0.78f, 1f)
                    pixmap.fillRectangle(cx - 45, height - 85, 90, 30)
                    // Door & Window
                    pixmap.setColor(0.4f, 0.25f, 0.15f, 1f)
                    pixmap.fillRectangle(cx - 10, height - 35, 20, 25)
                    pixmap.setColor(0.9f, 0.9f, 0.4f, 1f)
                    pixmap.fillRectangle(cx + 15, height - 55, 12, 12)
                }
                "farm" -> {
                    // Dirt patch
                    pixmap.setColor(0.5f, 0.35f, 0.2f, 1f)
                    pixmap.fillRectangle(cx - 45, height - 45, 90, 35)
                    // Green crops
                    pixmap.setColor(0.3f, 0.75f, 0.2f, 1f)
                    pixmap.fillCircle(cx - 25, height - 30, 12)
                    pixmap.fillCircle(cx, height - 30, 12)
                    pixmap.fillCircle(cx + 25, height - 30, 12)
                    // Windmill post & blades
                    pixmap.setColor(0.8f, 0.7f, 0.5f, 1f)
                    pixmap.fillRectangle(cx - 6, height - 90, 12, 45)
                    pixmap.setColor(0.95f, 0.9f, 0.8f, 1f)
                    pixmap.fillRectangle(cx - 25, height - 85, 50, 6)
                    pixmap.fillRectangle(cx - 3, height - 105, 6, 50)
                }
                "mine" -> {
                    // Mountain rock cave
                    pixmap.setColor(0.45f, 0.48f, 0.52f, 1f)
                    pixmap.fillCircle(cx, height - 40, 45)
                    // Cave opening
                    pixmap.setColor(0.15f, 0.15f, 0.18f, 1f)
                    pixmap.fillCircle(cx, height - 30, 22)
                    // Gold sparkle & rail track
                    pixmap.setColor(1.0f, 0.8f, 0.2f, 1f)
                    pixmap.fillRectangle(cx - 8, height - 25, 8, 8)
                    pixmap.setColor(0.6f, 0.4f, 0.2f, 1f)
                    pixmap.fillRectangle(cx - 15, height - 15, 30, 6)
                }
                "workshop" -> {
                    // Brick building
                    pixmap.setColor(0.72f, 0.35f, 0.28f, 1f)
                    pixmap.fillRectangle(cx - 40, height - 60, 80, 50)
                    // Chimney & Gear icon
                    pixmap.setColor(0.35f, 0.35f, 0.35f, 1f)
                    pixmap.fillRectangle(cx + 18, height - 85, 15, 30)
                    pixmap.setColor(1f, 0.8f, 0.2f, 1f)
                    pixmap.fillCircle(cx - 15, height - 40, 12)
                }
                "bakery" -> {
                    // Warm pink/orange Bakery
                    pixmap.setColor(0.95f, 0.7f, 0.55f, 1f)
                    pixmap.fillRectangle(cx - 38, height - 60, 76, 48)
                    // Red awning
                    pixmap.setColor(0.85f, 0.25f, 0.25f, 1f)
                    pixmap.fillRectangle(cx - 42, height - 70, 84, 15)
                    // Bread sign
                    pixmap.setColor(0.85f, 0.6f, 0.2f, 1f)
                    pixmap.fillCircle(cx, height - 40, 10)
                }
                "market" -> {
                    // Market stalls
                    pixmap.setColor(0.6f, 0.4f, 0.25f, 1f)
                    pixmap.fillRectangle(cx - 42, height - 50, 84, 40)
                    // Striped roof
                    pixmap.setColor(0.2f, 0.6f, 0.85f, 1f)
                    pixmap.fillRectangle(cx - 45, height - 72, 90, 22)
                    pixmap.setColor(1f, 1f, 1f, 0.9f)
                    pixmap.fillRectangle(cx - 30, height - 72, 12, 22)
                    pixmap.fillRectangle(cx, height - 72, 12, 22)
                    pixmap.fillRectangle(cx + 30, height - 72, 12, 22)
                }
                "lumber_mill" -> {
                    // Wood log mill
                    pixmap.setColor(0.5f, 0.32f, 0.18f, 1f)
                    pixmap.fillRectangle(cx - 40, height - 55, 80, 45)
                    // Saw blade
                    pixmap.setColor(0.8f, 0.82f, 0.85f, 1f)
                    pixmap.fillCircle(cx - 15, height - 35, 14)
                    pixmap.setColor(0.4f, 0.25f, 0.12f, 1f)
                    pixmap.fillRectangle(cx + 5, height - 30, 25, 12)
                }
                "harbor" -> {
                    // Wooden pier & boat
                    pixmap.setColor(0.45f, 0.3f, 0.18f, 1f)
                    pixmap.fillRectangle(cx - 45, height - 40, 90, 25)
                    // Water blue underneath
                    pixmap.setColor(0.2f, 0.55f, 0.85f, 1f)
                    pixmap.fillRectangle(cx - 45, height - 18, 90, 18)
                    // Ship sail
                    pixmap.setColor(0.95f, 0.95f, 0.9f, 1f)
                    pixmap.fillTriangle(cx, height - 85, cx + 25, height - 45, cx, height - 45)
                    pixmap.setColor(0.3f, 0.2f, 0.1f, 1f)
                    pixmap.fillRectangle(cx - 2, height - 85, 4, 42)
                }
                "laboratory" -> {
                    // Science Lab dome
                    pixmap.setColor(0.3f, 0.45f, 0.6f, 1f)
                    pixmap.fillCircle(cx, height - 50, 42)
                    pixmap.setColor(0.2f, 0.85f, 0.95f, 0.8f)
                    pixmap.fillCircle(cx, height - 55, 25)
                    // Glowing beaker symbol
                    pixmap.setColor(0.3f, 0.95f, 0.4f, 1f)
                    pixmap.fillRectangle(cx - 6, height - 50, 12, 12)
                }
                "magic_tower" -> {
                    // Purple Wizard Tower
                    pixmap.setColor(0.4f, 0.25f, 0.6f, 1f)
                    pixmap.fillRectangle(cx - 25, height - 70, 50, 60)
                    // Cone roof
                    pixmap.setColor(0.7f, 0.3f, 0.85f, 1f)
                    pixmap.fillTriangle(cx - 32, height - 70, cx + 32, height - 70, cx, height - 110)
                    // Magic crystal ball on top
                    pixmap.setColor(0.3f, 0.8f, 1f, 1f)
                    pixmap.fillCircle(cx, height - 112, 10)
                }
                "scaffolding" -> {
                    // Wooden construction scaffolding with hammer
                    pixmap.setColor(0.6f, 0.45f, 0.25f, 1f)
                    // Vertical poles
                    pixmap.fillRectangle(cx - 35, height - 80, 8, 70)
                    pixmap.fillRectangle(cx + 27, height - 80, 8, 70)
                    // Horizontal planks
                    pixmap.fillRectangle(cx - 40, height - 75, 80, 6)
                    pixmap.fillRectangle(cx - 40, height - 45, 80, 6)
                    pixmap.fillRectangle(cx - 40, height - 15, 80, 6)
                    // Hammer in center
                    pixmap.setColor(0.85f, 0.85f, 0.9f, 1f)
                    pixmap.fillRectangle(cx - 12, height - 52, 24, 10)
                    pixmap.setColor(0.5f, 0.3f, 0.15f, 1f)
                    pixmap.fillRectangle(cx - 3, height - 42, 6, 20)
                }
                "plot_empty" -> {
                    // Wooden sign board "BUILD HERE"
                    pixmap.setColor(0.65f, 0.45f, 0.25f, 1f)
                    pixmap.fillRectangle(cx - 30, height - 50, 60, 35)
                    pixmap.setColor(0.45f, 0.3f, 0.15f, 1f)
                    pixmap.drawRectangle(cx - 30, height - 50, 60, 35)
                    pixmap.fillRectangle(cx - 4, height - 15, 8, 15)
                    // Plus symbol
                    pixmap.setColor(0.2f, 0.75f, 0.25f, 1f)
                    pixmap.fillRectangle(cx - 3, height - 42, 6, 20)
                    pixmap.fillRectangle(cx - 10, height - 35, 20, 6)
                }
                "plot_locked" -> {
                    // Lock icon on stone pedestal
                    pixmap.setColor(0.5f, 0.5f, 0.55f, 1f)
                    pixmap.fillCircle(cx, height - 35, 28)
                    pixmap.setColor(0.85f, 0.7f, 0.2f, 1f)
                    pixmap.fillRectangle(cx - 10, height - 32, 20, 16)
                    pixmap.setColor(0.6f, 0.6f, 0.65f, 1f)
                    pixmap.drawCircle(cx, height - 38, 8)
                }
                else -> {
                    pixmap.setColor(0.8f, 0.8f, 0.8f, 1f)
                    pixmap.fillRectangle(cx - 30, height - 50, 60, 40)
                }
            }
        }
        return TextureRegionDrawable(TextureRegion(texture))
    }

    /**
     * Generates an original friendly character illustration avatar texture.
     */
    fun createCharacterAvatar(size: Int = 100): TextureRegionDrawable {
        val key = "avatar_character_$size"
        val texture = getOrCreateTexture(key, size, size) { pixmap ->
            pixmap.blending = Pixmap.Blending.SourceOver
            pixmap.setColor(0f, 0f, 0f, 0f)
            pixmap.fill()

            val r = size / 2
            // Background
            pixmap.setColor(0.2f, 0.6f, 0.9f, 1f)
            pixmap.fillCircle(r, r, r)

            // Face
            pixmap.setColor(0.98f, 0.85f, 0.72f, 1f)
            pixmap.fillCircle(r, r + 5, r - 18)

            // Hair
            pixmap.setColor(0.35f, 0.22f, 0.15f, 1f)
            pixmap.fillCircle(r, r - 12, r - 16)

            // Eyes
            pixmap.setColor(0.15f, 0.15f, 0.15f, 1f)
            pixmap.fillCircle(r - 10, r, 4)
            pixmap.fillCircle(r + 10, r, 4)

            // Smile
            pixmap.setColor(0.85f, 0.3f, 0.3f, 1f)
            pixmap.fillRectangle(r - 8, r + 14, 16, 5)
        }
        return TextureRegionDrawable(TextureRegion(texture))
    }

    fun createTileTexture(type: String, size: Int = 80): TextureRegionDrawable {
        val key = "tile_${type}_$size"
        val texture = getOrCreateTexture(key, size, size) { pixmap ->
            pixmap.blending = Pixmap.Blending.SourceOver
            pixmap.setColor(0f, 0f, 0f, 0f)
            pixmap.fill()

            val cx = size / 2
            val cy = size / 2
            val pad = 4
            val innerSize = size - pad * 2

            when (type.uppercase()) {
                "RED_HEART" -> {
                    // Glossy Red Heart
                    pixmap.setColor(0.2f, 0f, 0f, 0.3f)
                    pixmap.fillCircle(cx, cy + 3, innerSize / 2 - 2) // Shadow

                    pixmap.setColor(0.95f, 0.2f, 0.3f, 1f)
                    pixmap.fillCircle(cx - innerSize / 4, cy - innerSize / 6, innerSize / 4 + 2)
                    pixmap.fillCircle(cx + innerSize / 4, cy - innerSize / 6, innerSize / 4 + 2)
                    
                    // Triangle base
                    val x1 = cx - innerSize / 2 + 2
                    val y1 = cy - innerSize / 8
                    val x2 = cx + innerSize / 2 - 2
                    val y2 = cy - innerSize / 8
                    val x3 = cx
                    val y3 = cy + innerSize / 2 - 2
                    pixmap.fillTriangle(x1, y1, x2, y2, x3, y3)

                    // Highlights
                    pixmap.setColor(1f, 0.7f, 0.8f, 0.7f)
                    pixmap.fillCircle(cx - innerSize / 4, cy - innerSize / 4, innerSize / 8)
                }
                "YELLOW_STAR" -> {
                    // Glossy Yellow Star
                    pixmap.setColor(0.2f, 0.2f, 0f, 0.3f)
                    pixmap.fillCircle(cx, cy + 3, innerSize / 2 - 2) // Shadow

                    pixmap.setColor(1.0f, 0.85f, 0.1f, 1f)
                    pixmap.fillCircle(cx, cy, innerSize / 2 - 4)

                    // Star arms cross
                    pixmap.setColor(1.0f, 0.95f, 0.3f, 1f)
                    pixmap.fillRectangle(cx - 5, cy - innerSize / 2 + 4, 10, innerSize - 8)
                    pixmap.fillRectangle(cx - innerSize / 2 + 4, cy - 5, innerSize - 8, 10)

                    // Highlight
                    pixmap.setColor(1f, 1f, 0.8f, 0.8f)
                    pixmap.fillCircle(cx - 6, cy - 6, 6)
                }
                "BLUE_DROP" -> {
                    // Glossy Water Drop
                    pixmap.setColor(0f, 0.1f, 0.3f, 0.3f)
                    pixmap.fillCircle(cx, cy + 3, innerSize / 2 - 2)

                    pixmap.setColor(0.15f, 0.65f, 0.98f, 1f)
                    pixmap.fillCircle(cx, cy + 4, innerSize / 3)

                    val x1 = cx - innerSize / 3
                    val y1 = cy + 4
                    val x2 = cx + innerSize / 3
                    val y2 = cy + 4
                    val x3 = cx
                    val y3 = cy - innerSize / 2 + 4
                    pixmap.fillTriangle(x1, y1, x2, y2, x3, y3)

                    // Highlight
                    pixmap.setColor(0.8f, 0.95f, 1f, 0.8f)
                    pixmap.fillCircle(cx - 5, cy, 5)
                }
                "PURPLE_GEM" -> {
                    // Hexagonal Purple Gem
                    pixmap.setColor(0.1f, 0f, 0.2f, 0.3f)
                    pixmap.fillCircle(cx, cy + 3, innerSize / 2 - 2)

                    pixmap.setColor(0.68f, 0.22f, 0.88f, 1f)
                    pixmap.fillRectangle(cx - innerSize / 3, cy - innerSize / 2 + 4, innerSize * 2 / 3, innerSize - 8)

                    pixmap.setColor(0.82f, 0.42f, 0.98f, 1f)
                    pixmap.fillRectangle(cx - innerSize / 4, cy - innerSize / 3, innerSize / 2, innerSize * 2 / 3)

                    // Highlight
                    pixmap.setColor(0.95f, 0.8f, 1f, 0.8f)
                    pixmap.fillRectangle(cx - innerSize / 4 + 2, cy - innerSize / 3 + 2, 6, 6)
                }
                "GREEN_LEAF" -> {
                    // Emerald Green Leaf
                    pixmap.setColor(0f, 0.2f, 0f, 0.3f)
                    pixmap.fillCircle(cx, cy + 3, innerSize / 2 - 2)

                    pixmap.setColor(0.2f, 0.82f, 0.35f, 1f)
                    pixmap.fillCircle(cx - 4, cy - 4, innerSize / 3)
                    pixmap.fillCircle(cx + 4, cy + 4, innerSize / 3)

                    // Leaf vein
                    pixmap.setColor(0.6f, 0.95f, 0.5f, 0.9f)
                    pixmap.fillRectangle(cx - innerSize / 3, cy - 2, innerSize * 2 / 3, 4)

                    // Highlight
                    pixmap.setColor(0.8f, 1f, 0.8f, 0.7f)
                    pixmap.fillCircle(cx - 6, cy - 6, 5)
                }
                "ORANGE_GEM" -> {
                    // Diamond Orange Gem
                    pixmap.setColor(0.2f, 0.1f, 0f, 0.3f)
                    pixmap.fillCircle(cx, cy + 3, innerSize / 2 - 2)

                    pixmap.setColor(1.0f, 0.55f, 0.1f, 1f)
                    pixmap.fillCircle(cx, cy, innerSize / 2 - 3)

                    pixmap.setColor(1.0f, 0.75f, 0.3f, 1f)
                    pixmap.fillRectangle(cx - innerSize / 4, cy - innerSize / 4, innerSize / 2, innerSize / 2)

                    // Highlight
                    pixmap.setColor(1f, 0.95f, 0.8f, 0.8f)
                    pixmap.fillCircle(cx - 5, cy - 5, 5)
                }
            }
        }
        return TextureRegionDrawable(TextureRegion(texture))
    }

    fun createSpecialOverlayTexture(specialTypeStr: String, size: Int = 80): TextureRegionDrawable {
        val key = "special_overlay_${specialTypeStr}_$size"
        val texture = getOrCreateTexture(key, size, size) { pixmap ->
            pixmap.blending = Pixmap.Blending.SourceOver
            pixmap.setColor(0f, 0f, 0f, 0f)
            pixmap.fill()

            val cx = size / 2
            val cy = size / 2

            when (specialTypeStr.uppercase()) {
                "ROCKET_HORIZONTAL" -> {
                    pixmap.setColor(1f, 0.9f, 0.2f, 0.9f)
                    // Draw horizontal arrow
                    pixmap.fillRectangle(cx - size / 3, cy - 4, size * 2 / 3, 8)
                    pixmap.fillTriangle(cx + size / 3, cy - 10, cx + size / 3, cy + 10, cx + size / 2 - 2, cy)
                    pixmap.fillTriangle(cx - size / 3, cy - 10, cx - size / 3, cy + 10, cx - size / 2 + 2, cy)
                }
                "ROCKET_VERTICAL" -> {
                    pixmap.setColor(1f, 0.9f, 0.2f, 0.9f)
                    // Draw vertical arrow
                    pixmap.fillRectangle(cx - 4, cy - size / 3, 8, size * 2 / 3)
                    pixmap.fillTriangle(cx - 10, cy + size / 3, cx + 10, cy + size / 3, cx, cy + size / 2 - 2)
                    pixmap.fillTriangle(cx - 10, cy - size / 3, cx + 10, cy - size / 3, cx, cy - size / 2 + 2)
                }
                "BOMB" -> {
                    // Cartoon Bomb
                    pixmap.setColor(0.15f, 0.15f, 0.18f, 1f)
                    pixmap.fillCircle(cx, cy + 2, size / 3)
                    // Fuse
                    pixmap.setColor(0.8f, 0.6f, 0.3f, 1f)
                    pixmap.fillRectangle(cx - 2, cy - size / 3 - 4, 4, 8)
                    // Fuse Spark
                    pixmap.setColor(1.0f, 0.3f, 0.1f, 1f)
                    pixmap.fillCircle(cx, cy - size / 3 - 6, 4)
                    pixmap.setColor(1.0f, 0.9f, 0.2f, 1f)
                    pixmap.fillCircle(cx, cy - size / 3 - 6, 2)
                }
                "RAINBOW" -> {
                    // Multi-color Crystal Orb
                    pixmap.setColor(0.9f, 0.2f, 0.2f, 1f)
                    pixmap.fillCircle(cx, cy, size / 2 - 4)
                    pixmap.setColor(1.0f, 0.8f, 0.2f, 1f)
                    pixmap.fillCircle(cx, cy, size / 2 - 10)
                    pixmap.setColor(0.2f, 0.8f, 0.3f, 1f)
                    pixmap.fillCircle(cx, cy, size / 2 - 16)
                    pixmap.setColor(0.2f, 0.6f, 1.0f, 1f)
                    pixmap.fillCircle(cx, cy, size / 2 - 22)
                }
            }
        }
        return TextureRegionDrawable(TextureRegion(texture))
    }

    fun createObstacleTexture(typeStr: String, size: Int = 80): TextureRegionDrawable {
        val key = "obstacle_${typeStr}_$size"
        val texture = getOrCreateTexture(key, size, size) { pixmap ->
            pixmap.blending = Pixmap.Blending.SourceOver
            pixmap.setColor(0f, 0f, 0f, 0f)
            pixmap.fill()

            val pad = 4
            val innerSize = size - pad * 2

            when (typeStr.uppercase()) {
                "CRATE" -> {
                    // Wooden Crate
                    pixmap.setColor(0.58f, 0.38f, 0.22f, 1f)
                    pixmap.fillRectangle(pad, pad, innerSize, innerSize)
                    pixmap.setColor(0.42f, 0.26f, 0.12f, 1f)
                    pixmap.drawRectangle(pad, pad, innerSize, innerSize)
                    // Diagonal Planks
                    pixmap.setColor(0.48f, 0.3f, 0.16f, 1f)
                    pixmap.fillRectangle(pad + 4, pad + 4, innerSize - 8, 6)
                    pixmap.fillRectangle(pad + 4, pad + innerSize - 10, innerSize - 8, 6)
                    pixmap.fillRectangle(pad + 4, pad + innerSize / 2 - 3, innerSize - 8, 6)
                }
                "ICE" -> {
                    // Translucent Ice Block
                    pixmap.setColor(0.5f, 0.85f, 1.0f, 0.55f)
                    pixmap.fillRectangle(pad, pad, innerSize, innerSize)
                    pixmap.setColor(0.8f, 0.95f, 1.0f, 0.8f)
                    pixmap.drawRectangle(pad, pad, innerSize, innerSize)
                    // Crystalline highlights
                    pixmap.setColor(1.0f, 1.0f, 1.0f, 0.7f)
                    pixmap.fillTriangle(pad + 4, pad + 4, pad + 20, pad + 4, pad + 4, pad + 20)
                }
                "VINE" -> {
                    // Leafy Vine border
                    pixmap.setColor(0.15f, 0.6f, 0.2f, 0.85f)
                    pixmap.drawRectangle(pad, pad, innerSize, innerSize)
                    pixmap.drawRectangle(pad + 1, pad + 1, innerSize - 2, innerSize - 2)
                    pixmap.setColor(0.25f, 0.8f, 0.3f, 1f)
                    pixmap.fillCircle(pad + 8, pad + 8, 6)
                    pixmap.fillCircle(pad + innerSize - 8, pad + 8, 6)
                    pixmap.fillCircle(pad + 8, pad + innerSize - 8, 6)
                    pixmap.fillCircle(pad + innerSize - 8, pad + innerSize - 8, 6)
                }
                "LOCKED" -> {
                    // Dark Padlocked Chain Frame
                    pixmap.setColor(0.1f, 0.1f, 0.15f, 0.75f)
                    pixmap.fillRectangle(pad, pad, innerSize, innerSize)
                    pixmap.setColor(0.9f, 0.75f, 0.2f, 1f)
                    val cx = size / 2
                    val cy = size / 2
                    pixmap.fillRectangle(cx - 8, cy - 2, 16, 14) // Lock body
                    pixmap.setColor(0.7f, 0.7f, 0.75f, 1f)
                    pixmap.drawCircle(cx, cy - 6, 8) // Lock shackle
                }
            }
        }
        return TextureRegionDrawable(TextureRegion(texture))
    }


    fun createBoosterIcon(type: String, size: Int = 48): TextureRegionDrawable {
        val key = "booster_${type}_$size"
        val texture = getOrCreateTexture(key, size, size) { pixmap ->
            pixmap.blending = Pixmap.Blending.SourceOver
            pixmap.setColor(0f, 0f, 0f, 0f)
            pixmap.fill()

            val cx = size / 2
            val cy = size / 2

            // Circle background
            pixmap.setColor(0.18f, 0.28f, 0.45f, 0.9f)
            pixmap.fillCircle(cx, cy, cx - 2)
            pixmap.setColor(0.4f, 0.6f, 0.9f, 1f)
            pixmap.drawCircle(cx, cy, cx - 2)

            when (type.lowercase()) {
                "hammer" -> {
                    pixmap.setColor(0.7f, 0.45f, 0.2f, 1f)
                    pixmap.fillRectangle(cx - 3, cy, 6, size / 2 - 4) // handle
                    pixmap.setColor(0.8f, 0.8f, 0.85f, 1f)
                    pixmap.fillRectangle(cx - 12, cy - 10, 24, 12) // head
                }
                "rocket" -> {
                    pixmap.setColor(0.9f, 0.2f, 0.2f, 1f)
                    pixmap.fillRectangle(cx - 5, cy - 12, 10, 20)
                    pixmap.setColor(1.0f, 0.8f, 0.2f, 1f)
                    pixmap.fillTriangle(cx - 8, cy - 12, cx + 8, cy - 12, cx, cy - 20)
                }
                "rainbow" -> {
                    pixmap.setColor(0.9f, 0.2f, 0.2f, 1f)
                    pixmap.fillCircle(cx, cy, 14)
                    pixmap.setColor(1.0f, 0.8f, 0.2f, 1f)
                    pixmap.fillCircle(cx, cy, 10)
                    pixmap.setColor(0.2f, 0.7f, 0.9f, 1f)
                    pixmap.fillCircle(cx, cy, 6)
                }
            }
        }
        return TextureRegionDrawable(TextureRegion(texture))
    }

    fun dispose() {
        textureCache.values.forEach { it.dispose() }
        textureCache.clear()
    }
}
