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
| `gradle/libs.versions.toml` | Pins the Awake Core (`awake`) and Vulkan (`awake-vulkan`) versions |

The web build needs WebGPU in the browser, and serves the project next to `index.html`. The iOS build
doesn't include the project yet.

## License

Apache-2.0. See [LICENSE.md](LICENSE.md).
