package io.github.quillraven.quillycrawler.controller

import com.github.quillraven.fleks.World
import io.github.quillraven.quillycrawler.ecs.component.MoveDirection
import io.github.quillraven.quillycrawler.ecs.component.Tags
import io.github.quillraven.quillycrawler.ecs.moveTo

sealed interface ControlAction {
    fun execute()
}

data class ControlActionMove(
    val world: World,
    val direction: MoveDirection,
) : ControlAction {

    private val playerEntities = world.family { all(Tags.PLAYER) }

    override fun execute() {
        playerEntities.forEach { world.moveTo(it, direction) }
    }
}
