plugins {
    id("java")
}

group = "com.gic.cinemas.common"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Optional: lets you annotate DTOs without forcing Spring on the CLI
    compileOnly("jakarta.validation:jakarta.validation-api:3.1.0")
    compileOnly("com.fasterxml.jackson.core:jackson-annotations:2.18.1")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(17)) }
    withSourcesJar()
}