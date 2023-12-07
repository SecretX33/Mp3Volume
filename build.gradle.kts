import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
    kotlin("jvm") version "1.9.21"
//    id("org.beryx.runtime") version "1.9.0"
    application
}

group = "com.github.secretx33"
version = "1.0-SNAPSHOT"

val javaVersion = 17

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("io.github.secretx33:path-matching-resource-pattern-resolver:0.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    implementation("ch.qos.logback:logback-classic:1.4.11")
    implementation(platform("com.fasterxml.jackson:jackson-bom:2.16.0"))
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation(platform("io.arrow-kt:arrow-stack:1.2.1"))
    implementation("io.arrow-kt:arrow-core")
    implementation("io.arrow-kt:arrow-fx-coroutines")
    implementation("com.github.jbellis:jamm:0.4.0")  // VM arg: -javaagent:jamm-0.4.0.jar
    implementation("com.googlecode.soundlibs:mp3spi:1.9.5.4")
}

tasks.test { useJUnitPlatform() }

tasks.jar { enabled = false }

artifacts.archives(tasks.shadowJar)

tasks.shadowJar {
    archiveFileName.set("${rootProject.name}.jar")
}

tasks.withType<JavaCompile> {
    options.apply {
        release.set(javaVersion)
        options.encoding = "UTF-8"
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict", "-Xjvm-default=all")
        jvmTarget = javaVersion.toString()
    }
}

val mainClassName = "com.github.secretx33.mp3volume.Mp3VolumeKt"

application {
    mainClass.set(mainClassName)
}

// Enables the usage of Java classes inside 'kotlin' package
sourceSets {
    val allSourceMain = listOf("src/main/java", "src/main/kotlin")
    main {
        listOf(java, kotlin).forEach {
            it.setSrcDirs(allSourceMain)
        }
    }
}