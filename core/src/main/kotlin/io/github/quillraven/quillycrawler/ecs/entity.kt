package io.github.quillraven.quillycrawler.ecs

import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import io.github.quillraven.quillycrawler.QuillyCrawler.Companion.UNIT_SCALE
import io.github.quillraven.quillycrawler.ecs.component.Animation
import io.github.quillraven.quillycrawler.ecs.component.AnimationType
import io.github.quillraven.quillycrawler.ecs.component.Boundary
import io.github.quillraven.quillycrawler.ecs.component.Graphic
import io.github.quillraven.quillycrawler.ecs.system.AnimationSystem
import ktx.app.gdxError
import ktx.math.vec2

private val DEFAULT_SIZE = vec2(16f, 16f)

enum class CharacterType {
    PRIEST,
    SKULL;

    val atlasKey: String = this.name.lowercase()
}

fun World.character(type: CharacterType, position: Vector2) = this.entity {
    val world = this@character
    val animationSystem = world.system<AnimationSystem>()
    val textureAnimation = animationSystem.textureAnimation(type.atlasKey, AnimationType.IDLE)

    it += Boundary(position, vec2(DEFAULT_SIZE.x, DEFAULT_SIZE.y).scl(UNIT_SCALE))
    it += Animation(textureAnimation)
    it += Graphic(Sprite(textureAnimation.getKeyFrame(0f)))
}

fun World.changeAnimation(entity: Entity, characterType: CharacterType, animationType: AnimationType) {
    val animation = entity.getOrNull(Animation) ?: gdxError("Entity $entity has no animation component")
    val animationSystem = system<AnimationSystem>()

    animation.textureAnimation = animationSystem.textureAnimation(characterType.atlasKey, animationType)
}
