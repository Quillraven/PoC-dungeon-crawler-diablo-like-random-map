package io.github.quillraven.quillycrawler.ecs.component

import com.badlogic.gdx.maps.MapObject
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import io.github.quillraven.quillycrawler.ecs.EntityType
import io.github.quillraven.quillycrawler.map.DungeonMap

/**
 * [tiledPosition] is used as a key in a [DungeonMap] data structure to quickly retrieve
 * an [Entity] at a specific tile. It is always rounded to an integer position instead of float.
 */
data class Tiled(
    val mapObject: MapObject,
    val type: EntityType,
    val tiledPosition: Vector2,
) : Component<Tiled> {
    override fun type() = Tiled

    companion object : ComponentType<Tiled>()
}
