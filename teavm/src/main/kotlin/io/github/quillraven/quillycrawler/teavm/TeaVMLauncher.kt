@file:JvmName("TeaVMLauncher")

package io.github.quillraven.quillycrawler.teavm

import com.github.xpenatan.gdx.backends.teavm.TeaApplicationConfiguration
import com.github.xpenatan.gdx.backends.teavm.TeaApplication
import io.github.quillraven.quillycrawler.QuillyCrawler

/** Launches the TeaVM/HTML application. */
fun main() {
    val config = TeaApplicationConfiguration("canvas").apply {
        width = 640
        height = 480
    }
    TeaApplication(QuillyCrawler(), config)
}
