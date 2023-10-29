package io.github.quillraven.quillycrawler.controller

import com.github.quillraven.fleks.World
import io.github.quillraven.quillycrawler.ecs.component.Move
import io.github.quillraven.quillycrawler.ecs.component.MoveDirection
import io.github.quillraven.quillycrawler.ecs.component.Tags

sealed interface ControlAction {
    fun execute()
}

data class ControlActionMove(
    val world: World,
    val targetDirection: MoveDirection,
) : ControlAction {

    private val playerEntities = world.family { all(Tags.PLAYER) }

    override fun execute() {
        playerEntities.forEach { it.getOrNull(Move)?.direction = targetDirection }
    }
}
