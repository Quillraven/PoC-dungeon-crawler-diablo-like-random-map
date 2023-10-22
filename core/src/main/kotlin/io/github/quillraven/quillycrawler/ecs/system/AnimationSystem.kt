package io.github.quillraven.quillycrawler.ecs.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import io.github.quillraven.quillycrawler.assets.Assets
import io.github.quillraven.quillycrawler.assets.TextureAtlasAssets
import io.github.quillraven.quillycrawler.ecs.component.Animation
import io.github.quillraven.quillycrawler.ecs.component.AnimationType
import io.github.quillraven.quillycrawler.ecs.component.Graphic
import io.github.quillraven.quillycrawler.ecs.component.TextureAnimation
import ktx.app.gdxError

class AnimationSystem(private val assets: Assets = inject()) :
    IteratingSystem(family { all(Animation, Graphic) }) {

    private val animationCache = mutableMapOf<String, TextureAnimation>()

    override fun onTickEntity(entity: Entity) = with(entity[Animation]) {
        entity[Graphic].sprite.setRegion(textureAnimation.getKeyFrame(stateTime, loop))
        stateTime += deltaTime * speed
    }

    fun textureAnimation(atlasKey: String, type: AnimationType, atlasAsset: TextureAtlasAssets): TextureAnimation {
        val atlasAnimationKey = "$atlasKey/${type.atlasKey}"
        return animationCache.getOrPut(atlasAnimationKey) {
            val keyFrames = assets[atlasAsset].findRegions(atlasAnimationKey)
            if (keyFrames.isEmpty) {
                gdxError("There are no regions for animation $atlasKey in atlas $atlasAsset")
            }
            TextureAnimation(DEFAULT_ANIMATION_SPEED, keyFrames)
        }
    }

    companion object {
        private const val DEFAULT_ANIMATION_SPEED = 1 / 4f
    }
}
