package io.github.quillraven.quillycrawler.ecs.component

import com.badlogic.gdx.maps.MapObject
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import io.github.quillraven.quillycrawler.ecs.EntityType

data class Tiled(val mapObject: MapObject, val type: EntityType) : Component<Tiled> {
    override fun type() = Tiled

    companion object : ComponentType<Tiled>()
}
