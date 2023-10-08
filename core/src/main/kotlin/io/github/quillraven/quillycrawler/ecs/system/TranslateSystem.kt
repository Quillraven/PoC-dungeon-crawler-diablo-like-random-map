package io.github.quillraven.quillycrawler.ecs.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import io.github.quillraven.quillycrawler.ecs.component.Boundary
import io.github.quillraven.quillycrawler.ecs.component.Translate

class TranslateSystem : IteratingSystem(family { all(Translate, Boundary) }) {

    override fun onTickEntity(entity: Entity) {
        val translateCmp = entity[Translate]
        val (interpolation, from, translation, speed, alpha) = translateCmp
        if (alpha >= 1f) {
            entity.configure { it -= Translate }
        }

        entity[Boundary].position.set(
            interpolation.apply(from.x, from.x + translation.x, alpha),
            interpolation.apply(from.y, from.y + translation.y, alpha)
        )

        translateCmp.alpha = (alpha + deltaTime * speed).coerceAtMost(1f)
    }

}
