package io.github.quillraven.quillycrawler.ecs.system

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import io.github.quillraven.quillycrawler.QuillyCrawler.Companion.UNIT_SCALE
import io.github.quillraven.quillycrawler.ecs.component.Boundary
import io.github.quillraven.quillycrawler.ecs.component.Move
import io.github.quillraven.quillycrawler.ecs.component.MoveDirection
import io.github.quillraven.quillycrawler.ecs.component.Tags
import io.github.quillraven.quillycrawler.event.*
import io.github.quillraven.quillycrawler.map.DungeonMap
import ktx.log.logger
import ktx.math.component1
import ktx.math.component2
import ktx.math.div
import ktx.math.vec2
import ktx.tiled.forEachLayer
import ktx.tiled.id
import ktx.tiled.property
import ktx.tiled.shape

class MoveSystem : IteratingSystem(family { all(Move, Boundary) }), EventListener {

    private var currentMap: DungeonMap? = null

    override fun onTickEntity(entity: Entity) {
        val moveCmp = entity[Move]
        val (direction, from, to, alpha) = moveCmp
        if (alpha == 0f && direction == MoveDirection.NONE) {
            return
        }

        val boundaryCmp = entity[Boundary]
        val (position) = boundaryCmp
        when (alpha) {
            0f -> updateTarget(entity, boundaryCmp, from, to, direction)
            1f -> {
                // target reached
                moveCmp.alpha = 0f
                if (direction == MoveDirection.NONE) {
                    // ... and no more direction to go
                    return
                }
                updateTarget(entity, boundaryCmp, from, to, direction)
            }
        }

        moveCmp.alpha = (moveCmp.alpha + deltaTime * 3f).coerceAtMost(1f)
        val (fromX, fromY) = from
        val (toX, toY) = to
        position.set(
            MathUtils.lerp(fromX, toX, moveCmp.alpha),
            MathUtils.lerp(fromY, toY, moveCmp.alpha)
        )
    }

    private fun updateTarget(
        entity: Entity,
        boundary: Boundary,
        from: Vector2,
        to: Vector2,
        direction: MoveDirection
    ) {
        from.set(boundary.position)
        to.set(boundary.position)
        when (direction) {
            MoveDirection.UP -> to.y += 1
            MoveDirection.DOWN -> to.y -= 1
            MoveDirection.LEFT -> to.x -= 1
            MoveDirection.RIGHT -> to.x += 1
            else -> Unit
        }

        // check if target tile is walkable
        currentMap?.tiledMap?.let { tiledMap ->
            tiledMap.forEachLayer<TiledMapTileLayer> { layer ->
                val tile = layer.getCell(to.x.toInt(), to.y.toInt())?.tile ?: return@forEachLayer
                if (!tile.property("walkable", true)) {
                    // tile is not walkable -> stay at current position
                    to.set(boundary.position)
                    return
                }
            }
        }

        if (entity has Tags.PLAYER) {
            checkPlayerMovement(entity, boundary)
        }
    }

    private fun checkPlayerMovement(entity: Entity, boundary: Boundary) {
        val scaledPlayerCenter = boundary.center(TMP_CENTER).div(UNIT_SCALE)
        currentMap?.connections
            ?.firstOrNull { scaledPlayerCenter in it.shape }
            ?.let { connection ->
                LOG.debug { "Player is leaving connection ${connection.id}" }
                EventDispatcher.dispatch(MapConnectionEvent(entity, connection))
            }
    }

    override fun onEvent(event: Event) {
        if (event is MapLoadEvent) {
            currentMap = event.dungeonMap
        }
    }

    companion object {
        private val LOG = logger<MoveSystem>()
        private val TMP_CENTER = vec2()
    }

}
