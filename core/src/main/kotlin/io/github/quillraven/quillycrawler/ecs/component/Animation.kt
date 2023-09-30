package io.github.quillraven.quillycrawler.ecs.component

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

typealias TextureAnimation = com.badlogic.gdx.graphics.g2d.Animation<TextureRegion>

enum class AnimationType {
    IDLE;

    val atlasKey: String = this.name.lowercase()
}

data class Animation(
    var textureAnimation: TextureAnimation,
    var stateTime: Float = 0f,
    var loop: Boolean = true,
) : Component<Animation> {
    override fun type() = Animation

    companion object : ComponentType<Animation>()
}
