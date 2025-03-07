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

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
        }
    }
}

tasks.named<Test>("test") {
    //exclude("**/*IT.class")  // Exclude integration tests
}

sourceSets {
    create("integrationTest") {
        java {
            compileClasspath += sourceSets["main"].output + sourceSets["test"].output
            runtimeClasspath += sourceSets["main"].output + sourceSets["test"].output
        }
        resources.srcDir("src/test/java")
    }
}

dependencies {
    "integrationTestImplementation"("org.junit.jupiter:junit-jupiter-api:5.10.2")
    "integrationTestImplementation"("org.junit.jupiter:junit-jupiter-engine:5.10.2")
}

tasks.register<Test>("integrationTest") {
    description = "Runs only integration tests"
    group = "verification"

    include("**/*IT.class")

    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
    useJUnitPlatform()
}

tasks.named("check") {
    dependsOn("integrationTest")
}

val jacksonVersion = "2.17.1"
val junitVersion = "5.10.2"
val loggerVersion = "2.0.17"
val logbackClassicVersion = "1.5.17"

dependencies {
    // logging
    implementation("org.slf4j:slf4j-api:$loggerVersion")

    implementation("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")

    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testImplementation("ch.qos.logback:logback-classic:$logbackClassicVersion")
}
