import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
	id("java")
	id("maven-publish")
	id("org.openapi.generator") version "7.11.0"
}

group = "io.fliqa"
version = "1.0-SNAPSHOT"

/*
sourceCompatibility = JavaVersion.VERSION_21
targetCompatibility = JavaVersion.VERSION_21
*/


repositories {
	mavenCentral()
}

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(21))
	}
}

/*

// Fixed OpenAPI Generate Task
abstract class OpenApiGenerateTask @Inject constructor() : DefaultTask() {

	@Input
	var yamlPath: String = "<MISSING>"

	@Input
	var generatedFolder: String = "generated-api"

	@OutputDirectory
	val outputDir: DirectoryProperty = project.objects.directoryProperty().convention(
		project.layout.buildDirectory.dir(generatedFolder)
	                                                                                 )

	private val androidManifestPath = outputDir.map { it.file("src/main/AndroidManifest.xml") }
	private val generatedSrcPath = outputDir.map { it.dir("src/main") }
	private val destinationPath = project.layout.projectDirectory.dir("src/main")

	init {
		outputs.dir(outputDir) // ✅ Ensures Gradle tracks output files
	}

	@TaskAction
	fun generateApi() {
		println("🔹 Generating OpenAPI client from: $yamlPath")
		println("🔹 Output directory: ${outputDir.get().asFile.absolutePath}")

		// ✅ Ensure OpenAPI Generator task is created once
		val openApiTask = project.tasks.withType(GenerateTask::class.java).findByName("openApiGeneratedTask")
			?: project.tasks.register<GenerateTask>("openApiGeneratedTask") {
				generatorName.set("java")
				inputSpec.set(project.layout.projectDirectory.file(yamlPath).asFile.absolutePath)
				outputDir.set(this@OpenApiGenerateTask.outputDir.map { it.asFile.absolutePath })

				apiPackage.set("io.fliqa.interledger.client")
				modelPackage.set("io.fliqa.interledger.client.model")
				packageName.set("io.fliqa.interledger")
				groupId.set("io.fliqa.interledger")

				configOptions.set(
					mapOf(
						"library" to "webclient",
						"hideGenerationTimestamp" to "true",
						"groupId" to "io.fliqa.interledger",
						"artifactId" to "interledger-java-client",
						"useJakartaEe" to "true",
						"androidManifest" to "false",
						"importMappings" to "Amount=io.fliqa.interledger.client.model.Amount",
						"typeMappings" to "Amount=io.fliqa.interledger.client.model.Amount",
						"useOneOfDiscriminatorLookup" to "true"
					     )
				                 )
			}.get()

		// ✅ Run OpenAPI Generator only when the task executes
		project.gradle.taskGraph.whenReady {
			if (this.hasTask(openApiTask)) {
				openApiTask.actions.forEach { action -> action.execute(openApiTask) }
			}
		}

		// ✅ Cleanup & Move Files
		moveAndClean()
	}

	private fun moveAndClean() {
		val androidManifest = androidManifestPath.get().asFile
		val generatedSrc = generatedSrcPath.get().asFile
		val generatedFolderPath = outputDir.get().asFile

		if (androidManifest.exists()) {
			androidManifest.delete()
			println("🗑 Deleted AndroidManifest.xml")
		}

		if (generatedSrc.exists()) {
			project.copy {
				from(generatedSrc)
				into(destinationPath)
			}
			println("📁 Moved generated files to $destinationPath")
		}

		if (generatedFolderPath.exists()) {
			generatedFolderPath.deleteRecursively()
			println("🗑 Cleaned up temporary files")
		}
	}
}
*/

