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

// Exclude Integration tests from test task
tasks.named<Test>("test") {
    exclude("**/*IT.class")
}

tasks.test {
    useJUnitPlatform()
}

val jacksonVersion = "2.17.1"
val junitVersion = "5.10.2"

val jbosLoggerVersion = "3.1.2.Final"

dependencies {
    // logging
    implementation("org.jboss.logmanager:jboss-logmanager:$jbosLoggerVersion")

    implementation("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")

    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
}