package io.github.quillraven.quillycrawler.map

import com.badlogic.gdx.maps.MapObject
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTile
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.World
import io.github.quillraven.quillycrawler.QuillyCrawler.Companion.UNIT_SCALE
import io.github.quillraven.quillycrawler.assets.Assets
import io.github.quillraven.quillycrawler.assets.TiledMapAssets
import io.github.quillraven.quillycrawler.ecs.CharacterType
import io.github.quillraven.quillycrawler.ecs.PropType
import io.github.quillraven.quillycrawler.ecs.character
import io.github.quillraven.quillycrawler.ecs.component.Boundary
import io.github.quillraven.quillycrawler.ecs.component.Tags
import io.github.quillraven.quillycrawler.ecs.component.Tiled
import io.github.quillraven.quillycrawler.ecs.prop
import io.github.quillraven.quillycrawler.event.*
import ktx.app.gdxError
import ktx.math.minusAssign
import ktx.math.vec2
import ktx.tiled.*

class TiledMapService(private val world: World, private val assets: Assets) : EventListener {

    private val playerEntities = world.family { all(Tags.PLAYER) }
    private val tiledEntities = world.family { all(Tiled) }
    private val allMaps: Map<TiledMapAssets, DungeonMap>
    private lateinit var activeMap: DungeonMap
    private lateinit var nextMap: DungeonMap

    init {
        EventDispatcher.register(this)
        allMaps = TiledMapAssets.entries.associateWith { DungeonMap(assets[it]) }
    }

    fun loadMap(type: TiledMapAssets) {
        activeMap = allMaps[type] ?: gdxError("Map of type $type not loaded")
        val tiledMap = activeMap.tiledMap

        if (type.isStartMap()) {
            spawnPlayer(tiledMap)
        }
        spawnProps(tiledMap)
        spawnCharacters(tiledMap)

        EventDispatcher.dispatch(MapLoadEvent(activeMap))
    }

    private fun spawnCharacters(tiledMap: TiledMap) {
        tiledMap.forEachMapObject("characters") { mapObject ->
            val tileType = mapObject.tileType
            if (tileType == "Player") return@forEachMapObject

            world.character(CharacterType.valueOf(tileType.uppercase()), mapObject.scaledPosition)
        }
    }

    private fun spawnProps(tiledMap: TiledMap) {
        tiledMap.forEachMapObject("props") { mapObject ->
            val tileType = mapObject.tileType

            world.prop(PropType.valueOf(tileType.uppercase()), mapObject.scaledPosition) {
                it += Tiled(mapObject)
            }
        }
    }

    private fun spawnPlayer(tiledMap: TiledMap) {
        tiledMap.forEachMapObject("characters") { mapObject ->
            if (mapObject.tileType != "Player") return@forEachMapObject

            if (playerEntities.isEmpty) {
                // spawn new player
                world.character(CharacterType.PRIEST, mapObject.scaledPosition) { it += Tags.PLAYER }
            } else {
                // relocate player
                playerEntities.forEach { it[Boundary].position.set(mapObject.scaledPosition) }
            }
        }
    }

    override fun onEvent(event: Event) {
        when (event) {
            is MapConnectionEvent -> {
                activeMap.connect(event.connection, allMaps)
            }

            is MapTransitionStartEvent -> nextMap = event.toMap

            is MapTransitionStopEvent -> {
                // relocate player relative to (0,0). Currently, he is outside the boundaries of the activeMap
                playerEntities.forEach { player ->
                    val (position) = player[Boundary]
                    if (position.x < 0f || position.x >= activeMap.tiledMap.totalWidth() * UNIT_SCALE) {
                        position.x = 0f
                    }
                    if (position.y < 0f || position.y >= activeMap.tiledMap.totalHeight() * UNIT_SCALE) {
                        position.y = 0f
                    }
                    position -= event.offset
                }

                // TODO remember which entities are still alive -> maybe store them directly in DungeonMap instance ???
                tiledEntities.forEach { it.remove() }
                activeMap = nextMap
                // TODO either load entire map or state of DungeonMap
                spawnProps(activeMap.tiledMap)
                spawnCharacters(activeMap.tiledMap)

                // TODO fade in new props/chars instead of instantly showing them
                //   block player movement until transition is completely done
            }

            else -> Unit
        }
    }
}

private val TiledMapTile.type: String?
    get() = propertyOrNull("type")

val MapObject.scaledPosition: Vector2
    get() = vec2(this.x * UNIT_SCALE, this.y * UNIT_SCALE)

private val MapObject.tileType: String
    get() = (this as TiledMapTileMapObject).tile?.type
        ?: gdxError("MapObject ${this.id} is not linked to a tile with a type")
