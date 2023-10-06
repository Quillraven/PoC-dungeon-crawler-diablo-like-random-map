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
    START_1("maps/start_1.tmx"),
    EXIT_0("maps/exit_0.tmx"),
    EXIT_1("maps/exit_1.tmx"),
    TEST("maps/test.tmx"),
    TEST2("maps/test2.tmx");

    val isTest = this.name.startsWith("TEST")

    val isStart = this.name.startsWith("START")

    companion object {
        fun randomStartMap() = entries.filter { it.isStart }.random()
    }
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
