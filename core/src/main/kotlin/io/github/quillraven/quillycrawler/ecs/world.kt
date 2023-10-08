package io.github.quillraven.quillycrawler.ecs

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityCreateContext
import com.github.quillraven.fleks.World
import io.github.quillraven.quillycrawler.QuillyCrawler.Companion.UNIT_SCALE
import io.github.quillraven.quillycrawler.assets.TextureAtlasAssets
import io.github.quillraven.quillycrawler.controller.PlayerKeyboardController
import io.github.quillraven.quillycrawler.ecs.component.*
import io.github.quillraven.quillycrawler.ecs.system.AnimationSystem
import io.github.quillraven.quillycrawler.event.EventDispatcher
import io.github.quillraven.quillycrawler.event.EventListener
import ktx.app.gdxError
import ktx.math.vec2
import kotlin.math.max

interface EntityType

enum class CharacterType : EntityType {
    PRIEST,
    SKULL;

    val atlasKey: String = this.name.lowercase()
}

enum class PropType : EntityType {
    TORCH,
    COIN;

    val atlasKey: String = this.name.lowercase()
}

fun World.character(type: CharacterType, position: Vector2, extraCfg: EntityCreateContext.(Entity) -> Unit = {}) =
    this.entity {
        val world = this@character
        val animationSystem = world.system<AnimationSystem>()
        val textureAnimation =
            animationSystem.textureAnimation(type.atlasKey, AnimationType.IDLE, TextureAtlasAssets.CHARACTERS)
        val width = textureAnimation.getKeyFrame(0f).regionWidth * UNIT_SCALE
        val height = textureAnimation.getKeyFrame(0f).regionHeight * UNIT_SCALE

        it += Boundary(position, vec2(width, height))
        it += Animation(textureAnimation)
        it += Graphic(Sprite(textureAnimation.getKeyFrame(0f)))
        it += Move(MoveDirection.NONE)

        this.extraCfg(it)
    }

fun World.prop(type: PropType, position: Vector2, extraCfg: EntityCreateContext.(Entity) -> Unit = {}) = this.entity {
    val world = this@prop
    val animationSystem = world.system<AnimationSystem>()
    val textureAnimation = animationSystem.textureAnimation(type.atlasKey, AnimationType.IDLE, TextureAtlasAssets.PROPS)
    val width = textureAnimation.getKeyFrame(0f).regionWidth * UNIT_SCALE
    val height = textureAnimation.getKeyFrame(0f).regionHeight * UNIT_SCALE

    it += Boundary(position, vec2(width, height))
    it += Animation(textureAnimation)
    it += Graphic(Sprite(textureAnimation.getKeyFrame(0f)))

    this.extraCfg(it)
}

//fun World.changeAnimation(entity: Entity, characterType: CharacterType, animationType: AnimationType) {
//    val animation = entity.getOrNull(Animation) ?: gdxError("Entity $entity has no animation component")
//    val animationSystem = system<AnimationSystem>()
//
//    animation.textureAnimation = animationSystem.textureAnimation(characterType.atlasKey, animationType)
//}

fun World.moveTo(entity: Entity, direction: MoveDirection) {
    val move = entity.getOrNull(Move) ?: gdxError("Entity $entity has no move component")
    move.direction = direction
}

fun World.remove(entity: Entity, fadeOutTime: Float, translateTo: Vector2, translateTime: Float) = entity.configure {
    it += Fade(Interpolation.fade, 1f / fadeOutTime, from = 1f, to = 0f)
    it += Translate(Interpolation.circleOut, it[Boundary].position.cpy(), translateTo, 1f / translateTime)
    it += Remove(max(fadeOutTime, translateTime))
}


private fun currentInputProcessor(): InputMultiplexer {
    var processor = Gdx.input.inputProcessor
    if (processor == null) {
        processor = InputMultiplexer()
        Gdx.input.inputProcessor = processor
    } else if (processor !is InputMultiplexer) {
        processor = InputMultiplexer(processor)
        Gdx.input.inputProcessor = processor
    }
    return processor
}

fun World.activateKeyboardController() {
    val processor = currentInputProcessor()
    if (processor.processors.any { it is PlayerKeyboardController }) {
        // already active
        return
    }

    processor.addProcessor(PlayerKeyboardController(this))
}

fun World.registerEventListener() {
    systems.filterIsInstance<EventListener>().forEach { EventDispatcher.register(it) }
}

fun World.deRegisterEventListener() {
    systems.filterIsInstance<EventListener>().forEach { EventDispatcher.deRegister(it) }
}
