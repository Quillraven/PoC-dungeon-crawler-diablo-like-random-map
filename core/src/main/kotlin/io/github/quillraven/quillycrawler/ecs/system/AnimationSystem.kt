package io.github.quillraven.quillycrawler.ecs.system

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import io.github.quillraven.quillycrawler.ecs.component.Animation
import io.github.quillraven.quillycrawler.ecs.component.AnimationType
import io.github.quillraven.quillycrawler.ecs.component.Graphic
import io.github.quillraven.quillycrawler.ecs.component.TextureAnimation
import ktx.app.gdxError
import ktx.assets.getAsset

class AnimationSystem(assets: AssetManager = inject()) : IteratingSystem(family { all(Animation, Graphic) }) {

    private val animationCache = mutableMapOf<String, TextureAnimation>()
    private val characterAtlas = assets.getAsset<TextureAtlas>("graphics/characters.atlas")

    override fun onTickEntity(entity: Entity) {
        val cmp = entity[Animation]
        val (textureAnimation, stateTime, loop) = cmp

        entity[Graphic].sprite.setRegion(textureAnimation.getKeyFrame(stateTime, loop))
        cmp.stateTime += deltaTime
    }

    fun textureAnimation(atlasKey: String, type: AnimationType): TextureAnimation {
        val atlasAnimationKey = "$atlasKey/${type.atlasKey}"
        return animationCache.getOrPut(atlasAnimationKey) {
            val keyFrames = characterAtlas.findRegions(atlasAnimationKey)
            if (keyFrames.isEmpty) {
                gdxError("There are no regions for animation $atlasKey")
            }
            TextureAnimation(DEFAULT_ANIMATION_SPEED, keyFrames)
        }
    }

    companion object {
        private const val DEFAULT_ANIMATION_SPEED = 1 / 4f
    }
}
