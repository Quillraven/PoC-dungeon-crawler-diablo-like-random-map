package io.github.quillraven.quillycrawler.ecs.system

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.profiling.GLProfiler
import com.github.quillraven.fleks.IntervalSystem
import ktx.log.logger

class DebugSystem : IntervalSystem(enabled = true) {

    private val profiler = GLProfiler(Gdx.graphics).also { it.enable() }

    override fun onTick() {
        Gdx.graphics.setTitle(
            """
            FPS: ${Gdx.graphics.framesPerSecond} |
            Draw calls: ${profiler.drawCalls} |
            Bindings: ${profiler.textureBindings} |
            Entities: ${world.numEntities}
            """.trimIndent()
        )

        profiler.reset()
    }

    companion object {
        private val LOG = logger<DebugSystem>()
    }

}
