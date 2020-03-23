plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.70"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.3.70"
}

group = "org.itxtech"
version = "1.0-SNAPSHOT"

kotlin {
    sourceSets {
        all {
            languageSettings.enableLanguageFeature("InlineClasses")
            languageSettings.useExperimentalAnnotation("kotlin.Experimental")
        }
    }
}

repositories {
    maven { setUrl("https://mirrors.huaweicloud.com/repository/maven") }
    maven { setUrl("https://dl.bintray.com/him188moe/mirai") }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.4")

    implementation("net.mamoe:mirai-core-jvm:0.29.1")
    implementation("net.mamoe:mirai-console:0.3.5")
}

tasks.named<Jar>("jar") {
    manifest {
        attributes(
            mapOf(
                "Revision" to Runtime.getRuntime().exec("git rev-parse --short HEAD")
                    .inputStream.bufferedReader().readText().trim(),
                "Name" to "iTXTech MiraiNative"
            )
        )
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
