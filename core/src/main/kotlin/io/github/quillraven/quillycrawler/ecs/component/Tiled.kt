package io.github.quillraven.quillycrawler.ecs.component

import com.badlogic.gdx.maps.MapObject
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class Tiled(val mapObject: MapObject) : Component<Tiled> {
    override fun type() = Tiled

    companion object : ComponentType<Tiled>()
}
