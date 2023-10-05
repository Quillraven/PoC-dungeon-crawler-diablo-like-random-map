package io.github.quillraven.quillycrawler.ecs.system

import com.badlogic.gdx.graphics.OrthographicCamera
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import io.github.quillraven.quillycrawler.ecs.component.Boundary
import io.github.quillraven.quillycrawler.ecs.component.Tags
import io.github.quillraven.quillycrawler.event.*
import ktx.graphics.update
import ktx.log.logger
import ktx.math.vec2
import ktx.tiled.height
import ktx.tiled.width
import kotlin.math.max
import kotlin.math.min

class CameraSystem(private val camera: OrthographicCamera = inject()) :
    IteratingSystem(family { all(Tags.CAMERA_LOCK, Boundary) }), EventListener {

    private var mapW = 0f
    private var mapH = 0f
    private val entityCenter = vec2()

    override fun onTickEntity(entity: Entity) {
        if (family.numEntities > 1) {
            LOG.error { "There are multiple camera locked entities" }
        }

        focusEntity(entity)
    }

    private fun focusEntity(entity: Entity) {
        entity[Boundary].center(entityCenter)
        val camW = camera.viewportWidth * 0.5f
        val camH = camera.viewportHeight * 0.5f
        val minX = min(camW, mapW - camW).coerceAtLeast(camW)
        val maxX = max(camW, mapW - camW).coerceAtLeast(camW)
        val minY = min(camH, mapH - camH).coerceAtLeast(camH)
        val maxY = max(camH, mapH - camH).coerceAtLeast(camH)
        camera.update {
            position.x = entityCenter.x.coerceIn(minX, maxX)
            position.y = entityCenter.y.coerceIn(minY, maxY)
        }
    }

    override fun onEvent(event: Event) {
        when (event) {
            is MapLoadEvent -> {
                mapW = event.dungeonMap.tiledMap.width.toFloat()
                mapH = event.dungeonMap.tiledMap.height.toFloat()
            }

            is MapTransitionStartEvent -> this.enabled = false

            is MapTransitionStopEvent -> {
                this.enabled = true
                // We need to focus the entity before the system's 'onTick' gets triggered because it can happen
                // that a player is immediately leaving the new map and returning to the old one. In that
                // case the CameraSystem will not run because the MoveSystem is running first and it triggers
                // the MapTransitionStartEvent. This results in an incorrect rendering for the map transition.
                family.firstOrNull()?.let { focusEntity(it) }
            }

            else -> Unit
        }
    }

    companion object {
        private val LOG = logger<CameraSystem>()
    }

}
