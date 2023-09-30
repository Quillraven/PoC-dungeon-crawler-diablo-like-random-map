package io.github.quillraven.quillycrawler.ecs.system

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import com.github.quillraven.fleks.collection.compareEntityBy
import io.github.quillraven.quillycrawler.ecs.component.Boundary
import io.github.quillraven.quillycrawler.ecs.component.Graphic
import ktx.graphics.use

class RenderSystem(private val batch: Batch = inject(), private val viewport: Viewport = inject()) : IteratingSystem(
    family = family { all(Graphic, Boundary) },
    comparator = compareEntityBy(Boundary),
) {

    override fun onTick() {
        viewport.apply()
        batch.use(viewport.camera) {
            super.onTick()
        }
    }

    override fun onTickEntity(entity: Entity) {
        val (sprite) = entity[Graphic]
        val (position, size, rotation) = entity[Boundary]

        sprite.setBounds(position.x, position.y, size.x, size.y)
        sprite.setOriginCenter()
        sprite.rotation = rotation
        sprite.draw(batch)
    }
}
