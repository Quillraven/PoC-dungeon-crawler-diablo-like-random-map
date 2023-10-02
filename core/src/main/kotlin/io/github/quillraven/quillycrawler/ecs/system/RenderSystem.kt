package io.github.quillraven.quillycrawler.ecs.system

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import com.github.quillraven.fleks.collection.compareEntityBy
import io.github.quillraven.quillycrawler.QuillyCrawler.Companion.UNIT_SCALE
import io.github.quillraven.quillycrawler.ecs.component.Boundary
import io.github.quillraven.quillycrawler.ecs.component.Graphic
import io.github.quillraven.quillycrawler.event.Event
import io.github.quillraven.quillycrawler.event.EventListener
import io.github.quillraven.quillycrawler.event.MapLoadEvent
import ktx.assets.disposeSafely
import ktx.graphics.use

class RenderSystem(private val batch: Batch = inject(), private val viewport: Viewport = inject()) : IteratingSystem(
    family = family { all(Graphic, Boundary) },
    comparator = compareEntityBy(Boundary),
), EventListener {

    private val orthoCamera = viewport.camera as OrthographicCamera
    private val mapRenderer = OrthogonalTiledMapRenderer(null, UNIT_SCALE, batch)

    override fun onTick() {
        viewport.apply()

        if (mapRenderer.map != null) {
            mapRenderer.setView(orthoCamera)
            mapRenderer.render()
        }

        batch.use(orthoCamera) {
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

    override fun onEvent(event: Event) {
        if (event is MapLoadEvent) {
            mapRenderer.map = event.tiledMap
        }
    }

    override fun onDispose() {
        mapRenderer.disposeSafely()
    }
}
