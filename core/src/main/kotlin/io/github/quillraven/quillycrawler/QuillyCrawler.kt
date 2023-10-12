package io.github.quillraven.quillycrawler

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
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
        // TODO add game viewport utility method that can also be used in tests like ShaderTest
        // TODO rename dissolve vertex shader to default
        // TODO make shaders an asset (=part of loadAll + dispose)
        // TODO also use shader int location for uniforms instead of strings
        // TODO add debug logging to delayed removal
        // TODO add DebugSystem for profiling (render calls, fps, num entities, ...)
        const val UNIT_SCALE = 1 / 16f
    }
}
