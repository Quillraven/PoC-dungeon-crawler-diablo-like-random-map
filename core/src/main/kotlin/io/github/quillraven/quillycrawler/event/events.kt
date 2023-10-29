package io.github.quillraven.quillycrawler.event

import com.badlogic.gdx.maps.MapObject
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Entity
import io.github.quillraven.quillycrawler.ecs.CharacterType
import io.github.quillraven.quillycrawler.ecs.PropType
import io.github.quillraven.quillycrawler.ecs.component.MoveDirection
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

data class PlayerCollisionPropEvent(
    val player: Entity,
    val prop: Entity,
    val propId: Int,
    val propType: PropType,
    val position: Vector2
) : Event

data class PlayerCollisionCharacterEvent(
    val player: Entity,
    val character: Entity,
    val charId: Int,
    val charType: CharacterType,
    val position: Vector2
) : Event

data class PlayerMoveEvent(
    val direction: MoveDirection,
    val to: Vector2,
) : Event

data object EventDispatcher {
    private val listeners = mutableListOf<EventListener>()

    fun register(listener: EventListener) {
        listeners += listener
    }

    fun deRegister(listener: EventListener) {
        listeners -= listener
    }

    fun dispatch(event: Event) {
        // use index iteration instead of forEach to avoid
        // ConcurrentModification when adding/removing listeners during iteration
        for (i in 0 until listeners.size) {
            listeners[i].onEvent(event)
        }
    }
}
