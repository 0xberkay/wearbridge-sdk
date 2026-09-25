# WearBridge SDK

Client SDK for **WearBridge** on Wear OS.

WearBridge is a consent-first bridge service for Wear OS. This SDK allows partner applications to communicate with the WearBridge manager service over Android Binder, request permissions, and invoke allowed typed tasks.

## Features

- **Consent-First Architecture:** Calls are validated against on-device user permissions and signature pinning.
- **Typed Task Execution:** Execute tasks such as `core.ping`, `core.capabilities`, `core.diagnostics`, and `adb.dumpsys`.
- **Main Thread Callbacks:** All completion and permission callbacks are safely dispatched on the Android main looper.
- **Lightweight & Self-Contained:** Zero third-party dependencies, standard Android library.

---

## Installation

### Option 1: JitPack

Add the JitPack repository to your root `settings.gradle.kts` (or root `build.gradle.kts`):

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = java.net.URI("https://jitpack.io") }
    }
}
```

Add the dependency in your application module's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.0xberkay:wearbridge-sdk:1.0.0")
}
```

### Option 2: Maven Central / Local Maven

If publishing to Maven Central or `mavenLocal()`:

```kotlin
dependencies {
    implementation("io.github.0xberkay:wearbridge-sdk:1.0.0")
}
```

### Option 3: Direct AAR

Download `wearbridge-sdk-release.aar` from GitHub Releases and place it into your app module's `libs/` folder:

```kotlin
dependencies {
    implementation(files("libs/wearbridge-sdk-release.aar"))
}
```

---

## Integration

### 1. Declare Permission in `AndroidManifest.xml`

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- Required to bind to the WearBridge broker service -->
    <uses-permission android:name="com.zbd.wearbridge.permission.BIND_API" />

    <queries>
        <package android:name="com.zbd.wearbridge" />
    </queries>
</manifest>
```

### 2. Connect and Execute Tasks

```java
import com.zbd.wearbridge.protocol.TaskRequest;
import com.zbd.wearbridge.protocol.TaskResult;
import com.zbd.wearbridge.protocol.TaskStatus;
import com.zbd.wearbridge.protocol.WearBridgeContract;
import com.zbd.wearbridge.sdk.WearBridge;

// 1. Check if WearBridge Manager is installed
if (!WearBridge.isAvailable(context)) {
    // Prompt user to install WearBridge
    return;
}

// 2. Register listeners
WearBridge.addBinderReceivedListener(() -> {
    // Check user permission
    if (WearBridge.checkPermission() != WearBridgeContract.PERMISSION_GRANTED) {
        WearBridge.requestPermission(100);
        return;
    }

    // Execute a task
    TaskRequest request = TaskRequest.create(WearBridgeContract.TASK_CAPABILITIES, null);
    WearBridge.execute(request, result -> {
        if (result.isSuccess()) {
            int shellUid = result.getData().getInt("uid");
            // Process task data
        } else {
            String errorMsg = result.getMessage();
        }
    });
});

WearBridge.addPermissionResultListener((requestCode, result) -> {
    if (result == WearBridgeContract.PERMISSION_GRANTED) {
        // Permission granted, retry call
    }
});

// 3. Bind to the service
WearBridge.bind(context);
```

### 3. Cleanup

When done or in `onDestroy()`:

```java
WearBridge.unbind();
```

---

## Available Tasks

| Task ID | Description | Requires Shell Server |
| --- | --- | --- |
| `core.ping` | Health-check ping to the bridge service | Yes |
| `core.capabilities` | Query supported features and shell UID | No |
| `core.diagnostics` | Read-only device & bridge diagnostic data | No |
| `adb.dumpsys` | Fixed dumpsys output under ADB shell identity | Yes |

---

## Building from Source

```shell
./gradlew test
./gradlew :wearbridge-sdk:assembleRelease
```

The output AAR will be located at:
`wearbridge-sdk/build/outputs/aar/wearbridge-sdk-release.aar`

To publish to Maven Local:

```shell
./gradlew :wearbridge-sdk:publishToMavenLocal
```

---

## License

```text
Copyright 2026 WearBridge Authors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
```
