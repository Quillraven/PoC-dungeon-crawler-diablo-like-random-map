package io.github.quillraven.quillycrawler.ecs.system

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import com.github.quillraven.fleks.collection.compareEntityBy
import io.github.quillraven.quillycrawler.QuillyCrawler.Companion.UNIT_SCALE
import io.github.quillraven.quillycrawler.ecs.component.Boundary
import io.github.quillraven.quillycrawler.ecs.component.Graphic
import io.github.quillraven.quillycrawler.event.*
import io.github.quillraven.quillycrawler.map.ConnectionType
import ktx.assets.disposeSafely
import ktx.graphics.use
import ktx.math.vec2
import ktx.tiled.totalHeight
import ktx.tiled.totalWidth
import ktx.tiled.x
import ktx.tiled.y

class RenderSystem(private val batch: Batch = inject(), private val viewport: Viewport = inject()) : IteratingSystem(
    family = family { all(Graphic, Boundary) },
    comparator = compareEntityBy(Boundary),
), EventListener {

    private val orthoCamera = viewport.camera as OrthographicCamera
    private val mapRenderer = OrthogonalTiledMapRenderer(null, UNIT_SCALE, batch)

    private var doTransition = false
    private val transitionMapRenderer = OrthogonalTiledMapRenderer(null, UNIT_SCALE, batch)
    private val mapFrom = vec2()
    private val mapTo = vec2()
    private val transitionMapFrom = vec2()
    private val transitionMapTo = vec2()
    private val transitionMapOffset = vec2()
    private var transitionInterpolation = Interpolation.linear
    private var transitionAlpha = 0f
    private var transitionSpeed = 1f

    override fun onTick() {
        viewport.apply()

        if (mapRenderer.map != null) {
            mapRenderer.setView(orthoCamera)
            mapRenderer.render()
        }

        if (doTransition) {
            transitionAlpha = (transitionAlpha + deltaTime * transitionSpeed).coerceAtMost(1f)

            orthoCamera.position.x =
                transitionInterpolation.apply(transitionMapFrom.x, transitionMapTo.x, transitionAlpha)
            orthoCamera.position.y =
                transitionInterpolation.apply(transitionMapFrom.y, transitionMapTo.y, transitionAlpha)
            orthoCamera.update()

            if (transitionMapRenderer.map != null) {
                transitionMapRenderer.setView(orthoCamera)
                transitionMapRenderer.render()

                orthoCamera.position.x = transitionInterpolation.apply(mapFrom.x, mapTo.x, transitionAlpha)
                orthoCamera.position.y = transitionInterpolation.apply(mapFrom.y, mapTo.y, transitionAlpha)
                orthoCamera.update()

                if (transitionAlpha == 1f) {
                    EventDispatcher.dispatch(MapTransitionStopEvent(transitionMapOffset.cpy()))
                }
            }

            doTransition = transitionAlpha < 1f
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
        when (event) {
            is MapLoadEvent -> mapRenderer.map = event.dungeonMap.tiledMap
            is MapTransitionStartEvent -> onMapTransitionStart(event)
            is MapTransitionStopEvent -> onMapTransitionStop()

            else -> Unit
        }
    }

    private fun onMapTransitionStop() {
        if (transitionMapOffset.isZero) return

        doTransition = true
        transitionAlpha = 0f
        transitionInterpolation = Interpolation.bounceOut
        transitionSpeed = 0.6f
        mapFrom.set(mapTo)
        transitionMapFrom.set(transitionMapTo)
        transitionMapTo.set(orthoCamera.viewportWidth * 0.5f, orthoCamera.viewportHeight * 0.5f)

        mapRenderer.map = transitionMapRenderer.map
        transitionMapRenderer.map = null
    }

    private fun onMapTransitionStart(event: MapTransitionStartEvent) {
        transitionMapRenderer.map = event.toMap.tiledMap
        val scaledMapWidth = event.toMap.tiledMap.totalWidth() * UNIT_SCALE
        val scaledMapHeight = event.toMap.tiledMap.totalHeight() * UNIT_SCALE
        transitionMapOffset.set(
            (event.fromConnection.x - event.toConnection.x) * UNIT_SCALE,
            (event.fromConnection.y - event.toConnection.y) * UNIT_SCALE
        )

        doTransition = true
        transitionAlpha = 0f
        transitionSpeed = 0.8f
        transitionInterpolation = Interpolation.fade
        mapFrom.set(orthoCamera.position.x, orthoCamera.position.y)
        mapTo.set(mapFrom)
        // new map always starts at (0,0) again
        transitionMapTo.set(orthoCamera.viewportWidth * 0.5f, orthoCamera.viewportHeight * 0.5f)
        transitionMapFrom.set(transitionMapTo)

        when (event.connectionType) {
            ConnectionType.LEFT -> {
                mapTo.x -= scaledMapWidth
                transitionMapFrom.x += scaledMapWidth
                transitionMapOffset.x = 0f
                transitionMapFrom.y -= transitionMapOffset.y
                transitionMapTo.y -= transitionMapOffset.y
            }

            ConnectionType.RIGHT -> {
                mapTo.x += scaledMapWidth
                transitionMapFrom.x -= scaledMapWidth
                transitionMapOffset.x = 0f
                transitionMapFrom.y -= transitionMapOffset.y
                transitionMapTo.y -= transitionMapOffset.y
            }

            ConnectionType.DOWN -> {
                mapTo.y -= scaledMapHeight
                transitionMapFrom.y += scaledMapHeight
                transitionMapOffset.y = 0f
                transitionMapFrom.x -= transitionMapOffset.x
                transitionMapTo.x -= transitionMapOffset.x
            }

            ConnectionType.UP -> {
                mapTo.y += scaledMapHeight
                transitionMapFrom.y -= scaledMapHeight
                transitionMapOffset.y = 0f
                transitionMapFrom.x -= transitionMapOffset.x
                transitionMapTo.x -= transitionMapOffset.x
            }
        }
    }

    override fun onDispose() {
        mapRenderer.disposeSafely()
        transitionMapRenderer.disposeSafely()
    }
}
