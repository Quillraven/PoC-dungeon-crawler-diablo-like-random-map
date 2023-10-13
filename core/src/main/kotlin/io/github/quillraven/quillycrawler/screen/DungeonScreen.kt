package io.github.quillraven.quillycrawler.screen

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.configureWorld
import io.github.quillraven.quillycrawler.QuillyCrawler.Companion.gameViewport
import io.github.quillraven.quillycrawler.assets.Assets
import io.github.quillraven.quillycrawler.assets.TiledMapAssets
import io.github.quillraven.quillycrawler.ecs.activateKeyboardController
import io.github.quillraven.quillycrawler.ecs.deRegisterEventListener
import io.github.quillraven.quillycrawler.ecs.registerEventListener
import io.github.quillraven.quillycrawler.ecs.system.*
import io.github.quillraven.quillycrawler.map.DungeonMapService
import ktx.app.KtxScreen

class DungeonScreen(
    private val assets: Assets,
    private val batch: Batch,
    private val initialMap: TiledMapAssets = TiledMapAssets.randomStartMap()
) : KtxScreen {

    private val viewport: Viewport = gameViewport()
    private val world = configureWorld {
        injectables {
            add(viewport)
            add(viewport.camera as OrthographicCamera)
            add(batch)
            add(assets)
        }

        systems {
            add(MoveSystem())
            add(AnimationSystem())
            add(FadeSystem())
            add(ScaleSystem())
            add(TranslateSystem())
            add(DissolveSystem())
            add(CameraSystem())
            add(RenderSystem())
            add(RemoveSystem())
        }
    }
    private val dungeonMapService = DungeonMapService(world, assets)

    override fun show() {
        world.activateKeyboardController()
        world.registerEventListener()
        dungeonMapService.loadDungeon(initialMap)
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
