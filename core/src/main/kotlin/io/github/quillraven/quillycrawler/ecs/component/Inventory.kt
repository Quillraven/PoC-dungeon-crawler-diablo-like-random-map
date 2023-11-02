package io.github.quillraven.quillycrawler.ecs.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class Inventory(var coins: Int, var goldenKeys: Int, var silverKeys: Int) : Component<Inventory> {
    override fun type() = Inventory

    companion object : ComponentType<Inventory>()
}
