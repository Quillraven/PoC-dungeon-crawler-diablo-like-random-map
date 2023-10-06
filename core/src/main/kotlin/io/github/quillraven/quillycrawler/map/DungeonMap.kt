package io.github.quillraven.quillycrawler.map

import com.badlogic.gdx.maps.MapObject
import com.badlogic.gdx.maps.MapObjects
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTile
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.World
import io.github.quillraven.quillycrawler.QuillyCrawler.Companion.UNIT_SCALE
import io.github.quillraven.quillycrawler.assets.TiledMapAssets
import io.github.quillraven.quillycrawler.ecs.CharacterType
import io.github.quillraven.quillycrawler.ecs.PropType
import io.github.quillraven.quillycrawler.ecs.character
import io.github.quillraven.quillycrawler.ecs.component.Fade
import io.github.quillraven.quillycrawler.ecs.component.Graphic
import io.github.quillraven.quillycrawler.ecs.component.Tiled
import io.github.quillraven.quillycrawler.ecs.prop
import io.github.quillraven.quillycrawler.event.Event
import io.github.quillraven.quillycrawler.event.EventDispatcher
import io.github.quillraven.quillycrawler.event.EventListener
import io.github.quillraven.quillycrawler.event.MapTransitionStartEvent
import ktx.app.gdxError
import ktx.log.logger
import ktx.tiled.id
import ktx.tiled.layer
import ktx.tiled.propertyOrNull
import ktx.tiled.totalWidth
import kotlin.collections.set

enum class ConnectionType {
    LEFT, RIGHT, UP, DOWN;

    fun oppositeOf(type: ConnectionType): Boolean = when (type) {
        LEFT -> this == RIGHT
        RIGHT -> this == LEFT
        UP -> this == DOWN
        DOWN -> this == UP
    }
}

data class DungeonMap(val assetType: TiledMapAssets, val tiledMap: TiledMap) : EventListener {

    private val toMapConnections = mutableMapOf<MapObject, DungeonMap>()
    val connections: MapObjects = tiledMap.connections
    private val characters = tiledMap.characters.filter { it.tileType != "Player" }
    private val props = tiledMap.props
    val startPosition: Vector2? = tiledMap.playerStart?.scaledPosition

    fun spawnCharacters(world: World, fadeIn: Boolean = false) {
        characters.forEach { mapObject ->
            val tileType = mapObject.tileType

            world.character(CharacterType.valueOf(tileType.uppercase()), mapObject.scaledPosition) {
                it += Tiled(mapObject)
                if (fadeIn) {
                    it[Graphic].sprite.setAlpha(0f)
                    it += Fade(Interpolation.fade, 0.75f)
                }
            }
        }
    }

    fun spawnProps(world: World, fadeIn: Boolean = false) {
        props.forEach { mapObject ->
            val tileType = mapObject.tileType

            world.prop(PropType.valueOf(tileType.uppercase()), mapObject.scaledPosition) {
                it += Tiled(mapObject)
                if (fadeIn) {
                    it[Graphic].sprite.setAlpha(0f)
                    it += Fade(Interpolation.fade, 0.75f)
                }
            }
        }
    }

    private fun matchingConnection(type: ConnectionType, targetMap: TiledMap): MapObject? {
        return targetMap.connections.firstOrNull { connectionType(it).oppositeOf(type) }
    }

    fun connect(connectionMapObj: MapObject, allTiledMaps: Map<TiledMapAssets, TiledMap>): MapObject {
        val type = connectionType(connectionMapObj)
        val toMap = toMapConnections[connectionMapObj]
        if (toMap != null) {
            // connection already linked to a map
            LOG.debug { "Reusing previous connection" }
            // get the target connection to position the player correctly in the new map
            val toMapConnectionMapObj = toMap.connections.firstOrNull { connectionType(it).oppositeOf(type) }
                ?: gdxError("Map $toMap is not linked to $this map.")
            EventDispatcher.dispatch(MapTransitionStartEvent(toMap, type, connectionMapObj, toMapConnectionMapObj))
            return toMapConnectionMapObj
        }

        // make connection to a random new map
        val potentialMaps: Map<TiledMapAssets, TiledMap> = when (assetType) {
            // test maps are linked to themselves
            TiledMapAssets.TEST, TiledMapAssets.TEST2 -> mapOf(assetType to this.tiledMap)

            // other maps are linked to a random non-test / non-starting map
            else -> {
                allTiledMaps
                    .filterKeys { !it.isTest && !it.isStart }
                    .filterValues { matchingConnection(type, it) != null }
            }
        }

        // connect this map to a random other map and
        // connect the random other map to this map
        val nextMapEntry = potentialMaps.entries.randomOrNull() ?: gdxError("No connecting map for type $type")
        // null is not possible here because of the filter of potentialMaps from above -> use !! operator
        val nextConnection = matchingConnection(type, nextMapEntry.value)!!
        val nextMap = DungeonMap(nextMapEntry.key, nextMapEntry.value)
        toMapConnections[connectionMapObj] = nextMap
        nextMap.toMapConnections[nextConnection] = this
        LOG.debug { "Connecting with type $type to ${potentialMaps.size} potential map(s) -> Found: ${nextMap.assetType}" }

        EventDispatcher.dispatch(MapTransitionStartEvent(nextMap, type, connectionMapObj, nextConnection))
        return nextConnection
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

    override fun onEvent(event: Event) {
        // TODO react on entity removal like character/props (=coins)
    }

    companion object {
        private val LOG = logger<DungeonMap>()
    }
}

private val TiledMapTile.type: String?
    get() = propertyOrNull("type")

private val MapObject.tileType: String
    get() = (this as TiledMapTileMapObject).tile?.type
        ?: gdxError("MapObject ${this.id} is not linked to a tile with a type")

private val TiledMap.characters: MapObjects
    get() = this.layers["characters"].objects

private val TiledMap.playerStart: MapObject?
    get() = this.layers["characters"].objects.firstOrNull { it.tileType == "Player" }

private val TiledMap.props: MapObjects
    get() = this.layers["props"].objects

private val TiledMap.connections: MapObjects
    get() = this.layer("connections").objects
