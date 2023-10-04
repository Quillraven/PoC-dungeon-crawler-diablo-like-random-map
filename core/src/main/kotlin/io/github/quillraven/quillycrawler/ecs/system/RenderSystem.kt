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
import ktx.graphics.update
import ktx.graphics.use
import ktx.math.vec2
import ktx.tiled.totalHeight
import ktx.tiled.totalWidth
import ktx.tiled.x
import ktx.tiled.y
import kotlin.math.min

class RenderSystem(private val batch: Batch = inject(), private val viewport: Viewport = inject()) : IteratingSystem(
    family = family { all(Graphic, Boundary) },
    comparator = compareEntityBy(Boundary),
), EventListener {

    private val orthoCamera = viewport.camera as OrthographicCamera
    private val mapRenderer = OrthogonalTiledMapRenderer(null, UNIT_SCALE, batch)

    private var doTransition = false
    private var transitionInterpolation = Interpolation.linear
    private var transitionAlpha = 0f
    private val transitionMapRenderer = OrthogonalTiledMapRenderer(null, UNIT_SCALE, batch)
    private val transitionFrom = vec2()
    private val transitionTo = vec2()
    private val transitionOffset = vec2()

    override fun onTick() {
        viewport.apply()

        if (doTransition) {
            transitionAlpha = (transitionAlpha + deltaTime * TRANSITION_SPEED).coerceAtMost(1f)

            orthoCamera.position.x = transitionInterpolation.apply(transitionFrom.x, transitionTo.x, transitionAlpha)
            orthoCamera.position.y = transitionInterpolation.apply(transitionFrom.y, transitionTo.y, transitionAlpha)

            if (transitionMapRenderer.map != null) {
                orthoCamera.update {
                    position.x -= transitionOffset.x
                    position.y += transitionOffset.y
                }

                transitionMapRenderer.setView(orthoCamera)
                transitionMapRenderer.render()
            }

            orthoCamera.update {
                position.x += transitionOffset.x
                position.y -= transitionOffset.y
            }

            mapRenderer.setView(orthoCamera)
            val origAlpha = batch.color.a
            batch.color.a = 1f - transitionAlpha
            mapRenderer.render()
            batch.color.a = origAlpha

            doTransition = transitionAlpha < 1f
            if (!doTransition) {
                // transition ended -> change to new active map for rendering
                val (_, _, camW, camH) = orthoCamera
                orthoCamera.update { position.set(camW * 0.5f, camH * 0.5f, 0f) }
                mapRenderer.map = transitionMapRenderer.map
                transitionMapRenderer.map = null
                EventDispatcher.dispatch(MapTransitionStopEvent)
            }
        } else if (mapRenderer.map != null) {
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
        when (event) {
            is MapLoadEvent -> mapRenderer.map = event.dungeonMap.tiledMap
            is MapTransitionStartEvent -> onMapTransitionStart(event)

            else -> Unit
        }
    }

    private fun onMapTransitionStart(event: MapTransitionStartEvent) {
        transitionMapRenderer.map = event.toMap.tiledMap
        val scaledMapWidth = event.toMap.tiledMap.totalWidth() * UNIT_SCALE
        val scaledMapHeight = event.toMap.tiledMap.totalHeight() * UNIT_SCALE
        val mapDiff = vec2(
            (event.fromConnection.x - event.toConnection.x) * UNIT_SCALE,
            (event.fromConnection.y - event.toConnection.y) * UNIT_SCALE
        )
        val (camX, camY, camW, camH) = orthoCamera
        val distToPan = vec2(min(scaledMapWidth, camW), min(scaledMapHeight, camH))

        doTransition = true
        transitionAlpha = 0f
        transitionInterpolation = Interpolation.fade

        transitionFrom.set(camX, camY)
        transitionTo.set(transitionFrom)

        when (event.connectionType) {
            ConnectionType.LEFT -> {
                transitionTo.x -= distToPan.x
                transitionTo.y += mapDiff.y
                transitionOffset.set(-distToPan.x, -mapDiff.y)
            }

            ConnectionType.RIGHT -> {
                transitionTo.x += distToPan.x
                transitionTo.y += mapDiff.y
                transitionOffset.set(distToPan.x, -mapDiff.y)
            }

            ConnectionType.DOWN -> {
                transitionTo.x += mapDiff.x
                transitionTo.y -= distToPan.y
                transitionOffset.set(mapDiff.x, distToPan.y)
            }

            ConnectionType.UP -> {
                transitionTo.x += mapDiff.x
                transitionTo.y += distToPan.y
                transitionOffset.set(mapDiff.x, -distToPan.y)
            }
        }
    }

    override fun onDispose() {
        mapRenderer.disposeSafely()
        transitionMapRenderer.disposeSafely()
    }

    private operator fun OrthographicCamera.component1() = this.position.x

    private operator fun OrthographicCamera.component2() = this.position.y

    private operator fun OrthographicCamera.component3() = this.viewportWidth

    private operator fun OrthographicCamera.component4() = this.viewportHeight

    companion object {
        const val TRANSITION_SPEED = 0.8f
    }
}
