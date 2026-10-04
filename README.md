# Awake Template

A Kotlin Multiplatform game starter powered by [Awake Engine](https://github.com/awakekt/awake).
It is the Awake Player: it plays the Awake project bundled in its resources, the same kind of
project Awake Studio saves. The sample is a spinning cube; Awake Studio's export puts your project
in its place.

Targets included:

- Android and iOS
- Desktop JVM with Vulkan
- Web/Wasm with WebGPU
- Optional Ktor server

## Quick Start

Requirements: JDK 17+, Android SDK for Android builds, Xcode for iOS, Vulkan for desktop (on macOS,
`brew install molten-vk vulkan-loader`), and a WebGPU-capable browser for web.

```bash
# Check the project
./gradlew help

# Run desktop Vulkan
./gradlew :app:desktopApp:run

# Run the Wasm/WebGPU development server
./gradlew :app:webApp:wasmJsBrowserDevelopmentRun

# Build an Android debug APK
./gradlew :app:androidApp:assembleDebug
```

Open `app/iosApp/iosApp.xcodeproj` in Xcode to run the iOS app.

## Where To Start

The project the app plays is in
[`app/shared/src/commonMain/resources/project/`](app/shared/src/commonMain/resources/project/):
`awake.project.json` names its entry scene, and Core's project runtime plays it with the systems its
components call for. Replace the folder with a project from Awake Studio to play that instead.

[`Game.kt`](app/shared/src/commonMain/kotlin/com/awakekt/awake/template/Game.kt) reads the project
and builds the app around it; add your own Kotlin there.
[`RenderPlan.kt`](app/shared/src/commonMain/kotlin/com/awakekt/awake/template/RenderPlan.kt) builds
the pipelines a Studio project can draw with. Platform modules only provide the native Awake host.

Desktop and Android read the project from the app's resources. The web and iOS hosts compile, but
don't bundle the project folder yet: the web app needs it next to `index.html`, and the iOS app
needs it added to the app bundle in Xcode.

The version catalog has separate `awake` (Core) and `awake-vulkan` pins in
[`gradle/libs.versions.toml`](gradle/libs.versions.toml). Core modules such as the engine bootstrap,
shader assets, and WebGPU backend use the Core version; the Vulkan backend uses the Vulkan family
version. For development snapshots, Gradle resolves from Maven Central's snapshot repository. For
consumer-facing builds, set both pins to published non-snapshot releases. For local engine
development, keep the Awake checkout next to this repository as `../awaken`; Gradle will substitute
the local modules automatically.

Change `com.awakekt.awake.template` to your package, then customize `Game.kt` and add only the Awake
modules your application needs.

## Project Layout

```text
app/shared/       Shared game lifecycle, RenderPlan and the bundled project
app/androidApp/   Android Vulkan host
app/desktopApp/   Desktop Vulkan host
app/webApp/       Wasm WebGPU host
app/iosApp/       Xcode iOS host
server/           Optional Ktor backend
```

## License

Apache-2.0. See [LICENSE](LICENSE).
