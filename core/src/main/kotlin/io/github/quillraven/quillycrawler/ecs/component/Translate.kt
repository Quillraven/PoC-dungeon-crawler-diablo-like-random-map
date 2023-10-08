package io.github.quillraven.quillycrawler.ecs.component

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class Translate(
    val interpolation: Interpolation,
    val from: Vector2,
    val translation: Vector2,
    val speed: Float = 1f,
    var alpha: Float = 0f,
) : Component<Translate> {
    override fun type() = Translate

    companion object : ComponentType<Translate>()
}
