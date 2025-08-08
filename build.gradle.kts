plugins {
    `kotlin-dsl`
    id("java-gradle-plugin")
    id("maven-publish")
    kotlin("jvm") version "2.0.20"
}

group = "io.github.eyadabdullah"
version = "1.0.2"

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