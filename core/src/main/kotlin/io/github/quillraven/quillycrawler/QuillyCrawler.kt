package io.github.quillraven.quillycrawler

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import io.github.quillraven.quillycrawler.assets.Assets
import io.github.quillraven.quillycrawler.screen.DungeonScreen
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.assets.disposeSafely

class QuillyCrawler : KtxGame<KtxScreen>() {

    private val batch: Batch by lazy { SpriteBatch() }
    private val assets = Assets()

    override fun create() {
        assets.loadAll()

        addScreen(DungeonScreen(assets, batch))

        setScreen<DungeonScreen>()
    }

    override fun dispose() {
        super.dispose()

        batch.disposeSafely()
        assets.dispose()
    }

    companion object {
        const val UNIT_SCALE = 1 / 16f
    }
}
