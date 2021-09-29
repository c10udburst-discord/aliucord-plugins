import com.aliucord.gradle.AliucordExtension
import com.android.build.gradle.BaseExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.0.2")
        classpath("com.github.Aliucord:gradle:main-SNAPSHOT")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.30")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

fun Project.android(configuration: BaseExtension.() -> Unit) = extensions.getByName<BaseExtension>("android").configuration()
fun Project.aliucord(configuration: AliucordExtension.() -> Unit) = extensions.getByName<AliucordExtension>("aliucord").configuration()

subprojects {
    apply(plugin = "com.android.library")
    apply(plugin = "com.aliucord.gradle")
    apply(plugin = "kotlin-android")

    aliucord {
        author("Cloudburst", 295186738085756929L)
        updateUrl.set("https://raw.githubusercontent.com/C10udburst/aliucord-plugins/selfbotbuilds/updater.json")
        buildUrl.set("https://raw.githubusercontent.com/C10udburst/aliucord-plugins/selfbotbuilds/%s.zip")
    }

    android {
        compileSdkVersion(30)

        defaultConfig {
            minSdk = 24
            targetSdk= 30
            versionCode = 1
            versionName = "1.0"
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
        }

        tasks.withType<KotlinCompile> {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
    }

    dependencies {
        val api by configurations
        val discord by configurations

        discord("com.discord:discord:aliucord-SNAPSHOT")
        api("com.github.Aliucord:Aliucord:main-SNAPSHOT")

        "implementation"("androidx.appcompat:appcompat:1.3.1")
        "implementation"("com.google.android.material:material:1.4.0")
    }
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}