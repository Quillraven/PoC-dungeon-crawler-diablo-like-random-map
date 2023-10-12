package io.github.quillraven.quillycrawler.ecs.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import io.github.quillraven.quillycrawler.ecs.component.Graphic
import io.github.quillraven.quillycrawler.ecs.component.Scale

class ScaleSystem : IteratingSystem(family { all(Scale, Graphic) }) {

    override fun onTickEntity(entity: Entity) {
        val scaleCmp = entity[Scale]
        val (interpolation, from, scaling, speed, alpha) = scaleCmp
        if (alpha >= 1f) {
            entity.configure { it -= Scale }
        }

        entity[Graphic].sprite.setScale(
            interpolation.apply(from.x, from.x + scaling.x, alpha),
            interpolation.apply(from.y, from.y + scaling.y, alpha)
        )

        scaleCmp.alpha = (alpha + deltaTime * speed).coerceAtMost(1f)
    }

}
