# Awake Project Template

A Kotlin Multiplatform app that plays an [Awake Studio](https://studio.awakekt.com) project, built on
[Awake Engine](https://github.com/awakekt/awake). Awake Studio's **Export App Project** starts from
this template. To write your game in Kotlin instead, use
[awake-template](https://github.com/awakekt/awake-template).

## Run it

You need JDK 17 or newer. On macOS, desktop needs Vulkan: `brew install molten-vk vulkan-loader`.

```bash
./gradlew :app:desktopApp:run                # desktop
./gradlew :app:androidApp:assembleDebug      # Android APK (needs the Android SDK)
./gradlew :app:webApp:wasmJsBrowserDistribution   # web: a folder in app/webApp/build/dist to host anywhere
```

Or open the folder in Android Studio and run `androidApp`.

## Your project

The app plays the project in `app/shared/src/commonMain/resources/project/`. The sample is a spinning
cube. Replace the folder with a project saved from Awake Studio to play yours.

| File | What it does |
|---|---|
| `app/shared/.../Game.kt` | Loads the project and starts the game |
| `app/shared/.../RenderPlan.kt` | Sets up drawing for everything a Studio project can contain |
| `gradle/libs.versions.toml` | Pins Awake Core (`awake`), Vulkan (`awake-vulkan`), and the WebGPU snapshot (`awake-webgpu`) |

The shared module selects a project runtime adapter from the Core version in the catalog:
`core2Main` supports the older API; `core3Main` uses the renamed Core 0.3 API. This lets engine
consumer checks build the template against newer Core artifacts while its default pin stays on a
published release.

The web build needs WebGPU in the browser, and serves the project next to `index.html`. The iOS build
doesn't include the project yet.

## Your game's code

Gameplay a project can't express as data is Kotlin code in `app/shared`: a `SceneCapability` object that
registers the game's own scene components and adds the systems that run them. Name it in the project's
`awake.project.json`, and the app links it:

```json
"plugins": [{ "id": "com.example.my-game.beacon", "capabilityClass": "com.example.mygame.BeaconCapability", "required": true }]
```

- The build generates `projectCapabilities` from those entries, and `loadGame` passes it to `loadProject`,
  so the manifest is the one list. Core refuses a project whose `required` capability isn't linked.
- An entry that names an `artifact` (`group`, `name`, `version`) is a published capability: the build adds
  that library as well.
- `app/shared` applies the Kotlin serialization plugin, which a scene component needs.
- Awake Studio's desktop editor builds this module and plays the project with the same capabilities, so it
  plays in Studio as it does here. Leaving out `path`, as above, needs Core 0.6.0 or newer.

## License

Apache-2.0. See [LICENSE.md](LICENSE.md).
