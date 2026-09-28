plugins {
    java
    id("org.jetbrains.intellij.platform") version "2.19.0"
}

group = "com.sabareeshrao"
version = providers.environmentVariable("PLUGIN_VERSION").orElse("0.1.0-SNAPSHOT").get()

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    implementation("com.google.code.gson:gson:2.13.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("junit:junit:4.13.2")

    intellijPlatform {
        intellijIdea("2025.2.6.1")
        bundledPlugin("com.intellij.java")
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "252"
        }
    }
}

tasks {
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release = 21
    }
    test {
        useJUnitPlatform()
    }
}
