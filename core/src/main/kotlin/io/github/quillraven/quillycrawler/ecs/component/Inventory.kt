package io.github.quillraven.quillycrawler.ecs.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class Inventory(var coins: Int = 0, var goldenKeys: Int = 0, var silverKeys: Int = 0) : Component<Inventory> {
    override fun type() = Inventory

    companion object : ComponentType<Inventory>()
}
