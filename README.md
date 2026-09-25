# WearBridge SDK

[![Maven Central](https://img.shields.io/maven-central/v/io.github.0xberkay/wearbridge-sdk?color=blue)](https://central.sonatype.com/artifact/io.github.0xberkay/wearbridge-sdk)
[![Google Play](https://img.shields.io/badge/Google_Play-WearBridge_Manager-brightgreen?logo=googleplay&logoColor=white)](https://play.google.com/store/apps/details?id=com.zbd.wearbridge)
[![Build & Test](https://github.com/0xberkay/wearbridge-sdk/actions/workflows/ci.yml/badge.svg)](https://github.com/0xberkay/wearbridge-sdk/actions)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)

Official client SDK for **[WearBridge](https://play.google.com/store/apps/details?id=com.zbd.wearbridge)** on Wear OS.

**WearBridge** is a consent-first bridge service for Wear OS watches. It allows Wear OS applications to execute a curated, typed catalog of privileged ADB shell tasks without needing root, without bundling unsafe binary tools, and with cryptographic package verification.

> [!IMPORTANT]
> **WearBridge Manager Required:** This SDK is a client library. To run tasks and bind to the service, the **WearBridge** manager application must be installed on the watch from Google Play:
> 👉 **[Download WearBridge on Google Play](https://play.google.com/store/apps/details?id=com.zbd.wearbridge)**

---

## Architecture & How It Works

```
┌─────────────────────────────────┐
│     Your Partner App / SDK      │
│  (io.github.0xberkay:wearbridge)│
└────────────────┬────────────────┘
                 │ Android IPC (Binder)
                 ▼
┌─────────────────────────────────┐
│    WearBridge Wear OS Manager   │  ◄── User Consent Prompt
│    (com.zbd.wearbridge)         │  ◄── SHA-256 Certificate Pinning
└────────────────┬────────────────┘
                 │ Localhost / Unix Socket
                 ▼
┌─────────────────────────────────┐
│    app_process Shell Engine     │  ◄── ADB Shell Identity (UID 2000)
│   (Runs typed safe task subset) │
└─────────────────────────────────┘
```

1. **Explicit Consent:** A user explicitly grants access on the watch screen. The watch displays your app's package name and its signing certificate SHA-256 fingerprint.
2. **Restricted Shell Surface:** Third-party partners never obtain an arbitrary shell binder or raw command execution. Only strictly defined, safe tasks are permitted.
3. **No Root Required:** Leverages Android's built-in Wireless Debugging and ADB shell identity.

---

## Installation

### Maven Central (Recommended)

Maven Central is enabled by default in all Android projects. Simply add the dependency to your module's `build.gradle.kts` (or `build.gradle`):

```kotlin
dependencies {
    implementation("io.github.0xberkay:wearbridge-sdk:1.0.0")
}
```

### Alternative: JitPack

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = java.net.URI("https://jitpack.io") }
    }
}

// build.gradle.kts
dependencies {
    implementation("com.github.0xberkay:wearbridge-sdk:1.0.0")
}
```

---

## Integration Guide

### 1. Declare Permission & Queries in `AndroidManifest.xml`

Declare the BIND permission and query filter in your Wear OS app's `AndroidManifest.xml`:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- Required to bind to the WearBridge broker service -->
    <uses-permission android:name="com.zbd.wearbridge.permission.BIND_API" />

    <!-- Required for Android 11+ (API 30+) package visibility -->
    <queries>
        <package android:name="com.zbd.wearbridge" />
    </queries>
</manifest>
```

---

### 2. Verify Manager Installation & Prompt

Before binding, verify that the user has the WearBridge manager installed:

#### Kotlin
```kotlin
if (!WearBridge.isAvailable(context)) {
    // Open WearBridge on Google Play Store
    val playIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.zbd.wearbridge")).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(playIntent)
    return
}
```

#### Java
```java
if (!WearBridge.isAvailable(context)) {
    Intent playIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.zbd.wearbridge"));
    playIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    context.startActivity(playIntent);
    return;
}
```

---

### 3. Connect, Request Permission & Execute Tasks

#### Kotlin Example

```kotlin
import com.zbd.wearbridge.protocol.TaskRequest
import com.zbd.wearbridge.protocol.TaskResult
import com.zbd.wearbridge.protocol.TaskStatus
import com.zbd.wearbridge.protocol.WearBridgeContract
import com.zbd.wearbridge.sdk.WearBridge

class WatchTaskActivity : ComponentActivity() {

    override fun onStart() {
        super.onStart()

        // 1. Listen for connection
        WearBridge.addBinderReceivedListener {
            if (WearBridge.checkPermission() != WearBridgeContract.PERMISSION_GRANTED) {
                // Request user consent on the watch
                WearBridge.requestPermission(REQUEST_CODE_PERMISSION)
                return@addBinderReceivedListener
            }
            executeBridgeTask()
        }

        // 2. Listen for permission grant
        WearBridge.addPermissionResultListener { requestCode, result ->
            if (requestCode == REQUEST_CODE_PERMISSION && result == WearBridgeContract.PERMISSION_GRANTED) {
                executeBridgeTask()
            }
        }

        // 3. Bind to WearBridge
        WearBridge.bind(this)
    }

    private fun executeBridgeTask() {
        val request = TaskRequest.create(WearBridgeContract.TASK_CAPABILITIES, null)
        WearBridge.execute(request) { result: TaskResult ->
            if (result.isSuccess) {
                val shellUid = result.data.getInt("uid")
                Log.d("WearBridge", "Connected with shell UID: $shellUid")
            } else {
                Log.e("WearBridge", "Task failed: ${result.message} (status: ${result.status})")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        WearBridge.unbind()
    }

    companion object {
        private const val REQUEST_CODE_PERMISSION = 1001
    }
}
```

#### Java Example

```java
import com.zbd.wearbridge.protocol.TaskRequest;
import com.zbd.wearbridge.protocol.TaskResult;
import com.zbd.wearbridge.protocol.TaskStatus;
import com.zbd.wearbridge.protocol.WearBridgeContract;
import com.zbd.wearbridge.sdk.WearBridge;

public class WatchBridgeHelper {

    public static void initialize(Context context) {
        WearBridge.addBinderReceivedListener(() -> {
            if (WearBridge.checkPermission() != WearBridgeContract.PERMISSION_GRANTED) {
                WearBridge.requestPermission(100);
                return;
            }

            TaskRequest request = TaskRequest.create(WearBridgeContract.TASK_CAPABILITIES, null);
            WearBridge.execute(request, result -> {
                if (result.isSuccess()) {
                    int shellUid = result.getData().getInt("uid");
                }
            });
        });

        WearBridge.addPermissionResultListener((requestCode, result) -> {
            if (result == WearBridgeContract.PERMISSION_GRANTED) {
                // Permission granted by user, ready to execute
            }
        });

        WearBridge.bind(context);
    }

    public static void dispose() {
        WearBridge.unbind();
    }
}
```

---

## Available Tasks Catalog

| Task ID Constant | Task String | Description | Requires Shell Server |
| --- | --- | --- | --- |
| `WearBridgeContract.TASK_PING` | `core.ping` | Health-check ping to the bridge service | Yes |
| `WearBridgeContract.TASK_CAPABILITIES` | `core.capabilities` | Query supported features, version, and shell UID | No |
| `WearBridgeContract.TASK_DIAGNOSTICS` | `core.diagnostics` | Read-only device & bridge diagnostic data | No |
| `WearBridgeContract.TASK_DUMPSYS` | `adb.dumpsys` | Fixed dumpsys output under ADB shell identity | Yes |

---

## Status & Error Codes

Task execution returns a `TaskResult` object containing an integer status code:

| Status Constant | Code | Description |
| --- | --- | --- |
| `TaskStatus.OK` | `0` | Task executed successfully. |
| `TaskStatus.SERVER_UNAVAILABLE` | `1` | WearBridge service is not running or disconnected. |
| `TaskStatus.ADB_NOT_PAIRED` | `2` | Wireless Debugging is not paired on the watch. |
| `TaskStatus.PERMISSION_DENIED` | `3` | User rejected the permission dialog. |
| `TaskStatus.PARTNER_NOT_ALLOWED` | `4` | Calling package or signature is not recognized. |
| `TaskStatus.TASK_NOT_ALLOWED` | `5` | The requested task ID is not allowed for this partner. |
| `TaskStatus.INVALID_ARGUMENT` | `6` | Invalid arguments provided in `TaskRequest`. |
| `TaskStatus.CAPABILITY_UNAVAILABLE`| `7` | Requested feature is unavailable on this device. |
| `TaskStatus.USER_CANCELLED` | `8` | Operation was cancelled by the user. |
| `TaskStatus.EXECUTION_FAILED` | `9` | Internal task execution failed. |

---

## Building from Source

```shell
# Run unit tests
./gradlew test

# Assemble Release AAR
./gradlew :wearbridge-sdk:assembleRelease

# Publish to Local Maven (~/.m2/repository)
./gradlew :wearbridge-sdk:publishToMavenLocal
```

The output AAR will be located at:
`wearbridge-sdk/build/outputs/aar/wearbridge-sdk-release.aar`

---

## Links

- **Google Play:** [https://play.google.com/store/apps/details?id=com.zbd.wearbridge](https://play.google.com/store/apps/details?id=com.zbd.wearbridge)
- **Maven Central:** [io.github.0xberkay:wearbridge-sdk](https://central.sonatype.com/artifact/io.github.0xberkay/wearbridge-sdk)
- **GitHub Repository:** [0xberkay/wearbridge-sdk](https://github.com/0xberkay/wearbridge-sdk)

---

## License

```text
Copyright 2026 WearBridge Authors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
```
