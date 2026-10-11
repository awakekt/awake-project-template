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

## Ship it

Release builds are shrunk and obfuscated, which makes them smaller and harder to take apart: R8 on
Android, ProGuard on desktop, and an optimized, minified build on the web.

```bash
./gradlew :app:desktopApp:packageReleaseDistributionForCurrentOS   # desktop installer
./gradlew :app:androidApp:assembleRelease                          # Android APK
./gradlew :app:webApp:wasmJsBrowserDistribution                    # web
```

- **Keep rules:** Awake's libraries bring the rules for what their native code finds by name. If
  your app finds a class by name, keep it in `app/androidApp/proguard-rules.pro` and
  `app/desktopApp/proguard-rules.pro`.
- **Signing:** the Android release is signed with the debug key so that it installs as it is. Sign
  it with your own key before you publish.

## License

Apache-2.0. See [LICENSE.md](LICENSE.md).
