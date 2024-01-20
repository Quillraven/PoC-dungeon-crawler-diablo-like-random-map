# PoC for a dungeon crawler with random map generation

### Technologies

- LibGDX
- Kotlin
- Fleks entity component system
- LibKTX Kotlin extensions for LibGDX

### Features

- tile based movement
- random map creation of predefined map segment (like in Diablo 2)
- dissolve shader for removing enemies
- AI movement that reacts on player movement (like in Lufia 2 on SNES)

### How to check out the features?

There is `test` sourceset in the `core` project that contains test classes
for the different features. `testLauncher.kt` is the main class to run the tests.
There are following tests that can be executed:
- `ShaderTest`: press '1' to stop the shader and '2' to start the shader
- `MapTransitionTest`
- `AIMoveTest`

The shader itself can be found in the `assets/shaders` folder. Only the fragment
shader is needed and is documented.
