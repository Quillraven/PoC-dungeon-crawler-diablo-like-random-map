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

data class DungeonMap(val assetType: TiledMapAssets, val tiledMap: TiledMap) {

    private val toMapConnections = mutableMapOf<MapObject, DungeonMap>()
    val connections: MapObjects = tiledMap.layer("connections").objects

    fun connect(connectionMapObj: MapObject, allMaps: List<DungeonMap>): MapObject {
        val type = connectionType(connectionMapObj)
        val toMap = toMapConnections[connectionMapObj]
        if (toMap != null) {
            // connection already linked to a map
            LOG.debug { "Reusing previous connection" }
            val toMapConnectionMapObj = toMap.connections.firstOrNull { connectionType(it).oppositeOf(type) }
                ?: gdxError("Map $toMap is not linked to $this map.")
            EventDispatcher.dispatch(MapTransitionStartEvent(toMap, type, connectionMapObj, toMapConnectionMapObj))
            return toMapConnectionMapObj
        }

        // make connection to a random new map
        lateinit var nextMapConnectionMapObj: MapObject
        val potentialMaps = when (assetType) {
            TiledMapAssets.TEST, TiledMapAssets.TEST2 -> {
                this.connections.firstOrNull { connectionType(it).oppositeOf(type) }?.let { mapObj ->
                    nextMapConnectionMapObj = mapObj
                }
                listOf(this)
            }

            else -> {
                allMaps.filter { map ->
                    if (map.assetType == TiledMapAssets.TEST || map.assetType == TiledMapAssets.TEST2) return@filter false

                    val matchingConnection = map.connections.firstOrNull { connectionType(it).oppositeOf(type) }
                    if (matchingConnection != null) {
                        nextMapConnectionMapObj = matchingConnection
                    }
                    map != this && matchingConnection != null
                }
            }
        }

        // connect this map to a random other map and
        // connect the random other map to this map
        val nextMap = potentialMaps.randomOrNull() ?: gdxError("No connecting map for type $type")
        toMapConnections[connectionMapObj] = nextMap
        nextMap.toMapConnections[nextMapConnectionMapObj] = this
        LOG.debug { "Connecting with type $type to ${potentialMaps.size} potential map(s) -> Found: ${nextMap.assetType}" }

        EventDispatcher.dispatch(MapTransitionStartEvent(nextMap, type, connectionMapObj, nextMapConnectionMapObj))
        return nextMapConnectionMapObj
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
