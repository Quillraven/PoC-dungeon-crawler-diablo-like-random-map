package io.github.quillraven.quillycrawler.ecs.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import io.github.quillraven.quillycrawler.ecs.component.Graphic
import io.github.quillraven.quillycrawler.ecs.component.Scale

class ScaleSystem : IteratingSystem(family { all(Scale, Graphic) }) {

    override fun onTickEntity(entity: Entity) = with(entity[Scale]) {
        if (alpha >= 1f) {
            entity.configure { it -= Scale }
        }

        entity[Graphic].sprite.setScale(
            interpolation.apply(from.x, from.x + scaling.x, alpha),
            interpolation.apply(from.y, from.y + scaling.y, alpha)
        )

        alpha = (alpha + deltaTime * speed).coerceAtMost(1f)
    }

}
