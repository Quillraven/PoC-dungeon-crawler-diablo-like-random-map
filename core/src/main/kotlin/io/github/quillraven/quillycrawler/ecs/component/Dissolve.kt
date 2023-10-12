package io.github.quillraven.quillycrawler.ecs.component

import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import io.github.quillraven.quillycrawler.QuillyCrawler.Companion.UNIT_SCALE
import ktx.math.vec2

data class Dissolve(
    val speed: Float,
    val uvOffset: Vector2,
    val uvMax: Vector2,
    val numFragments: Vector2,
    var value: Float = 0f // 0..1
) : Component<Dissolve> {
    override fun type() = Dissolve

    companion object : ComponentType<Dissolve>() {
        fun of(sprite: Sprite, speed: Float): Dissolve {
            // numFragments is equal to the amount of pixels of the sprite. This gives a nice
            // effect for our pixelated graphics.
            val numFragments = vec2(sprite.width / UNIT_SCALE, sprite.height / UNIT_SCALE)
            val uvOffset = vec2(sprite.u, sprite.v)
            val uvMax = vec2(sprite.u2, sprite.v2)
            return Dissolve(speed, uvOffset, uvMax, numFragments)
        }
    }
}
