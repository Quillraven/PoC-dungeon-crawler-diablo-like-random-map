package io.github.quillraven.quillycrawler.ecs.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import io.github.quillraven.quillycrawler.ecs.component.Fade
import io.github.quillraven.quillycrawler.ecs.component.Graphic

class FadeSystem : IteratingSystem(family { all(Fade, Graphic) }) {
    override fun onTickEntity(entity: Entity) = with(entity[Fade]) {
        if (alpha >= 1f) {
            entity.configure { it -= Fade }
        }

        val (sprite) = entity[Graphic]
        sprite.setAlpha(interpolation.apply(from, to, alpha))
        alpha = (alpha + deltaTime * speed).coerceAtMost(1f)
    }
}
