package io.github.quillraven.quillycrawler

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import io.github.quillraven.quillycrawler.assets.Assets
import io.github.quillraven.quillycrawler.assets.TiledMapAssets
import io.github.quillraven.quillycrawler.screen.DungeonScreen
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.assets.disposeSafely

class MapTransitionTest : KtxGame<KtxScreen>() {

    private val testMap = TiledMapAssets.TEST
    private val batch: Batch by lazy { SpriteBatch() }
    private val assets = Assets()

    override fun create() {
        Gdx.app.logLevel = Application.LOG_DEBUG
        assets.loadAll()

        addScreen(DungeonScreen(assets, batch, testMap))

        setScreen<DungeonScreen>()
    }

    override fun dispose() {
        super.dispose()

        batch.disposeSafely()
        assets.dispose()
    }

}
