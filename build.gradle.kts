plugins {
    `kotlin-dsl`
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish") version "1.3.1"
    kotlin("jvm") version "2.0.20"
}

group = "io.github.eyadabdullah"
version = "1.0.0-SNAPSHOT"

dependencies {
    implementation(gradleApi())
    implementation(localGroovy())
    implementation(kotlin("stdlib"))

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.8.1")
    testImplementation("org.mockito:mockito-core:3.+")
    testImplementation("org.assertj:assertj-core:3.24.2")
}

gradlePlugin {
    website.set("https://github.com/EyadAbdullah/gradle-repository-manager")
    vcsUrl.set("https://github.com/EyadAbdullah/gradle-repository-manager")
    plugins {
        create("gradle-repository-manager") {
            id = "io.github.eyadabdullah.gradle-repository-manager"
            implementationClass = "io.github.eyadabdullah.gradlerepositorymanager.RepositoryManagerPlugin"

            displayName = "Gradle Repository Manager"
            description = "A powerful Gradle plugin that simplifies Maven repository management for your Gradle projects"
            tags.set(listOf("manager", "settings"))
        }
    }
}

kotlin {
    jvmToolchain(17)
}

tasks.test {
    useJUnitPlatform()
}