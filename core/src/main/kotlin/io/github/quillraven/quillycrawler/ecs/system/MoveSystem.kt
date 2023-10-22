package io.github.quillraven.quillycrawler.ecs.system

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import io.github.quillraven.quillycrawler.QuillyCrawler.Companion.UNIT_SCALE
import io.github.quillraven.quillycrawler.ecs.PropType
import io.github.quillraven.quillycrawler.ecs.component.*
import io.github.quillraven.quillycrawler.ecs.remove
import io.github.quillraven.quillycrawler.event.*
import io.github.quillraven.quillycrawler.map.DungeonMap
import ktx.log.logger
import ktx.math.component1
import ktx.math.component2
import ktx.math.div
import ktx.math.vec2
import ktx.tiled.*

class MoveSystem : IteratingSystem(family { all(Move, Boundary).none(Remove) }), EventListener {

    private var currentMap: DungeonMap? = null
    private val playerEntities = world.family { all(Tags.PLAYER) }

    override fun onTickEntity(entity: Entity) = with(entity[Move]) {
        if (alpha == 0f && direction == MoveDirection.NONE) {
            return@with
        }

        val boundaryCmp = entity[Boundary]
        val (position) = boundaryCmp
        when (alpha) {
            0f -> updateTarget(entity, boundaryCmp, from, to, direction)
            1f -> {
                // target reached ...
                if (entity has Tags.ROOT) {
                    // ... and entity is rooted -> lock it in place
                    return
                } else if (entity has Tags.PLAYER) {
                    // ... and entity is player -> check for char/prop collision
                    checkPlayerMovementEnd(entity, boundaryCmp)
                }

                alpha = 0f
                if (direction == MoveDirection.NONE) {
                    // ... and no more direction to go
                    return
                }
                updateTarget(entity, boundaryCmp, from, to, direction)
            }
        }

        alpha = (alpha + deltaTime * 3f).coerceAtMost(1f)
        val (fromX, fromY) = from
        val (toX, toY) = to
        position.set(
            MathUtils.lerp(fromX, toX, alpha),
            MathUtils.lerp(fromY, toY, alpha)
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
            checkPlayerMovementBegin(entity, boundary, to)
        }
    }

    private fun checkPlayerMovementBegin(player: Entity, boundary: Boundary, to: Vector2) {
        val scaledPlayerCenter = boundary.center(TMP_CENTER).div(UNIT_SCALE)
        val mapWidth = currentMap?.tiledMap?.width?.toFloat() ?: 0f
        val mapHeight = currentMap?.tiledMap?.height?.toFloat() ?: 0f
        if (to.x in 0f..<mapWidth && to.y in 0f..<mapHeight) {
            // player is not leaving map
            return
        }

        currentMap?.connection(scaledPlayerCenter)?.let { connection ->
            LOG.debug { "Player is leaving connection ${connection.id}" }
            EventDispatcher.dispatch(MapConnectionEvent(player, connection))
        }
    }

    private fun checkPlayerMovementEnd(player: Entity, boundary: Boundary) {
        currentMap?.character(boundary.position)?.let { character ->
            val tiledId = character[Tiled].mapObject.id
            LOG.debug { "Colliding with character $tiledId" }
            EventDispatcher.dispatch(PlayerCollisionCharacterEvent(player, character, tiledId, boundary.position))
            world.remove(character, dissolveTime = 1.25f, scaleBy = vec2(1f, 1f), scaleTime = 1.5f)
            // TODO trigger combat -> CombatScreen
        }

        currentMap?.prop(boundary.position)?.let { prop ->
            val tiledId = prop[Tiled].mapObject.id
            LOG.debug { "Colliding with prop $tiledId" }
            EventDispatcher.dispatch(PlayerCollisionPropEvent(player, prop, tiledId, boundary.position))

            when (prop[Tiled].type) {
                PropType.COIN -> {
                    world.remove(prop, fadeOutTime = 1.25f, translateBy = vec2(0f, 2f), translateTime = 1.75f)
                    prop[Animation].speed = 5f
                    player[Inventory].coins++
                }

                else -> Unit
            }
        }
    }

    override fun onEvent(event: Event) {
        when (event) {
            is MapLoadEvent -> {
                currentMap = event.dungeonMap
            }

            is MapTransitionStartEvent -> playerEntities.forEach { player -> player.configure { it += Tags.ROOT } }

            is MapTransitionStopEvent -> playerEntities.forEach { player -> player.configure { it -= Tags.ROOT } }

            else -> Unit
        }
    }

    companion object {
        private val LOG = logger<MoveSystem>()
        private val TMP_CENTER = vec2()
    }

}
