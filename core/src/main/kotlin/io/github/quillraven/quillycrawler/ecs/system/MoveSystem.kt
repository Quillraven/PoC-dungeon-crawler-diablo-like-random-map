package io.github.quillraven.quillycrawler.ecs.system

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import io.github.quillraven.quillycrawler.QuillyCrawler.Companion.UNIT_SCALE
import io.github.quillraven.quillycrawler.ecs.CharacterType
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
    private var mapWidth = 0f
    private var mapHeight = 0f
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
                    checkPlayerMovementEnd(entity, position)
                } else {
                    // ... other entities stop their movement after one tile
                    direction = MoveDirection.NONE
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

    private fun Vector2.isWithinMap(): Boolean = x in 0f..<mapWidth && y in 0f..<mapHeight

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

        // check if target tile contains non-walkable prop
        currentMap?.prop(to)?.let { prop ->
            if (!(prop[Tiled].type as PropType).walkable) {
                // tile could be blocked by a chest and if the moving entity is a
                // player then we want to collect it -> trigger checkPlayerMovementEnd
                if (entity has Tags.PLAYER) {
                    checkPlayerMovementEnd(entity, to)
                }

                // prop is blocking tile -> stay at current position
                to.set(boundary.position)
                return
            }
        }

        if (entity has Tags.PLAYER) {
            EventDispatcher.dispatch(PlayerMoveEvent(direction, to))
            checkPlayerMovementBegin(entity, boundary, to)
            return
        }

        // restrict other character movement within map
        if (!to.isWithinMap()) {
            to.set(boundary.position)
            return
        }

        // check if there is already a character on the target tile (=char <-> char collision)
        if (currentMap?.character(to) != null) {
            to.set(boundary.position)
            return
        }

        // update entity location in DungeonMap
        currentMap?.moveCharacter(from, to)
    }

    private fun checkPlayerMovementBegin(player: Entity, boundary: Boundary, to: Vector2) {
        val scaledPlayerCenter = boundary.center(TMP_CENTER).div(UNIT_SCALE)
        if (to.isWithinMap()) {
            // player is not leaving map
            return
        }

        currentMap?.connection(scaledPlayerCenter)?.let { connection ->
            LOG.debug { "Player is leaving connection ${connection.id}" }
            EventDispatcher.dispatch(MapConnectionEvent(player, connection))
        }
    }

    private fun checkPlayerMovementEnd(player: Entity, position: Vector2) {
        currentMap?.character(position)?.let { character ->
            val (mapObject, type) = character[Tiled]
            val tiledId = mapObject.id
            val charType = type as CharacterType
            LOG.debug { "Colliding with character $charType and id $tiledId" }
            EventDispatcher.dispatch(PlayerCollisionCharacterEvent(player, character, tiledId, charType, position))
            world.remove(character, dissolveTime = 1.25f, scaleBy = vec2(1f, 1f), scaleTime = 1.5f)
            // TODO trigger combat -> CombatScreen
        }

        currentMap?.prop(position)?.let { prop ->
            val (mapObject, type) = prop[Tiled]
            val tiledId = mapObject.id
            val propType = type as PropType
            LOG.debug { "Colliding with prop $propType and id $tiledId" }
            EventDispatcher.dispatch(PlayerCollisionPropEvent(player, prop, tiledId, propType, position))

            if (propType.destructible) {
                world.remove(prop, fadeOutTime = 1.25f, translateBy = vec2(0f, 2f), translateTime = 1.75f)
            }

            when (propType) {
                PropType.COIN -> {
                    prop[Animation].speed = 5f
                    player[Inventory].coins++
                }

                PropType.KEY1 -> {
                    prop[Animation].speed = 5f
                    // TODO add golden key to player
                }

                PropType.KEY2 -> {
                    prop[Animation].speed = 5f
                    // TODO add silver key to player
                }

                PropType.BOX -> {
                    // TODO play open animation and add loot
                }

                PropType.CHEST -> {
                    // TODO play open animation and add loot
                }

                else -> Unit
            }
        }
    }

    override fun onEvent(event: Event) {
        when (event) {
            is MapLoadEvent -> {
                currentMap = event.dungeonMap
                mapWidth = event.dungeonMap.tiledMap.width.toFloat()
                mapHeight = event.dungeonMap.tiledMap.height.toFloat()
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
