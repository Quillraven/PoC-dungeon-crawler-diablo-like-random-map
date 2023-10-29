package io.github.quillraven.quillycrawler.ecs.component

import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import ktx.math.vec2

enum class MoveDirection {
    NONE, LEFT, RIGHT, UP, DOWN;

    companion object {
        private val ALL_DIRECTIONS = listOf(LEFT, RIGHT, UP, DOWN)

        fun random(): MoveDirection = ALL_DIRECTIONS.random()
    }
}

data class Move(
    var direction: MoveDirection,
    val from: Vector2 = vec2(),
    val to: Vector2 = vec2(),
    var alpha: Float = 0f,
) : Component<Move> {
    override fun type() = Move

    companion object : ComponentType<Move>()
}
