package io.github.quillraven.quillycrawler.event

import com.badlogic.gdx.maps.tiled.TiledMap

interface EventListener {
    fun onEvent(event: Event)
}

sealed interface Event

data class MapLoadEvent(val tiledMap: TiledMap) : Event

data object EventDispatcher {
    private val listeners = mutableListOf<EventListener>()

    fun register(listener: EventListener) {
        listeners += listener
    }

    fun deRegister(listener: EventListener) {
        listeners -= listener
    }

    fun dispatch(event: Event) {
        listeners.forEach { it.onEvent(event) }
    }
}
