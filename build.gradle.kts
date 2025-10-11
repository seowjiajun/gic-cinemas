plugins {
    id("java")
    id("com.diffplug.spotless") version "8.0.0"
}

group = "com.gic.cinemas"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

subprojects {
    apply(plugin = "com.diffplug.spotless")

    spotless {
        java {
            googleJavaFormat("1.17.0")
            removeUnusedImports()
            importOrder()
            trimTrailingWhitespace()
            endWithNewline()
        }
    }
}
