package io.github.quillraven.quillycrawler.map

import com.badlogic.gdx.maps.MapObject
import com.badlogic.gdx.maps.MapObjects
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTile
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject
import com.badlogic.gdx.math.Vector2
import io.github.quillraven.quillycrawler.QuillyCrawler.Companion.UNIT_SCALE
import ktx.app.gdxError
import ktx.math.vec2
import ktx.tiled.*

val TiledMapTile.type: String?
    get() = propertyOrNull("type")

val MapObject.tileType: String
    get() = (this as TiledMapTileMapObject).tile?.type
        ?: gdxError("MapObject ${this.id} is not linked to a tile with a type")

val MapObject.scaledPosition: Vector2
    get() = vec2(this.x * UNIT_SCALE, this.y * UNIT_SCALE)

val TiledMap.characters: MapObjects
    get() = this.layers["characters"].objects

val TiledMap.playerStart: MapObject?
    get() = this.layers["characters"].objects.firstOrNull { it.tileType == "Player" }

val TiledMap.props: MapObjects
    get() = this.layers["props"].objects

val TiledMap.connections: MapObjects
    get() = this.layer("connections").objects
