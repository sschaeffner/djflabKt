import org.gradle.api.tasks.testing.logging.TestLogEvent

group = "xyz.sschaeffner"
version = "0.0.0-SNAPSHOT"

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.4.0"
    id("com.gradleup.shadow") version "9.5.1"
    id("com.github.ben-manes.versions") version "0.54.0"
    kotlin("plugin.serialization") version "2.4.0"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(enforcedPlatform("org.jetbrains.kotlin:kotlin-bom:2.4.0"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("io.ktor:ktor-server-core:3.5.1")
    implementation("io.ktor:ktor-server-core-jvm:3.5.1")
    implementation("io.ktor:ktor-server-netty:3.5.1")
    implementation("io.ktor:ktor-server-auth:3.5.1")
    implementation("io.ktor:ktor-server-content-negotiation:3.5.1")
    implementation("io.ktor:ktor-server-websockets:3.5.1")
    implementation("io.ktor:ktor-server-call-logging:3.5.1")
    implementation("io.ktor:ktor-client-core:3.5.1")
    implementation("io.ktor:ktor-client-cio:3.5.1")
    implementation("io.ktor:ktor-client-content-negotiation:3.5.1")
    implementation("io.ktor:ktor-client-websockets:3.5.1")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.5.1")
    implementation("org.slf4j:slf4j-api:2.0.18")
    implementation("ch.qos.logback:logback-classic:1.5.38")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:6.1.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:6.1.2")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.28.1")
}

tasks {
    test {
        useJUnitPlatform()

        testLogging {
            events = setOf(TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED)
            showStandardStreams = true
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

application {
    mainClass.set("xyz.schaeffner.djflab.AppKt")
}

tasks {
    shadowJar {
        manifest {
            attributes(Pair("Main-Class", "xyz.schaeffner.djflab.AppKt"))
        }
    }
}
