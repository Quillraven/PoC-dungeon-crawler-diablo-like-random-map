@file:JvmName("Launcher")

package io.github.quillraven.quillycrawler

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration

fun main() {
    Lwjgl3Application(MapTransitionTest(), Lwjgl3ApplicationConfiguration().apply {
        setTitle("Test Quilly Crawler")
        setWindowedMode(640, 480)
    })
}
