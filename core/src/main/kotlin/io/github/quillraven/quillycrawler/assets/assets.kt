package io.github.quillraven.quillycrawler.assets

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import ktx.assets.disposeSafely
import ktx.assets.getAsset
import ktx.assets.load
import ktx.assets.setLoader

enum class TextureAtlasAssets(val path: String) {
    CHARACTERS("graphics/characters.atlas"),
    PROPS("graphics/props.atlas"),
}

enum class TiledMapAssets(val path: String) {
    START_0("maps/start_0.tmx"),
}

class Assets {
    private val assetManager = AssetManager()

    operator fun get(asset: TiledMapAssets): TiledMap = assetManager.getAsset<TiledMap>(asset.path)

    operator fun get(asset: TextureAtlasAssets): TextureAtlas = assetManager.getAsset<TextureAtlas>(asset.path)

    fun loadAll() {
        assetManager.setLoader(TmxMapLoader(assetManager.fileHandleResolver))

        TextureAtlasAssets.entries.forEach { assetManager.load<TextureAtlas>(it.path) }
        TiledMapAssets.entries.forEach { assetManager.load<TiledMap>(it.path) }

        assetManager.finishLoading()
    }

    fun dispose() {
        assetManager.disposeSafely()
    }
}
