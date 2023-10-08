package io.github.quillraven.quillycrawler.ecs.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class Inventory(var coins: Int) : Component<Inventory> {
    override fun type() = Inventory

    companion object : ComponentType<Inventory>()
}
