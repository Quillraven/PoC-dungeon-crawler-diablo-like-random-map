package io.github.quillraven.quillycrawler

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.badlogic.gdx.utils.viewport.Viewport
import io.github.quillraven.quillycrawler.assets.Assets
import io.github.quillraven.quillycrawler.screen.DungeonScreen
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.assets.disposeSafely

// TODO add more enemies + move pattern (circle, follow walls, mimic player)

class QuillyCrawler : KtxGame<KtxScreen>() {

    private val batch: Batch by lazy { SpriteBatch() }
    private val assets = Assets()

    override fun create() {
        Gdx.app.logLevel = Application.LOG_DEBUG
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

        fun gameViewport(): Viewport = ExtendViewport(8f, 6f)
    }
}
