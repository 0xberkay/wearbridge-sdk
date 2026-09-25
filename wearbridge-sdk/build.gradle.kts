plugins {
    alias(libs.plugins.android.library)
    id("maven-publish")
    id("signing")
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

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

dependencies {
    testImplementation(libs.junit)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = "io.github.0xberkay"
                artifactId = "wearbridge-sdk"
                version = "1.0.0"
                artifact(sdkSourcesJar)
                artifact(javadocJar)

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
                            url.set("https://github.com/0xberkay")
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
        repositories {
            maven {
                name = "sonatype"
                val releasesRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
                url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
                credentials {
                    username = (findProperty("mavenCentralUsername") ?: "") as String
                    password = (findProperty("mavenCentralPassword") ?: "") as String
                }
            }
        }
    }

    signing {
        val signingKeyId = findProperty("signing.keyId") as String?
        if (signingKeyId != null) {
            sign(publishing.publications["release"])
        }
    }
}
