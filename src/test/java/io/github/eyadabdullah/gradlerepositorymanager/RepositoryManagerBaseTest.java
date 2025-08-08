package io.github.eyadabdullah.gradlerepositorymanager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

abstract class RepositoryManagerBaseTest {

  @TempDir
  protected static File tempProjectDir;
  protected static File settingsFile;
  protected static File buildFile;
  protected static File propertiesFile;

  @BeforeAll
  static void setup() throws IOException {
    // arrange
    log(tempProjectDir.getAbsolutePath());
    settingsFile = new File(tempProjectDir, "settings.gradle");
    buildFile = new File(tempProjectDir, "build.gradle");
    propertiesFile = new File(tempProjectDir, "gradle.properties");
    writeFile(settingsFile, "rootProject.name = 'sample-java-project'");
    // assert
    assertTrue(tempProjectDir.isDirectory(), "Should be a directory ");
  }

  protected void configurePluginInSettings() throws IOException {
    this.configurePluginInSettings("");
  }

  protected void configurePluginInSettings(String configurations) throws IOException {
    writeFile(settingsFile, """
        plugins {
            id ("io.github.eyadabdullah.gradle-repository-manager") version("1.0.0")
        }
        
        rootProject.name = 'sample-java-project'
        """ + configurations);
  }

  protected void addPublicDependencies() throws IOException {
    writeFile(buildFile, """
        plugins {
          id("java")
        }
        
        dependencies {
            testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
            testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
            testImplementation("org.junit.jupiter:junit-jupiter-params:5.8.1")
            testImplementation("org.mockito:mockito-core:4.8.0")
            testImplementation("org.assertj:assertj-core:3.24.2")
        }
        """);
  }

  protected BuildResult loadAndAssertLoadingProject() {
    // act
    var result = GradleRunner.create()
        .withDebug(true)
        .forwardOutput()
        .withProjectDir(tempProjectDir)
        .withPluginClasspath()
        .build();
    // assert
    assertThat(result).isNotNull();
    return result;
  }

  static void log(String message) {
    System.out.println("[RepositoryManager]: " + message);
  }

  static void writeFile(File destination, String content) throws IOException {
    try (BufferedWriter output = new BufferedWriter(new FileWriter(destination))) {
      output.write(content);
    }
  }

}