package io.github.quillraven.quillycrawler

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import io.github.quillraven.quillycrawler.screen.DungeonScreen
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.assets.disposeSafely
import ktx.assets.load

class QuillyCrawler : KtxGame<KtxScreen>() {

    private val batch: Batch by lazy { SpriteBatch() }
    private val assets = AssetManager()

    override fun create() {
        assets.load<TextureAtlas>("graphics/characters.atlas")
        assets.finishLoading()

        addScreen(DungeonScreen(assets, batch))

        setScreen<DungeonScreen>()
    }

    override fun dispose() {
        super.dispose()

        batch.disposeSafely()
        assets.disposeSafely()
    }

    companion object {
        const val UNIT_SCALE = 1 / 16f
    }
}
