package io.github.quillraven.quillycrawler.map

import com.badlogic.gdx.maps.MapObject
import com.badlogic.gdx.maps.MapObjects
import com.badlogic.gdx.maps.tiled.TiledMap
import io.github.quillraven.quillycrawler.QuillyCrawler.Companion.UNIT_SCALE
import io.github.quillraven.quillycrawler.assets.TiledMapAssets
import io.github.quillraven.quillycrawler.event.EventDispatcher
import io.github.quillraven.quillycrawler.event.MapTransitionStartEvent
import ktx.app.gdxError
import ktx.log.logger
import ktx.tiled.layer
import ktx.tiled.totalWidth

enum class ConnectionType {
    LEFT, RIGHT, UP, DOWN;

    fun oppositeOf(type: ConnectionType): Boolean = when (type) {
        LEFT -> this == RIGHT
        RIGHT -> this == LEFT
        UP -> this == DOWN
        DOWN -> this == UP
    }
}

class DungeonMap(val tiledMap: TiledMap) {

    private val toMapConnections = mutableMapOf<MapObject, DungeonMap>()
    val connections: MapObjects = tiledMap.layer("connections").objects

    fun connect(connection: MapObject, allMaps: Map<TiledMapAssets, DungeonMap>) {
        val type = connectionType(connection)
        lateinit var targetConnection: MapObject
        val potentialMaps = allMaps.filter { mapEntry ->
            val matchingConnection = mapEntry.value.connections.firstOrNull { connectionType(it).oppositeOf(type) }
            if (matchingConnection != null) {
                targetConnection = matchingConnection
            }
            mapEntry.value != this && matchingConnection != null
        }

        LOG.debug { "Connecting with type $type to ${potentialMaps.size} potential map(s)" }
        val nextMap = potentialMaps.entries.randomOrNull() ?: gdxError("No connecting map for type $type")
        toMapConnections[connection] = nextMap.value
        nextMap.value.toMapConnections[targetConnection] = this
        LOG.debug { "Next map is ${nextMap.key}" }

        EventDispatcher.dispatch(MapTransitionStartEvent(nextMap.value, type, connection, targetConnection))
    }

    private fun connectionType(connection: MapObject): ConnectionType {
        val objPos = connection.scaledPosition
        val mapWidth = tiledMap.totalWidth() * UNIT_SCALE

        return when {
            objPos.x < 0.5f -> ConnectionType.LEFT
            objPos.x >= mapWidth - 1.5f -> ConnectionType.RIGHT
            objPos.y < 0.5f -> ConnectionType.DOWN
            else -> ConnectionType.UP
        }
    }

    companion object {
        private val LOG = logger<DungeonMap>()
    }
}