/*// Custom wrapper task for OpenAPI Generator
abstract class OpenApiGenerateTask @Inject constructor() : DefaultTask() {

	@Input
	var yamlPath: String = "<MISSING>"

	@Input
	var generatedFolder: String = "generated-api"

	private val androidManifestPath = project.layout.buildDirectory.dir("$generatedFolder/src/main/AndroidManifest.xml")
	private val generatedSrcPath = project.layout.buildDirectory.dir("$generatedFolder/src/main")
	private val destinationPath = project.layout.projectDirectory.dir("src/main")
	private val generatedFolderPath = project.layout.buildDirectory.dir(generatedFolder)

	@TaskAction
	fun generateApi() {
		println("* Generating OpenAPI client from: $yamlPath")
		println("* Output directory: $generatedFolder")

		// Dynamically create the OpenAPI GenerateTask within this task
*//*
		val openApiTask = project.tasks.withType(GenerateTask::class.java).findByName("openApiGeneratedTask")
			?: project.tasks.register<GenerateTask>("openApiGeneratedTask") {
*//*
		val openApiTask = project.tasks.register<GenerateTask>("generate_" + UUID.randomUUID()) {
			generatorName.set("java")
			inputSpec.set(project.layout.projectDirectory.file(yamlPath).asFile.absolutePath)
			outputDir.set(project.layout.buildDirectory.dir(generatedFolder).get().asFile.absolutePath)

			apiPackage.set("io.fliqa.interledger.client")
			modelPackage.set("io.fliqa.interledger.client.model")
			packageName.set("io.fliqa.interledger")
			groupId.set("io.fliqa.interledger")

			configOptions.set(
				mapOf(
					"library" to "webclient",
					"hideGenerationTimestamp" to "true",
					"groupId" to "io.fliqa.interledger",
					"artifactId" to "interledger-java-client",
					"useJakartaEe" to "true",
					"androidManifest" to "false",
					"importMappings" to "Amount=io.fliqa.interledger.client.model.Amount",
					"typeMappings" to "Amount=io.fliqa.interledger.client.model.Amount",
					"useOneOfDiscriminatorLookup" to "true"
				     )
			                 )
		}

		// Execute OpenAPI Generator within this task
		//openApiTask.get().actions.forEach { it.execute(openApiTask.get()) }

		moveAndClean()
		// Cleanup & Move Generated Files
	}

	private fun moveAndClean() {
		if (androidManifestPath.get().asFile.exists()) {
			project.delete(androidManifestPath)
			println("- Deleted AndroidManifest.xml")
		}

		if (generatedSrcPath.get().asFile.exists()) {
			project.copy {
				from(generatedSrcPath)
				into(destinationPath)
			}
			println("- Moved generated files to $destinationPath")
		}

		*//*if (generatedFolderPath.get().asFile.exists()) {
			project.delete(generatedFolderPath)
			println("- Cleaned up temporary files")
		}*//*
	}
}*/

/*tasks.register<OpenApiGenerateTask>("generateAuthServer") {
yamlPath = project.findProperty("yaml")?.toString() ?: "src/main/resources/api/auth-server.yaml"
generatedFolder = project.findProperty("generatedFolder")?.toString() ?: "generated-auth-server"
}

tasks.register<OpenApiGenerateTask>("generateResourceServer") {
yamlPath = project.findProperty("yaml")?.toString() ?: "src/main/resources/api/resource-server.yaml"
generatedFolder = project.findProperty("generatedFolder")?.toString() ?: "generated-resource-server"
}

tasks.register<OpenApiGenerateTask>("generateWalletServer") {
yamlPath = project.findProperty("yaml")?.toString() ?: "src/main/resources/api/wallet-address-server.yaml"
generatedFolder = project.findProperty("generatedFolder")?.toString() ?: "generated-wallet-server"
}*/

tasks.register<GenerateTask>("generateAuthServer") {
		generatorName.set("java")
		val generatedFolder = project.layout.buildDirectory.dir("generated-auth-server").get().asFile.absolutePath

		inputSpec.set(project.layout.projectDirectory.file("src/main/resources/api/auth-server.yaml").asFile.absolutePath)
		outputDir.set(generatedFolder)

		apiPackage.set("io.fliqa.interledger.client")
		modelPackage.set("io.fliqa.interledger.client.model")
		packageName.set("io.fliqa.interledger")
		groupId.set("io.fliqa.interledger")

		globalProperties.set(
			mapOf("modelDocs" to "false")
		                    )
		configOptions.set(
			mapOf(
				"annotationLibrary" to "none",
				"library" to "native",
				"dateLibrary" to "java8",
				"hideGenerationTimestamp" to "true",
				"androidManifest" to "false"
			     )
		                 )

	doLast {
		delete(layout.buildDirectory.dir("$generatedFolder/src/main/AndroidManifest.xml").get().asFile.absolutePath) // Cleanup

		// Move only the 'src' folder to the desired location
		copy {
			from(layout.buildDirectory.dir("$generatedFolder/src/main").get().asFile.absolutePath)
			into("$rootDir/src/main")
		}
		//delete(layout.buildDirectory.dir(generatedFolder).get().asFile.absolutePath) // Clean up
	}
}

