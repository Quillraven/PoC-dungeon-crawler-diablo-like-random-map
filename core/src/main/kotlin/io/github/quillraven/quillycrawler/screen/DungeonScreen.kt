package io.github.quillraven.quillycrawler.screen

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.configureWorld
import io.github.quillraven.quillycrawler.assets.Assets
import io.github.quillraven.quillycrawler.assets.TiledMapAssets
import io.github.quillraven.quillycrawler.ecs.activateKeyboardController
import io.github.quillraven.quillycrawler.ecs.deRegisterEventListener
import io.github.quillraven.quillycrawler.ecs.registerEventListener
import io.github.quillraven.quillycrawler.ecs.system.AnimationSystem
import io.github.quillraven.quillycrawler.ecs.system.MoveSystem
import io.github.quillraven.quillycrawler.ecs.system.RenderSystem
import io.github.quillraven.quillycrawler.map.TiledMapService
import ktx.app.KtxScreen

class DungeonScreen(private val assets: Assets, private val batch: Batch) : KtxScreen {

    private val viewport: Viewport = ExtendViewport(16f, 9f)
    private val world = configureWorld {
        injectables {
            add(viewport)
            add(batch)
            add(assets)
        }

        systems {
            add(MoveSystem())
            add(AnimationSystem())
            add(RenderSystem())
        }
    }
    private val tiledMapService = TiledMapService(world, assets)

    override fun show() {
        world.activateKeyboardController()
        world.registerEventListener()
        tiledMapService.loadMap(TiledMapAssets.START_1)
    }

    override fun hide() {
        world.deRegisterEventListener()
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
    }

    override fun render(delta: Float) {
        world.update(delta)
    }

    override fun dispose() {
        world.dispose()
    }

}
