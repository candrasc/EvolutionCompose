import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.31"
    id("org.jetbrains.compose") version "1.0.0"
}


group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    implementation(compose.desktop.currentOs)
}

//dependencies {
//    implementation(compose.desktop.currentOs)
//    implementation("androidx.core:core-ktx:1.10.1")
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.2")
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.2")
//}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}


compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "EvolutionCompose"
            packageVersion = "1.0.0"
        }
    }
}
