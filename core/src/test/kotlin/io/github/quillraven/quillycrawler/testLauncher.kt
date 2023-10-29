@file:JvmName("TestLauncher")

package io.github.quillraven.quillycrawler

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration

fun main() {
    // val application = MapTransitionTest()
    // val application = ShaderTest()
    val application = AIMoveTest()

    Lwjgl3Application(application, Lwjgl3ApplicationConfiguration().apply {
        setTitle(application::class.java.simpleName)
        setWindowedMode(640, 480)
    })
}
