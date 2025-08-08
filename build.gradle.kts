plugins {
    `kotlin-dsl`
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish") version "1.3.1"
    kotlin("jvm") version "2.0.20"
}

group = "io.github.eyadabdullah"
version = "1.0.0"

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
    plugins {
        create("gradle-repository-manager") {
            displayName = "Gradle Repository Manager"
            id = "io.github.eyadabdullah.gradle-repository-manager"
            implementationClass = "io.github.eyadabdullah.gradlerepositorymanager.RepositoryManagerPlugin"
        }
    }
}

kotlin {
    jvmToolchain(17)
}

tasks.test {
    useJUnitPlatform()
}