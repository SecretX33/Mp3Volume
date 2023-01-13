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
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.7.3")
//    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions:1.2.2")

    implementation("ch.qos.logback:logback-classic:1.4.11")
//    implementation("org.seleniumhq.selenium:selenium-java:4.3.0")
//    compileOnly("org.apache.commons:commons-lang3:3.12.0")
//    implementation("org.apache.commons:commons-text:1.9")
//    implementation("com.google.code.gson:gson:2.9.0")
    implementation(platform("com.fasterxml.jackson:jackson-bom:2.15.2"))
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
//    implementation("org.apache.commons:commons-compress:1.21")
//    implementation("com.google.guava:guava:31.1-jre")
//    implementation("org.jgrapht:jgrapht-core:1.5.1")
//    implementation("org.jgrapht:jgrapht-io:1.5.1")
    implementation(platform("io.arrow-kt:arrow-stack:1.2.1"))
    implementation("io.arrow-kt:arrow-core")
    implementation("io.arrow-kt:arrow-fx-coroutines")
//    implementation("it.unimi.dsi:fastutil:8.5.12")
//    implementation("it.unimi.dsi:fastutil-core:8.5.12")
//    implementation("it.unimi.dsi:dsiutils:2.7.3")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
//    implementation("com.opencsv:opencsv:5.7.1")
//    implementation("org.mariuszgromada.math:MathParser.org-mXparser:5.2.1")
//    implementation("com.github.albfernandez:juniversalchardet:2.4.0")
//    implementation("org.apache.pdfbox:pdfbox:3.0.0-beta1")
//    implementation("org.imgscalr:imgscalr-lib:4.2")
//    implementation("org.fusesource.jansi:jansi:2.4.0")
//    implementation("com.discord4j:discord4j-core:3.2.5")
//    implementation("me.xdrop:fuzzywuzzy:1.4.0")
//    implementation("com.github.mpkorstanje:simmetrics-core:4.1.1")
//    implementation("org.apache.commons:commons-text:1.10.0")
//    implementation("co.elastic.clients:elasticsearch-java:8.10.2")
    implementation("com.github.jbellis:jamm:0.4.0")  // VM arg: -javaagent:jamm-0.4.0.jar
//    implementation("org.jsoup:jsoup:1.16.2")
    implementation("com.googlecode.soundlibs:mp3spi:1.9.5.4")
//    testImplementation("org.apache.logging.log4j:log4j-slf4j-impl")
    testImplementation(platform("org.junit:junit-bom:5.9.2"))
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation(kotlin("test-junit5"))
//    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0")
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

val mainClassName = "com.github.secretx33.kotlinplayground.MainKt"

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

/*runtime {
    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
    modules.set(listOf("java.base"))
    launcher {
//        name = "KotlinPlayground"
//        mainClass.set(mainClassName)
        jvmArgs = listOf("-Xms1m", "-Xmx256m", "-XX:+UseG1GC", "-XX:+DisableExplicitGC", "-Dfile.encoding=UTF-8")
    }
    jpackage {
        installerType = "app-image"
        mainJar = tasks.getByName<ShadowJar>("shadowJar").archiveFileName.get()
        version = "0.1"
        installerOptions = listOf(
            "--description", rootProject.description,
            "--win-menu",
            "--win-per-user-install",
            "--win-shortcut"
        )
    }
}*/

//jlink {
//    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
//    launcher {
//        name = "KotlinPlayground"
//        mainClass.set(mainClassName)
//        jvmArgs = listOf("-Xms1m", "-Xmx256m", "-XX:+UseG1GC", "-XX:+DisableExplicitGC")
//    }
//    jpackage {
//        installerType = appInstallerType
//        installerName = appName
//        appVersion = project.version.toString()
//        if (os.isWindows) {
//            icon = rootProject.file(appIconIco).path
//            installerOptions = listOf(
//                "--description", rootProject.description,
//                "--copyright", appCopyright,
//                "--vendor", appVendor,
//                "--win-dir-chooser",
//                "--win-menu",
//                "--win-per-user-install",
//                "--win-shortcut"
//            )
//        }
//        if (os.isLinux) {
//            icon = rootProject.file(appIconPng).path
//            installerOptions = listOf(
//                "--description", rootProject.description,
//                "--copyright", appCopyright,
//                "--vendor", appVendor,
//                "--linux-shortcut"
//            )
//        }
//        if (os.isMacOsX) {
//            icon = rootProject.file(appIconPng).path
//        }
//    }
//}