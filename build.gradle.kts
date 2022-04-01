plugins {
    id("me.him188.maven-central-publish") version "1.0.0-dev-3"

    kotlin("jvm") version "1.6.0"
    kotlin("plugin.serialization") version "1.6.20-RC"

    id("net.mamoe.mirai-console") version "2.11.0-M1"
}

group = "org.itxtech"
version = "2.0.0"
description = "强大的 mirai 原生插件加载器。"

kotlin {
    sourceSets {
        all {
            languageSettings.enableLanguageFeature("InlineClasses")
            languageSettings.optIn("kotlin.Experimental")
        }
    }
}

repositories {
    mavenCentral()
    maven("https://maven.aliyun.com/repository/public")
}

dependencies {
    compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")
    compileOnly("org.jetbrains.kotlinx:atomicfu:0.17.0")
}

mavenCentralPublish {
    singleDevGithubProject("iTXTech", "mirai-native")
    licenseAGplV3()
    useCentralS01()
}

tasks.named<Jar>("jar") {
    manifest {
        attributes["Name"] = "iTXTech MiraiNative"
        attributes["Revision"] = Runtime.getRuntime().exec("git rev-parse --short HEAD")
            .inputStream.bufferedReader().readText().trim()
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}
