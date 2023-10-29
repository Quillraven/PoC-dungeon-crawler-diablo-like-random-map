package io.github.quillraven.quillycrawler.map

import com.badlogic.gdx.maps.MapObject
import com.badlogic.gdx.maps.MapObjects
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import io.github.quillraven.quillycrawler.QuillyCrawler.Companion.UNIT_SCALE
import io.github.quillraven.quillycrawler.assets.TiledMapAssets
import io.github.quillraven.quillycrawler.ecs.*
import io.github.quillraven.quillycrawler.ecs.component.Fade
import io.github.quillraven.quillycrawler.ecs.component.Graphic
import io.github.quillraven.quillycrawler.ecs.component.Tiled
import io.github.quillraven.quillycrawler.event.*
import ktx.app.gdxError
import ktx.log.logger
import ktx.math.vec2
import ktx.tiled.id
import ktx.tiled.shape
import ktx.tiled.totalWidth
import kotlin.collections.set
import kotlin.math.roundToInt

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
    private val connections: MapObjects = tiledMap.connections
    private val charMapObjects = tiledMap.characters.filter { it.tileType != "Player" }.associateBy { it.id }.toMutableMap()
    private val propMapObjects = tiledMap.props.associateBy { it.id }.toMutableMap()
    private val charEntities = mutableMapOf<Vector2, Entity>()
    private val propEntities = mutableMapOf<Vector2, Entity>()
    val startPosition: Vector2? = tiledMap.playerStart?.scaledPosition

    fun character(at: Vector2): Entity? = charEntities[at]

    fun prop(at: Vector2): Entity? = propEntities[at]

    fun connection(at: Vector2): MapObject? = connections.firstOrNull { at in it.shape }


    // this is used as an index in a map structure of a DungeonMap instance to efficiently retrieve a character/prop
    // by its position. To avoid floating accuracy issues, we make sure to convert it to a proper Int before, since
    // movement is tile based (=Int based calculations).
    private fun Vector2.roundToInt(): Vector2 = vec2(x.roundToInt().toFloat(), y.roundToInt().toFloat())

    fun spawnCharacters(world: World, fadeIn: Boolean = false) {
        // TODO correctly restore last position of character
        charEntities.clear()
        charMapObjects.values.forEach { mapObject ->
            val charType = CharacterType.valueOf(mapObject.tileType.uppercase())
            val mapObjPosition = mapObject.scaledPosition
            val entity = world.character(charType, mapObjPosition) {
                it += Tiled(mapObject, charType)
                if (fadeIn) {
                    it[Graphic].sprite.setAlpha(0f)
                    it += Fade(Interpolation.fade, 0.75f)
                }

                configureEntity(it, charType)
            }
            charEntities[mapObjPosition.roundToInt()] = entity
        }
    }

    fun spawnProps(world: World, fadeIn: Boolean = false) {
        propEntities.clear()
        propMapObjects.values.forEach { mapObject ->
            val propType = PropType.valueOf(mapObject.tileType.uppercase())
            val mapObjPosition = mapObject.scaledPosition
            val entity = world.prop(propType, mapObjPosition) {
                it += Tiled(mapObject, propType)
                if (fadeIn) {
                    it[Graphic].sprite.setAlpha(0f)
                    it += Fade(Interpolation.fade, 0.75f)
                }
            }
            propEntities[mapObjPosition.roundToInt()] = entity
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
            TiledMapAssets.TEST, TiledMapAssets.TEST2, TiledMapAssets.TEST_MOVE -> mapOf(assetType to this.tiledMap)

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
        when (event) {
            is PlayerCollisionPropEvent -> {
                if (event.propType.destructible) {
                    propEntities.remove(event.position)
                    propMapObjects.remove(event.propId)
                }
            }

            is PlayerCollisionCharacterEvent -> {
                charEntities.remove(event.position)
                charMapObjects.remove(event.charId)
            }

            else -> Unit
        }
    }

    fun moveCharacter(from: Vector2, to: Vector2) {
        if (charEntities[to] != null) {
            gdxError("There is already a character at $to: ${charEntities[to]}")
        }

        val charEntity = charEntities.remove(from) ?: gdxError("There is no entity at $from")
        LOG.debug { "Move character $charEntity from $from to $to" }
        // it is important to create a new Vector2 instance for the key because if we just use the 'to' instance
        // then we get a wrong behavior since that instance gets modified further in the MoveSystem which results
        // in an updating key. However, we want a fixed key that refers to a tile based index -> new Vector2 instance.
        charEntities[to.roundToInt()] = charEntity
    }

    companion object {
        private val LOG = logger<DungeonMap>()
    }
}
