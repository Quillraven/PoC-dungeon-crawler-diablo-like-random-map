package io.github.quillraven.quillycrawler.screen

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.fleks.configureWorld
import io.github.quillraven.quillycrawler.ecs.CharacterType
import io.github.quillraven.quillycrawler.ecs.activateKeyboardController
import io.github.quillraven.quillycrawler.ecs.changeAnimation
import io.github.quillraven.quillycrawler.ecs.character
import io.github.quillraven.quillycrawler.ecs.component.AnimationType
import io.github.quillraven.quillycrawler.ecs.component.Boundary
import io.github.quillraven.quillycrawler.ecs.component.Tags
import io.github.quillraven.quillycrawler.ecs.system.AnimationSystem
import io.github.quillraven.quillycrawler.ecs.system.MoveSystem
import io.github.quillraven.quillycrawler.ecs.system.RenderSystem
import ktx.app.KtxScreen
import ktx.math.vec2

class DungeonScreen(private val assets: AssetManager, private val batch: Batch) : KtxScreen {

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

    override fun show() {
        world.activateKeyboardController()

        // TODO remove debug stuff
        with(world) {
            character(CharacterType.PRIEST, vec2(0f, 0f)).also { entity ->
                entity.configure { it += Tags.PLAYER }
            }
            character(CharacterType.PRIEST, vec2(0.5f, 0f))
            character(CharacterType.SKULL, vec2(0f, 0.5f))

            character(CharacterType.SKULL, vec2(2f, 2f)).also { entity ->
                changeAnimation(entity, CharacterType.PRIEST, AnimationType.IDLE)
                entity[Boundary].size.set(2f, 2f)
                entity[Boundary].rotation = 45f
            }
        }
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
