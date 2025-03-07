plugins {
    id("java")
    id("maven-publish")
}

group = "io.fliqa.client.interledger"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

val jacksonVersion = "2.17.1"
val junitVersion = "5.10.2"
val loggerVersion = "2.0.17"
val logbackClassicVersion = "1.5.17"

// Define source sets for integration tests
sourceSets {
    create("integrationTest") {
        java.srcDir("src/integrationTest/java")
        resources.srcDir("src/integrationTest/resources")
        // Include classes from 'main' and 'test' source sets
        compileClasspath += sourceSets["main"].output + sourceSets["test"].output
        runtimeClasspath += sourceSets["main"].output + sourceSets["test"].output
    }
}

// Create custom integrationTest task
tasks.register<Test>("integrationTest") {
    description = "Runs the integration tests."
    group = "verification"

    // Link this task to the `integrationTest` source set
    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath

    useJUnitPlatform() // JUnit 5 platform for tests

    // Optional: Enable logging for debugging purposes
    testLogging {
        events("PASSED", "FAILED", "SKIPPED")
    }
}

// Don't run integrationTest task with main build (run by hand)
tasks.named("check") {
    dependsOn.remove(tasks.named("integrationTest"))
}

// Standard test task
tasks.test {
    useJUnitPlatform()
    testLogging {
        events("PASSED", "FAILED", "SKIPPED")
    }
}

dependencies {
    implementation("org.slf4j:slf4j-api:$loggerVersion")

    implementation("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")

    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testImplementation("ch.qos.logback:logback-classic:$logbackClassicVersion")
}

// Use the integrationTest source set for integration test dependencies
dependencies {
    "integrationTestImplementation"("org.junit.jupiter:junit-jupiter:$junitVersion")
    "integrationTestImplementation"("ch.qos.logback:logback-classic:$logbackClassicVersion")

    "integrationTestImplementation"(project(":"))
}



