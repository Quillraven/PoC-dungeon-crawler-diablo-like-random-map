package io.github.quillraven.quillycrawler.map

import com.badlogic.gdx.maps.MapObject
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.math.Interpolation
import com.github.quillraven.fleks.World
import io.github.quillraven.quillycrawler.assets.Assets
import io.github.quillraven.quillycrawler.assets.TiledMapAssets
import io.github.quillraven.quillycrawler.ecs.CharacterType
import io.github.quillraven.quillycrawler.ecs.character
import io.github.quillraven.quillycrawler.ecs.component.*
import io.github.quillraven.quillycrawler.ecs.system.RenderSystem.Companion.TRANSITION_SPEED
import io.github.quillraven.quillycrawler.event.*
import ktx.app.gdxError

class DungeonMapService(private val world: World, private val assets: Assets) : EventListener {

    private val playerEntities = world.family { all(Tags.PLAYER) }
    private val tiledEntities = world.family { all(Tiled) }
    private val allTiledMaps: Map<TiledMapAssets, TiledMap>
    private lateinit var activeMap: DungeonMap
    private lateinit var nextMap: DungeonMap
    private lateinit var lastConnection: MapObject

    init {
        EventDispatcher.register(this)
        allTiledMaps = TiledMapAssets.entries.associateWith { assets[it] }
    }

    fun loadDungeon(startMapType: TiledMapAssets) {
        val tiledMap = allTiledMaps[startMapType] ?: gdxError("Map of type $startMapType not loaded")

        activeMap = DungeonMap(startMapType, tiledMap).also { EventDispatcher.register(it) }
        spawnPlayer()
        activeMap.spawnCharacters(world)
        activeMap.spawnProps(world)

        EventDispatcher.dispatch(MapLoadEvent(activeMap))
    }

    private fun spawnPlayer() {
        val startLoc = activeMap.startPosition ?: gdxError("No start pos. defined for ${activeMap.assetType}")

        if (playerEntities.isEmpty) {
            // spawn new player
            world.character(CharacterType.PRIEST, startLoc) {
                it += Tags.PLAYER
                it += Tags.CAMERA_LOCK
                it += Inventory(coins = 0)
            }
        } else {
            // relocate player
            playerEntities.forEach { it[Boundary].position.set(startLoc) }
        }
    }

    override fun onEvent(event: Event) {
        when (event) {
            is MapConnectionEvent -> {
                lastConnection = activeMap.connect(event.connection, allTiledMaps)
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
                }

                // remove any tiled entity of the current map ...
                tiledEntities.forEach { it.remove() }

                // ... change event listening to new map ...
                EventDispatcher.deRegister(activeMap)
                activeMap = nextMap
                EventDispatcher.register(activeMap)

                // ... and spawn entities of new map
                activeMap.spawnProps(world, fadeIn = true)
                activeMap.spawnCharacters(world, fadeIn = true)
                EventDispatcher.dispatch(MapLoadEvent(activeMap))
            }

            else -> Unit
        }
    }
}
