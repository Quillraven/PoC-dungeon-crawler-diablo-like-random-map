package io.github.quillraven.quillycrawler.ecs.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import io.github.quillraven.quillycrawler.ecs.component.Remove

class RemoveSystem : IteratingSystem(family { all(Remove) }) {

    override fun onTickEntity(entity: Entity) {
        val removeCmp = entity[Remove]
        removeCmp.time -= deltaTime
        if (removeCmp.time <= 0f) {
            entity.remove()
        }
    }

}
