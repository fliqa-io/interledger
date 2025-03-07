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

// Create a configuration for integration test implementation dependencies
configurations {
    val integrationTestImplementation by getting {
        extendsFrom(configurations.testImplementation.get())
    }
    val integrationTestRuntimeOnly by getting {
        extendsFrom(configurations.testRuntimeOnly.get())
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

// Standard test task
tasks.test {
    useJUnitPlatform()
    testLogging {
        events("PASSED", "FAILED", "SKIPPED")
    }
}

dependencies {
    // Logging
    implementation("org.slf4j:slf4j-api:$loggerVersion")

    // Serialization / Deserialization
    implementation("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    implementation("com.google.code.findbugs:jsr305:3.0.2")

    // Unit tests
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("ch.qos.logback:logback-classic:$logbackClassicVersion")

    // Integration tests - explicitly declare test framework dependencies
    "integrationTestImplementation"("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    "integrationTestRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    "integrationTestImplementation"("ch.qos.logback:logback-classic:$logbackClassicVersion")
    "integrationTestImplementation"(project(":"))

    // Needed for Gradle 9.0
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    "integrationTestRuntimeOnly"("org.junit.platform:junit-platform-launcher")
}

// Add this at the end of your build.gradle.kts file

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url =
                uri("https://maven.pkg.github.com/${project.findProperty("github.repository") ?: System.getenv("GITHUB_REPOSITORY") ?: "fliqa-io/interledger"}")
            credentials {
                username = project.findProperty("github.username") as String? ?: System.getenv("GITHUB_ACTOR")
                password = project.findProperty("github.token") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }

    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            pom {
                name.set("Interledger API Client")
                description.set("Java client for Interledger Open Payments protocol")
                url.set("https://github.com/fliqa-io/interledger")

                developers {
                    developer {
                        id.set("azavrsnik")
                        name.set("Andrej Završnik")
                        email.set("andrej@fliqa.io")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/fliqa-io/interledger.git")
                    developerConnection.set("scm:git:ssh://github.com/fliqa-io/interledger.git")
                    url.set("https://github.com/fliqa-io/interledger")
                }
            }
        }
    }
}