package io.github.quillraven.quillycrawler.ecs.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

enum class AIMoveType {
    RANDOM, CIRCLE, TO_PLAYER, LINE
}

data class AIMove(val moveType: AIMoveType) : Component<AIMove> {
    override fun type() = AIMove

    companion object : ComponentType<AIMove>()
}
