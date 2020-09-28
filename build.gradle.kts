plugins {
    kotlin("jvm") version "1.4.10"
    kotlin("plugin.serialization") version "1.4.10"
    id("com.jfrog.bintray") version "1.8.5"
    `maven-publish`
}

group = "org.itxtech"
version = "1.9.2"
description = "强大的 mirai 原生插件加载器。"
val vcs = "https://github.com/iTXTech/mirai-native"

kotlin {
    sourceSets {
        all {
            languageSettings.enableLanguageFeature("InlineClasses")
            languageSettings.useExperimentalAnnotation("kotlin.Experimental")
        }
    }
}

repositories {
    maven("https://maven.aliyun.com/repository/public")
    maven("https://dl.bintray.com/him188moe/mirai")
    maven("https://dl.bintray.com/kotlin/kotlin-eap")
}

dependencies {
    api("org.jetbrains.kotlinx:atomicfu:0.14.4")

    implementation("net.mamoe:mirai-core:1.3.1")
    implementation("net.mamoe:mirai-console:1.0-RC-dev-29")
}

tasks.named<Jar>("jar") {
    manifest {
        attributes["Name"] = "iTXTech MiraiNative"
        attributes["Revision"] = Runtime.getRuntime().exec("git rev-parse --short HEAD")
            .inputStream.bufferedReader().readText().trim()
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

bintray {
    user = findProperty("buser") as String?
    key = findProperty("bkey") as String?
    setPublications("mavenJava")
    setConfigurations("archives")
    pkg.apply {
        repo = "mirai"
        name = "mirai-native"
        userOrg = "itxtech"
        setLicenses("AGPLv3")
        publicDownloadNumbers = true
        vcsUrl = vcs
    }
}

@Suppress("DEPRECATION")
val sourcesJar by tasks.registering(Jar::class) {
    classifier = "sources"
    from(sourceSets["main"].allSource)
}

publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])

            groupId = rootProject.group.toString()
            this.artifactId = artifactId
            version = version

            pom.withXml {
                val root = asNode()
                root.appendNode("description", description)
                root.appendNode("name", project.name)
                root.appendNode("url", vcs)
                root.children().last()
            }
            artifact(sourcesJar.get())
        }
    }
}
