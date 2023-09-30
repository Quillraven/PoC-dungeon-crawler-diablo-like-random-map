package io.github.quillraven.quillycrawler.ecs.component

import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import ktx.math.component1
import ktx.math.component2

data class Boundary(
    val position: Vector2,
    val size: Vector2,
    var rotation: Float = 0f, // in degrees
    val z: Int = 0
) : Component<Boundary>, Comparable<Boundary> {

    override fun type() = Boundary

    override fun compareTo(other: Boundary): Int {
        val (x, y) = position
        val (otherX, otherY) = other.position

        return when {
            z < other.z -> -1
            z > other.z -> 1
            y < otherY -> 1
            y > otherY -> -1
            x < otherX -> -1
            x > otherX -> 1
            else -> 0
        }
    }

    companion object : ComponentType<Boundary>()
}
