plugins {
    alias(libs.plugins.android.library)
    id("maven-publish")
}

android {
    namespace = "com.zbd.wearbridge.sdk"
    compileSdk = 37

    defaultConfig {
        minSdk = 30
        consumerProguardFiles("consumer-rules.pro")
    }
    buildFeatures { aidl = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    publishing {
        singleVariant("release")
    }
}

val sdkSourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from("src/main/java")
    from("src/main/aidl")
}

dependencies {
    testImplementation(libs.junit)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = "com.zbd.wearbridge"
                artifactId = "wearbridge-sdk"
                version = "1.0.0"
                artifact(sdkSourcesJar)
                pom {
                    name.set("WearBridge SDK")
                    description.set("Consent-first Binder client SDK for WearBridge on Wear OS")
                    url.set("https://github.com/0xberkay/wearbridge-sdk")
                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    developers {
                        developer {
                            id.set("0xberkay")
                            name.set("0xberkay")
                        }
                    }
                    scm {
                        connection.set("scm:git:git://github.com/0xberkay/wearbridge-sdk.git")
                        developerConnection.set("scm:git:ssh://github.com:0xberkay/wearbridge-sdk.git")
                        url.set("https://github.com/0xberkay/wearbridge-sdk")
                    }
                }
            }
        }
    }
}
