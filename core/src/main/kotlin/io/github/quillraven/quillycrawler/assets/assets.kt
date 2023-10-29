package io.github.quillraven.quillycrawler.assets

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.ShaderProgramLoader.ShaderProgramParameter
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import ktx.app.gdxError
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
    TEST2("maps/test2.tmx"),
    TEST_MOVE("maps/move_test.tmx");

    val isTest = this.name.startsWith("TEST")

    val isStart = this.name.startsWith("START")

    companion object {
        fun randomStartMap() = entries.filter { it.isStart }.random()
    }
}

enum class ShaderAssets(val vertexPath: String, val fragmentPath: String) {
    DISSOLVE("shaders/default.vert", "shaders/dissolve.frag");

    companion object {
        var DISSOLVE_VALUE = -1
        var DISSOLVE_UV_OFFSET = -1
        var DISSOLVE_ATLAS_MAX_UV = -1
        var DISSOLVE_FRAG_NUMBER = -1
    }
}

class Assets {

    @PublishedApi
    internal val assetManager = AssetManager()

    operator fun get(asset: TiledMapAssets): TiledMap = assetManager.getAsset<TiledMap>(asset.path)

    operator fun get(asset: TextureAtlasAssets): TextureAtlas = assetManager.getAsset<TextureAtlas>(asset.path)

    operator fun get(asset: ShaderAssets): ShaderProgram = assetManager.getAsset<ShaderProgram>(asset.name)

    inline fun <reified T> load(path: String): T {
        assetManager.load(path, T::class.java)
        return assetManager.finishLoadingAsset(path)
    }

    fun loadShader(type: ShaderAssets): ShaderProgram {
        assetManager.load<ShaderProgram>(type.name, ShaderProgramParameter().apply {
            vertexFile = type.vertexPath
            fragmentFile = type.fragmentPath
        })
        return assetManager.finishLoadingAsset(type.name)
    }

    fun loadAll() {
        assetManager.setLoader(TmxMapLoader(assetManager.fileHandleResolver))

        TextureAtlasAssets.entries.forEach { assetManager.load<TextureAtlas>(it.path) }
        TiledMapAssets.entries.forEach { assetManager.load<TiledMap>(it.path) }
        ShaderAssets.entries.forEach { shaderAsset ->
            assetManager.load<ShaderProgram>(shaderAsset.name, ShaderProgramParameter().apply {
                vertexFile = shaderAsset.vertexPath
                fragmentFile = shaderAsset.fragmentPath
            })
        }

        assetManager.finishLoading()

        // verify shaders and init ShaderAssets uniform locations
        ShaderAssets.entries.forEach { shaderAsset ->
            val shader = this[shaderAsset]
            if (!shader.isCompiled) {
                gdxError("Shader " + shaderAsset + " could not be loaded: ${shader.log}")
            }

            when (shaderAsset) {
                ShaderAssets.DISSOLVE -> {
                    ShaderAssets.DISSOLVE_VALUE = shader.getUniformLocation("u_dissolve")
                    ShaderAssets.DISSOLVE_UV_OFFSET = shader.getUniformLocation("u_uvOffset")
                    ShaderAssets.DISSOLVE_ATLAS_MAX_UV = shader.getUniformLocation("u_atlasMaxUV")
                    ShaderAssets.DISSOLVE_FRAG_NUMBER = shader.getUniformLocation("u_fragmentNumber")
                }
            }
        }
    }

    fun dispose() {
        assetManager.disposeSafely()
    }
}
