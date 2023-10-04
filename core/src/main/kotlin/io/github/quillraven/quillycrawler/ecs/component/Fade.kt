package io.github.quillraven.quillycrawler.ecs.component

import com.badlogic.gdx.math.Interpolation
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class Fade(
    val interpolation: Interpolation,
    var fadeSpeed: Float = 1f,
    var alpha: Float = 0f,
) : Component<Fade> {
    override fun type() = Fade

    companion object : ComponentType<Fade>()
}
