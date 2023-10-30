package io.github.quillraven.quillycrawler.ecs

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityCreateContext
import io.github.quillraven.quillycrawler.ecs.component.AIMove
import io.github.quillraven.quillycrawler.ecs.component.AIMoveType
import ktx.log.Logger

private val LOG = Logger("EntityCfg")

fun EntityCreateContext.configureCharacter(entity: Entity, characterType: CharacterType) = when (characterType) {
    CharacterType.SKULL -> entity += AIMove(AIMoveType.RANDOM)
    CharacterType.VAMPIRE -> entity += AIMove(AIMoveType.CIRCLE)
    CharacterType.SKELETON1 -> entity += AIMove(AIMoveType.LINE)
    CharacterType.SKELETON2 -> entity += AIMove(AIMoveType.TO_PLAYER)

    else -> LOG.debug { "No entity configuration for CharacterType $characterType" }
}
