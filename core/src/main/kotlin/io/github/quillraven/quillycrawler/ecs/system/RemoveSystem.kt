package io.github.quillraven.quillycrawler.ecs.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import io.github.quillraven.quillycrawler.ecs.component.Remove
import ktx.log.logger

class RemoveSystem : IteratingSystem(family { all(Remove) }) {

    override fun onTickEntity(entity: Entity) = with(entity[Remove]) {
        time -= deltaTime
        if (time <= 0f) {
            LOG.debug { "Entity $entity removed via delay." }
            entity.remove()
        }
    }

    companion object {
        private val LOG = logger<RemoveSystem>()
    }

}