tasks.register<GenerateTask>("generateResourceServer") {
	generatorName.set("java")
	val generatedFolder = project.layout.buildDirectory.dir("generated-resource-server").get().asFile.absolutePath

	inputSpec.set(project.layout.projectDirectory.file("src/main/resources/api/resource-server.yaml").asFile.absolutePath)
	outputDir.set(generatedFolder)

	apiPackage.set("io.fliqa.interledger.client")
	modelPackage.set("io.fliqa.interledger.client.model")
	packageName.set("io.fliqa.interledger")
	groupId.set("io.fliqa.interledger")

	globalProperties.set(
		mapOf("modelDocs" to "false")
	                    )
	configOptions.set(
		mapOf(
			"annotationLibrary" to "none",
			"library" to "native",
			"dateLibrary" to "java8",
			"hideGenerationTimestamp" to "true",
			"androidManifest" to "false"
		     )
	                 )


	doLast {
		delete(layout.buildDirectory.dir("$generatedFolder/src/main/AndroidManifest.xml").get().asFile.absolutePath) // Cleanup

		// Move only the 'src' folder to the desired location
		copy {
			from(layout.buildDirectory.dir("$generatedFolder/src/main").get().asFile.absolutePath)
			into("$rootDir/src/main")
		}
		delete(layout.buildDirectory.dir(generatedFolder).get().asFile.absolutePath) // Clean up
	}
}

tasks.register<GenerateTask>("generateWalletServer") {
	generatorName.set("java")
	val generatedFolder = project.layout.buildDirectory.dir("generated-wallet-server").get().asFile.absolutePath

	inputSpec.set(project.layout.projectDirectory.file("src/main/resources/api/wallet-address-server.yaml").asFile.absolutePath)
	outputDir.set(generatedFolder)

	apiPackage.set("io.fliqa.interledger.client")
	modelPackage.set("io.fliqa.interledger.client.model")
	packageName.set("io.fliqa.interledger")
	groupId.set("io.fliqa.interledger")

	globalProperties.set(
		mapOf("modelDocs" to "false")
	                    )
	configOptions.set(
		mapOf(
			"annotationLibrary" to "none",
			"library" to "native",
			"dateLibrary" to "java8",
			"hideGenerationTimestamp" to "true",
			"androidManifest" to "false"
		     )
	                 )

	doLast {
		delete(layout.buildDirectory.dir("$generatedFolder/src/main/AndroidManifest.xml").get().asFile.absolutePath) // Cleanup

		// Move only the 'src' folder to the desired location
		copy {
			from(layout.buildDirectory.dir("$generatedFolder/src/main").get().asFile.absolutePath)
			into("$rootDir/src/main")
		}
		delete(layout.buildDirectory.dir(generatedFolder).get().asFile.absolutePath) // Clean up
	}
}

/*tasks.named("build") {
	dependsOn("generateAuthServer", "generateResourceServer", "generateWalletServer")
}*/

val jakartaAnnotationVersion = "2.1.1"
val jacksonVersion = "2.17.1"
val jacksonDatabindVersion = "2.17.1"
val jacksonDatabindNullableVersion = "0.2.6"
val junitVersion = "5.10.2"

dependencies {

	implementation("javax.annotation:javax.annotation-api:1.3.2") // For Java 8+

	implementation("com.google.code.findbugs:jsr305:3.0.2")
	implementation("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
	implementation("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")
	implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonDatabindVersion")
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
	implementation("org.openapitools:jackson-databind-nullable:$jacksonDatabindNullableVersion")
	implementation("jakarta.annotation:jakarta.annotation-api:$jakartaAnnotationVersion")

	testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
}


tasks.test {
	useJUnitPlatform()
}