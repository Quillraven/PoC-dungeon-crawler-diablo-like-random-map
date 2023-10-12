package io.github.quillraven.quillycrawler.ecs.component

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class Scale(
    val interpolation: Interpolation,
    val from: Vector2,
    val scaling: Vector2,
    val speed: Float = 1f,
    var alpha: Float = 0f,
) : Component<Scale> {
    override fun type() = Scale

    companion object : ComponentType<Scale>()
}
