package io.github.quillraven.quillycrawler.map

import com.badlogic.gdx.maps.MapObject
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTile
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.World
import io.github.quillraven.quillycrawler.QuillyCrawler.Companion.UNIT_SCALE
import io.github.quillraven.quillycrawler.assets.Assets
import io.github.quillraven.quillycrawler.assets.TiledMapAssets
import io.github.quillraven.quillycrawler.ecs.CharacterType
import io.github.quillraven.quillycrawler.ecs.PropType
import io.github.quillraven.quillycrawler.ecs.character
import io.github.quillraven.quillycrawler.ecs.component.*
import io.github.quillraven.quillycrawler.ecs.prop
import io.github.quillraven.quillycrawler.ecs.system.RenderSystem.Companion.TRANSITION_SPEED
import io.github.quillraven.quillycrawler.event.*
import ktx.app.gdxError
import ktx.math.vec2
import ktx.tiled.*

class TiledMapService(private val world: World, private val assets: Assets) : EventListener {

    private val playerEntities = world.family { all(Tags.PLAYER) }
    private val tiledEntities = world.family { all(Tiled) }
    private val allMaps: List<DungeonMap>
    private lateinit var activeMap: DungeonMap
    private lateinit var nextMap: DungeonMap
    private lateinit var lastConnection: MapObject

    init {
        EventDispatcher.register(this)
        allMaps = TiledMapAssets.entries.map { DungeonMap(it, assets[it]) }
    }

    fun loadMap(type: TiledMapAssets) {
        activeMap = allMaps.firstOrNull { it.assetType == type } ?: gdxError("Map of type $type not loaded")
        val tiledMap = activeMap.tiledMap

        if (type.isStartMap()) {
            spawnPlayer(tiledMap)
        }
        spawnProps(tiledMap)
        spawnCharacters(tiledMap)

        EventDispatcher.dispatch(MapLoadEvent(activeMap))
    }

    private fun spawnCharacters(tiledMap: TiledMap, fadeIn: Boolean = false) {
        tiledMap.forEachMapObject("characters") { mapObject ->
            val tileType = mapObject.tileType
            if (tileType == "Player") return@forEachMapObject

            world.character(CharacterType.valueOf(tileType.uppercase()), mapObject.scaledPosition) {
                it += Tiled(mapObject)
                if (fadeIn) {
                    it[Graphic].sprite.setAlpha(0f)
                    it += Fade(Interpolation.fade, 0.75f)
                }
            }
        }
    }

    private fun spawnProps(tiledMap: TiledMap, fadeIn: Boolean = false) {
        tiledMap.forEachMapObject("props") { mapObject ->
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
                lastConnection = activeMap.connect(event.connection, allMaps)
                // fade out current characters/props together with the current map.
                // Map fade out is happening in RenderSystem.
                // The characters/props get removed in the MapTransitionStopEvent below.
                tiledEntities.forEach { tiledEntity ->
                    tiledEntity.configure { it += Fade(Interpolation.fade, TRANSITION_SPEED, 1f, 0f) }
                }

            }

            is MapTransitionStartEvent -> nextMap = event.toMap

            is MapTransitionStopEvent -> {
                // map transition is finished and current player position
                // is relative to the previous map -> relocate player
                // to the correct location of the new map (=connection location of new map).
                val playerTargetPos = lastConnection.scaledPosition
                playerEntities.forEach { player ->
                    player[Boundary].position.set(playerTargetPos)
                    player.configure { it -= Tags.ROOT }
                }

                // TODO remember which entities are still alive -> maybe store them directly in DungeonMap instance ???
                tiledEntities.forEach { it.remove() }
                activeMap = nextMap
                // TODO either load entire map or state of DungeonMap
                spawnProps(activeMap.tiledMap, fadeIn = true)
                spawnCharacters(activeMap.tiledMap, fadeIn = true)
                EventDispatcher.dispatch(MapLoadEvent(activeMap))
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
