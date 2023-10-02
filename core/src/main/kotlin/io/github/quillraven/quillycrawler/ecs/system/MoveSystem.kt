package io.github.quillraven.quillycrawler.ecs.system

import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import io.github.quillraven.quillycrawler.ecs.component.Boundary
import io.github.quillraven.quillycrawler.ecs.component.Move
import io.github.quillraven.quillycrawler.ecs.component.MoveDirection
import io.github.quillraven.quillycrawler.event.Event
import io.github.quillraven.quillycrawler.event.EventListener
import io.github.quillraven.quillycrawler.event.MapLoadEvent
import ktx.math.component1
import ktx.math.component2
import ktx.tiled.forEachLayer
import ktx.tiled.property

class MoveSystem : IteratingSystem(family { all(Move, Boundary) }), EventListener {

    private var currentMap: TiledMap? = null

    override fun onTickEntity(entity: Entity) {
        val moveCmp = entity[Move]
        val (direction, from, to, alpha) = moveCmp
        if (alpha == 0f && direction == MoveDirection.NONE) {
            return
        }

        val (position) = entity[Boundary]
        when (alpha) {
            0f -> updateTarget(position, from, to, direction)
            1f -> {
                // target reached
                moveCmp.alpha = 0f
                if (direction == MoveDirection.NONE) {
                    // ... and no more direction to go
                    return
                }
                updateTarget(position, from, to, direction)
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
        position: Vector2,
        from: Vector2,
        to: Vector2,
        direction: MoveDirection
    ) {
        from.set(position)
        to.set(position)
        when (direction) {
            MoveDirection.UP -> to.y += 1
            MoveDirection.DOWN -> to.y -= 1
            MoveDirection.LEFT -> to.x -= 1
            MoveDirection.RIGHT -> to.x += 1
            else -> Unit
        }

        // check if target tile is walkable
        currentMap?.let { tiledMap ->
            tiledMap.forEachLayer<TiledMapTileLayer> { layer ->
                val tile = layer.getCell(to.x.toInt(), to.y.toInt())?.tile ?: return@forEachLayer
                if (!tile.property("walkable", true)) {
                    // tile is not walkable -> stay at current position
                    to.set(position)
                    return
                }
            }
        }
    }

    override fun onEvent(event: Event) {
        if (event is MapLoadEvent) {
            currentMap = event.tiledMap
        }
    }

}
