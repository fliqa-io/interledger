import java.security.MessageDigest
import java.util.*

plugins {
    id("java")
    id("maven-publish")
    id("signing")
}

group = "io.fliqa"
version = "1.0.2-SNAPSHOT"

// Take version from parameter or set default
val projectVersion = project.findProperty("release.version") as String? ?: version
version = projectVersion

// Set a proper artifact name
val artifactName = "interledger"

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


publishing {
    repositories {
        // Private internal repository for fliqa-io/packages
        maven {
            name = "Private"
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

            // Set the artifact name explicitly  
            artifactId = artifactName

            pom {
                name.set("Interledger API Client")
                description.set("Java client for Interledger Open Payments protocol")
                url.set("https://github.com/fliqa-io/interledger")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("repo")
                    }
                }

                developers {
                    developer {
                        id.set("drejc")
                        name.set("Andrej Završnik")
                        email.set("andrej@fliqa.io")
                        organization.set("Fliqa d.o.o.")
                        organizationUrl.set("https://fliqa.io")
                    }
                }

                organization {
                    name.set("Fliqa d.o.o.")
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


// Test task to check signing setup
tasks.register("testSigning") {
    doLast {
        val keyId = project.findProperty("signing.keyId")?.toString() ?: System.getenv("SIGNING_KEY_ID")
        val password = project.findProperty("signing.password")?.toString() ?: System.getenv("SIGNING_PASSWORD")
        val secretKey = project.findProperty("signing.secretKey")?.toString() ?: System.getenv("SIGNING_SECRET_KEY")

        /*
        println("Testing signing configuration...")
        println("KeyId length: ${keyId?.length ?: 0}")
        println("Password length: ${password?.length ?: 0}")
        println("SecretKey length: ${secretKey?.length ?: 0}")
        */

        if (!keyId.isNullOrBlank() && !password.isNullOrBlank() && !secretKey.isNullOrBlank()) {
            try {
                // Try to decode the base64 secret key
                val decodedKey = String(Base64.getDecoder().decode(secretKey))
                println("Secret key decoding: SUCCESS")
                println("Decoded key starts with: ${decodedKey.take(30)}...")
            } catch (e: Exception) {
                println("Secret key decoding: FAILED - ${e.message}")
            }
        } else {
            println("Missing required properties")
        }
    }
}

// Signing configuration for Maven Central
signing {
    isRequired = false // Optional signing - won't fail if credentials missing

    val keyId = project.findProperty("signing.keyId")?.toString() ?: System.getenv("SIGNING_KEY_ID")
    val password = project.findProperty("signing.password")?.toString() ?: System.getenv("SIGNING_PASSWORD")
    val secretKey = project.findProperty("signing.secretKey")?.toString() ?: System.getenv("SIGNING_SECRET_KEY")

    if (!keyId.isNullOrBlank() && !password.isNullOrBlank() && !secretKey.isNullOrBlank()) {
        useInMemoryPgpKeys(keyId, secretKey, password)
        sign(publishing.publications["maven"])
    }
}

// Task to create a bundle for Central Portal upload
tasks.register<Zip>("createCentralPortalBundle") {
    group = "publishing"
    description = "Creates a bundle for Central Portal upload"

    dependsOn("publishMavenPublicationToMavenLocal")

    archiveBaseName.set("${project.group}.${artifactName}")
    archiveVersion.set(project.version.toString())
    archiveClassifier.set("bundle")

    val groupPath = project.group.toString().replace('.', '/')
    val repoPath =
        file("${System.getProperty("user.home")}/.m2/repository/${groupPath}/${artifactName}/${project.version}")

    from(repoPath) {
        include("**/*.jar", "**/*.pom", "**/*.module", "**/*.asc", "**/*.md5", "**/*.sha1")
        into("${groupPath}/${artifactName}/${project.version}")
    }

    doFirst {
        // Generate missing checksums and signatures
        val files = fileTree(repoPath) {
            include("**/*.jar", "**/*.pom", "**/*.module")
            exclude("**/*.asc", "**/*.md5", "**/*.sha1")
        }

        files.forEach { file ->
            // Generate MD5 checksum
            val md5File = File(file.parentFile, "${file.name}.md5")
            if (!md5File.exists()) {
                val md5 = file.readBytes().let { bytes ->
                    MessageDigest.getInstance("MD5").digest(bytes).joinToString("") { byte ->
                        "%02x".format(byte)
                    }
                }
                md5File.writeText(md5)
            }

            // Generate SHA1 checksum
            val sha1File = File(file.parentFile, "${file.name}.sha1")
            if (!sha1File.exists()) {
                val sha1 = file.readBytes().let { bytes ->
                    MessageDigest.getInstance("SHA1").digest(bytes).joinToString("") { byte ->
                        "%02x".format(byte)
                    }
                }
                sha1File.writeText(sha1)
            }
        }
    }
}

// Task to upload bundle to Central Portal
tasks.register("uploadToCentralPortal") {
    group = "publishing"
    description = "Uploads the bundle to Central Portal"

    dependsOn("createCentralPortalBundle")

    doLast {
        val username = System.getenv("SONATYPE_USERNAME") ?: project.findProperty("sonatypeUsername") as String?
        val password = System.getenv("SONATYPE_PASSWORD") ?: project.findProperty("sonatypePassword") as String?
        val bundleFile =
            layout.buildDirectory.file("distributions/${project.group}.${artifactName}-${project.version}-bundle.zip")
                .get().asFile

        if (username != null && password != null && bundleFile.exists()) {
            // Central Portal uses basic auth with username:password base64 encoded
            val credentials = Base64.getEncoder().encodeToString("${username}:${password}".toByteArray())

            exec {
                commandLine(
                    "curl", "-X", "POST",
                    "-H", "Authorization: Basic ${credentials}",
                    "-F", "bundle=@${bundleFile.absolutePath}",
                    "https://central.sonatype.com/api/v1/publisher/upload"
                )
            }
        } else {
            throw GradleException("Missing credentials or bundle file not found")
        }
    }
}