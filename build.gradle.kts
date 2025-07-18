plugins {
    id("java")
    id("maven-publish")
    id("signing")
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
}

group = "io.fliqa"
version = "1.0.0-SNAPSHOT"

// Take version from parameter or set default
val projectVersion = project.findProperty("release.version") as String? ?: version
version = projectVersion

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withJavadocJar()
    withSourcesJar()
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

// Configure duplicate handling for integration test resources
tasks.named<ProcessResources>("processIntegrationTestResources") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// Standard test task
tasks.test {
    useJUnitPlatform()
    testLogging {
        events("PASSED", "FAILED", "SKIPPED")
    }
}

tasks.withType<Javadoc>().configureEach {
    (options as StandardJavadocDocletOptions).apply {
        addStringOption("Xdoclint:all/public")
    }
    isFailOnError = false
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

// Nexus publishing configuration for Maven Central
nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            username.set(System.getenv("SONATYPE_USERNAME") ?: project.findProperty("sonatypeUsername") as String?)
            password.set(System.getenv("SONATYPE_PASSWORD") ?: project.findProperty("sonatypePassword") as String?)
        }
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/fliqa-io/packages")
            credentials {
                username = project.findProperty("github.username") as String? ?: System.getenv("GITHUB_ACTOR")
                password = project.findProperty("github.token") as String? ?: System.getenv("PACKAGES_TOKEN")
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

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("repo")
                    }
                }

                developers {
                    developer {
                        id.set("azavrsnik")
                        name.set("Andrej Završnik")
                        email.set("andrej@fliqa.io")
                        organization.set("Fliqa")
                        organizationUrl.set("https://fliqa.io")
                    }
                }

                organization {
                    name.set("Fliqa")
                    url.set("https://fliqa.io")
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

// Signing configuration for Maven Central
signing {
    val signingKeyId = System.getenv("SIGNING_KEY_ID") ?: project.findProperty("signing.keyId") as String?
    val signingPassword = System.getenv("SIGNING_PASSWORD") ?: project.findProperty("signing.password") as String?
    val signingSecretKey = System.getenv("SIGNING_SECRET_KEY") ?: project.findProperty("signing.secretKey") as String?

    if (signingKeyId != null && signingPassword != null && signingSecretKey != null) {
        useInMemoryPgpKeys(signingKeyId, signingSecretKey, signingPassword)
        sign(publishing.publications["maven"])
    }
}