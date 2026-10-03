# Awake Template

A minimal Kotlin Multiplatform game starter powered by
[Awake Engine](https://github.com/awakekt/awake).

Targets included:

- Android and iOS
- Desktop JVM with Vulkan
- Web/Wasm with WebGPU
- Optional Ktor server

## Quick Start

Requirements: JDK 17+, Android SDK for Android builds, Xcode for iOS, Vulkan for desktop, and a
WebGPU-capable browser for web.

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

Shared game setup and rendering live in
[`app/shared/src/commonMain/kotlin/com/awakekt/awake/template/Game.kt`](app/shared/src/commonMain/kotlin/com/awakekt/awake/template/Game.kt).
Platform modules only provide the native Awake host.

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
app/shared/       Shared game lifecycle and RenderPlan
app/androidApp/   Android Vulkan host
app/desktopApp/   Desktop Vulkan host
app/webApp/       Wasm WebGPU host
app/iosApp/       Xcode iOS host
server/           Optional Ktor backend
```

## License

Apache-2.0. See [LICENSE](LICENSE).
