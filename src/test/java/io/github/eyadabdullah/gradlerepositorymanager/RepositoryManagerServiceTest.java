package io.github.eyadabdullah.gradlerepositorymanager;

import java.io.IOException;
import java.util.Map;

import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;

import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("java:S2699")
class RepositoryManagerServiceTest extends RepositoryManagerBaseTest {

  @Test
  void testLoadingDefaultRepositories() throws IOException {
    // arrange
    setTestLogLevel("debug");
    configurePluginInSettings();
    // act & assert
    var result = loadAndAssertLoadingProject();
    assertTrue(result.getOutput().contains("configured repository: gradlePluginPortal"));
  }

  @Test
  void testWithMavenCentral_loadingPublicDependencies() throws IOException {
    // arrange
    // enable validate_dependencies to check if gradle could resolve dependencies from repositories
    configurePluginInSettings( """
        RepositoryManager {
          mavenCentral()
          validateDependencies()
        }
        """);
    addPublicDependencies();
    // act & assert
    var result = loadAndAssertLoadingProject();
    assertTrue(result.getOutput().contains("configured repository: mavenCentral"));
    assertTrue(result.getOutput().contains("Validating dependencies:"));

  }

  @Test
  void testAddingSimpleRepo() throws IOException {
    // arrange
    configurePluginInSettings("""
          RepositoryManager {
            repository("https://gitlab.example.com/api/v4/groups/680/-/packages/maven")
          }
        """);
    addPublicDependencies();
    // act & assert
    var result = loadAndAssertLoadingProject();
    assertTrue(result.getOutput().contains("found repository: https://gitlab.example.com/api/v4/groups/680/-/packages/maven"));
  }

  @Test
  void testAddingRepoWithName() throws IOException {
    // arrange
    configurePluginInSettings("""
          RepositoryManager {
            repository("repository", "https://gitlab.example.com/api/v4/groups/680/-/packages/maven")
          }
        """);
    addPublicDependencies();
    // act & assert
    var result = loadAndAssertLoadingProject();
    assertTrue(result.getOutput().contains("found repository: repository\t- https://gitlab.example.com/api/v4/groups/680/-/packages/maven"));
  }

  @Test
  void testAddingRepoWithNameAndCredentials() throws IOException {
    // arrange
    System.setProperty("repository_manager_repo_my_name123_username", "foo");
    System.setProperty("repository_manager_repo_my_name123_url", "https://gitlab.example.com/api/v4/groups/680/-/packages/maven");
    configurePluginInSettings("""
      RepositoryManager {
        repository("repository", "https://gitlab.example.com/api/v4/groups/680/-/packages/maven")
      }
    """);
    addPublicDependencies();
    // act & assert
    var result = loadAndAssertLoadingProject();
    assertTrue(result.getOutput().contains("found repository: repository\t- https://gitlab.example.com/api/v4/groups/680/-/packages/maven"));
    assertTrue(result.getOutput().contains("- found credential: RepositoryCredentials{identifier='my_name123', url='https://gitlab.example.com/api/v4/groups/680/-/packages/maven', tokenName='null', username='foo'}"));
  }


  @Test
  void testAddingRepoWithNameAndCredentialsWithWildcardUrl() throws IOException {
    // arrange
    System.setProperty("repository_manager_repo_my_name123_username", "foo");
    System.setProperty("repository_manager_repo_my_name123_url", "https://gitlab.example.com/.*");
    configurePluginInSettings("""
      RepositoryManager {
        repository("repository", "https://gitlab.example.com/api/v4/groups/680/-/packages/maven")
        repository("repository2", "https://gitlab.example.com/api/v4/groups/42/-/packages/maven")
      }
    """);
    addPublicDependencies();
    // act & assert
    var result = loadAndAssertLoadingProject();
    assertTrue(result.getOutput().contains("found repository: repository\t- https://gitlab.example.com/api/v4/groups/680/-/packages/maven"));
    assertTrue(result.getOutput().contains("found repository: repository2\t- https://gitlab.example.com/api/v4/groups/42/-/packages/maven"));
    assertTrue(result.getOutput().contains("- found credential: RepositoryCredentials{identifier='my_name123', url='https://gitlab.example.com/.*', tokenName='null', username='foo'}"));
  }

  @Test
  void testAddingRepoWithNameAndCredentialsFromEnv() throws IOException {
      // arrange
      var env = Map.ofEntries(
          entry("repository_manager_repo_my_name123_username", "foo"),
          entry("repository_manager_repo_my_name123_url", "https://gitlab.example.com/api/v4/groups/680/-/packages/maven"),
          entry("unrelated_entry", "x")
      );

      setTestLogLevel("debug");

      configurePluginInSettings("""
            RepositoryManager {
              repository("repository", "https://gitlab.example.com/api/v4/groups/680/-/packages/maven")
            }
          """);
      addPublicDependencies();
      // act & assert
      var result = loadAndAssertLoadingProject(env);
      assertTrue(result.getOutput().contains("found repository: repository\t- https://gitlab.example.com/api/v4/groups/680/-/packages/maven"));
      assertTrue(result.getOutput()
          .contains("- found credential: RepositoryCredentials{identifier='my_name123', url='https://gitlab.example.com/api/v4/groups/680/-/packages/maven', tokenName='null', username='foo'}"));
  }

  @Test
  void testOrderingRepositories() throws IOException {
    // arrange
    configurePluginInSettings("""
          RepositoryManager {
            repository("repository 1", "https://gitlab.example.com/api/v4/groups/680/-/packages/maven")
            repository("repository 2", "https://gitlab.example.com/api/v4/groups/680/-/packages/maven")
          }
        """);
    addPublicDependencies();
    // act & assert
    var result = loadAndAssertLoadingProject();
    assertTrue(result.getOutput().contains("found repository: repository 1\t- https://gitlab.example.com/api/v4/groups/680/-/packages/maven"));
    var firstRepoPosition = result.getOutput().indexOf("repository 1");
    assertTrue(result.getOutput().contains("found repository: repository 2\t- https://gitlab.example.com/api/v4/groups/680/-/packages/maven"));
    var secondRepoPosition = result.getOutput().indexOf("repository 2");
    assertTrue(secondRepoPosition > firstRepoPosition);
  }

  @Test
  void testDeactivatedDefaultRepositories() throws IOException {
    // arrange
    configurePluginInSettings( """
        RepositoryManager {
          gradlePluginPortal(false)
          mavenCentral(false)
          mavenLocal(false)
        }
        """);
    addPublicDependencies();
    // act & assert
    var result = loadAndAssertLoadingProject();
    assertFalse(result.getOutput().contains("configured repository: gradlePluginPortal"));
    assertFalse(result.getOutput().contains("configured repository: mavenCentral"));
    assertFalse(result.getOutput().contains("configured repository: mavenLocal"));

  }

  @Test
  void testFailingToFindDependenciesInRepositories() throws IOException {
    // arrange
    configurePluginInSettings( """
        RepositoryManager {
          gradlePluginPortal(false)
          mavenCentral(false)

          repository("repository", "https://gitlab.example.com/api/v4/groups/680/-/packages/maven")
          validateDependencies()
        }
        """);
    addPublicDependencies();
    // act
    var result = GradleRunner.create()
        .withDebug(true)
        .forwardOutput()
        .withProjectDir(tempProjectDir)
        .withPluginClasspath();
    // assert
    assertThrows(Exception.class, result::build);
  }
}
