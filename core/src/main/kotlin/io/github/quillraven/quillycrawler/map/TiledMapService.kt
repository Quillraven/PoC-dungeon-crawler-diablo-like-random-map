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
import io.github.quillraven.quillycrawler.ecs.prop
import io.github.quillraven.quillycrawler.event.EventDispatcher
import io.github.quillraven.quillycrawler.event.MapLoadEvent
import ktx.app.gdxError
import ktx.math.vec2
import ktx.tiled.*

class TiledMapService(private val world: World, private val assets: Assets) {

    private val playerEntities = world.family { all(Tags.PLAYER) }
    private lateinit var currentMap: TiledMap

    fun loadMap(type: TiledMapAssets) {
        val tiledMap = assets[type]
        currentMap = tiledMap

        spawnPlayer()
        spawnProps()
        spawnCharacters()

        EventDispatcher.dispatch(MapLoadEvent(tiledMap))
    }

    private fun spawnCharacters() {
        currentMap.forEachMapObject("characters") { mapObject ->
            val tileType = mapObject.tileType
            if (tileType == "Player") return@forEachMapObject

            world.character(CharacterType.valueOf(tileType.uppercase()), mapObject.scaledPosition)
        }
    }

    private fun spawnProps() {
        currentMap.forEachMapObject("props") { mapObject ->
            val tileType = mapObject.tileType

            world.prop(PropType.valueOf(tileType.uppercase()), mapObject.scaledPosition)
        }
    }

    private fun spawnPlayer() {
        currentMap.forEachMapObject("characters") { mapObject ->
            if (mapObject.tileType != "Player") return@forEachMapObject

            if (playerEntities.isEmpty) {
                // spawn new player
                with(world) {
                    character(CharacterType.PRIEST, mapObject.scaledPosition).also { player ->
                        player.configure { it += Tags.PLAYER }
                    }
                }
            } else {
                // relocate player
                playerEntities.forEach { player ->
                    player[Boundary].position.set(mapObject.scaledPosition)
                }
            }
        }
    }
}

private val TiledMapTile.type: String?
    get() = propertyOrNull("type")

private val MapObject.scaledPosition: Vector2
    get() = vec2(this.x * UNIT_SCALE, this.y * UNIT_SCALE)

private val MapObject.tileType: String
    get() = (this as TiledMapTileMapObject).tile?.type
        ?: gdxError("MapObject ${this.id} is not linked to a tile with a type")
