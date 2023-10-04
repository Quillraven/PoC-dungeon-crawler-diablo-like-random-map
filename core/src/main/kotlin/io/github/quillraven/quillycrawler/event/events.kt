package io.github.quillraven.quillycrawler.event

import com.badlogic.gdx.maps.MapObject
import com.github.quillraven.fleks.Entity
import io.github.quillraven.quillycrawler.map.ConnectionType
import io.github.quillraven.quillycrawler.map.DungeonMap

interface EventListener {
    fun onEvent(event: Event)
}

sealed interface Event

data class MapLoadEvent(val dungeonMap: DungeonMap) : Event

data class MapConnectionEvent(val player: Entity, val connection: MapObject) : Event

data class MapTransitionStartEvent(
    val toMap: DungeonMap,
    val connectionType: ConnectionType,
    val fromConnection: MapObject,
    val toConnection: MapObject
) : Event

data object MapTransitionStopEvent : Event

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
