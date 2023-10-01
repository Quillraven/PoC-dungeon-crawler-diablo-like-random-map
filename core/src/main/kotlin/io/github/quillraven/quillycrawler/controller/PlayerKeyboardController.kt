package io.github.quillraven.quillycrawler.controller

import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.github.quillraven.fleks.World
import io.github.quillraven.quillycrawler.ecs.component.MoveDirection

class PlayerKeyboardController(private val world: World) : InputAdapter() {

    private val moveQueue = ArrayDeque<Int>()

    // TODO make mapping configurable
    private val keyMapping = mutableMapOf(
        Input.Keys.W to ControlActionMove(world, MoveDirection.UP),
        Input.Keys.S to ControlActionMove(world, MoveDirection.DOWN),
        Input.Keys.A to ControlActionMove(world, MoveDirection.LEFT),
        Input.Keys.D to ControlActionMove(world, MoveDirection.RIGHT),
    )

    private fun isMoveKey(keycode: Int): Boolean = keyMapping[keycode] is ControlActionMove

    override fun keyDown(keycode: Int): Boolean {
        if (isMoveKey(keycode)) {
            moveQueue.addFirst(keycode)
        }

        keyMapping[keycode]?.execute() ?: return false
        return true
    }

    override fun keyUp(keycode: Int): Boolean {
        if (isMoveKey(keycode)) {
            moveQueue.removeIf { it == keycode }
            if (moveQueue.isEmpty()) {
                ControlActionMove(world, MoveDirection.NONE).execute()
            } else {
                keyMapping[moveQueue.first()]?.execute()
            }
            return true
        }

        return false
    }

}
