plugins {
    id("java")
    application
}

group = "com.gic.cinemas.cli"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

application {
    mainClass.set("com.gic.cinemas.cli.CinemaCli")
}

dependencies {
    implementation(project(":common"))
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.1") // JSON parsing
    implementation("org.slf4j:slf4j-simple:2.0.13") // simple logging (optional)
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}