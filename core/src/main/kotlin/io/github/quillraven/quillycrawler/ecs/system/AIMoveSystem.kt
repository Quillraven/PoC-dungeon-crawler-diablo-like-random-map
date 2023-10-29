package io.github.quillraven.quillycrawler.ecs.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import io.github.quillraven.quillycrawler.ecs.component.*
import io.github.quillraven.quillycrawler.event.Event
import io.github.quillraven.quillycrawler.event.EventListener
import io.github.quillraven.quillycrawler.event.PlayerMoveEvent

class AIMoveSystem : IteratingSystem(
    enabled = false,
    family = family { all(AIMove, Move, Boundary).none(Remove, Tags.PLAYER) }
), EventListener {

    override fun onTickEntity(entity: Entity) = Unit

    override fun onEvent(event: Event) {
        // TODO add logic for different move types

        if (event is PlayerMoveEvent) {
            family.forEach { entity ->
                // if character is at target tile of player -> don't move
                if (entity[Boundary].position.epsilonEquals(event.to, 0.5f)) return@forEach

                val (moveType) = entity[AIMove]
                when (moveType) {
                    AIMoveType.RANDOM -> entity[Move].direction = MoveDirection.random()
                    AIMoveType.LINE -> entity[Move].direction = MoveDirection.random()
                    AIMoveType.CIRCLE -> entity[Move].direction = MoveDirection.random()
                    AIMoveType.TO_PLAYER -> entity[Move].direction = MoveDirection.random()
                }
            }
        }
    }

}
