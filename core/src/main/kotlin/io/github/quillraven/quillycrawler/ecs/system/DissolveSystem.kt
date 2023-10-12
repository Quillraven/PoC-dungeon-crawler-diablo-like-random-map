package io.github.quillraven.quillycrawler.ecs.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import io.github.quillraven.quillycrawler.ecs.component.Dissolve

class DissolveSystem : IteratingSystem(family { all(Dissolve) }) {

    override fun onTickEntity(entity: Entity) {
        val dissolveCmp = entity[Dissolve]
        dissolveCmp.value = (dissolveCmp.value + dissolveCmp.speed * deltaTime).coerceAtMost(1f)
    }

}
