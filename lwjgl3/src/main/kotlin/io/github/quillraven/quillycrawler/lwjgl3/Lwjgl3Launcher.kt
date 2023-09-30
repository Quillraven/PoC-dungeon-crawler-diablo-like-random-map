@file:JvmName("Lwjgl3Launcher")

package io.github.quillraven.quillycrawler.lwjgl3

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import io.github.quillraven.quillycrawler.QuillyCrawler

/** Launches the desktop (LWJGL3) application. */
fun main() {
    // This handles macOS support and helps on Windows.
    if (StartupHelper.startNewJvmIfRequired()) {
        return
    }

    Lwjgl3Application(QuillyCrawler(), Lwjgl3ApplicationConfiguration().apply {
        setTitle("quilly-crawler")
        setWindowedMode(640, 480)
        setWindowIcon(*(arrayOf(128, 64, 32, 16).map { "libgdx$it.png" }.toTypedArray()))
        useVsync(true)
    })
}
