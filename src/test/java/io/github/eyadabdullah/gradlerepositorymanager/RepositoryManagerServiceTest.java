package io.github.eyadabdullah.gradlerepositorymanager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.gradle.api.Action;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import io.github.eyadabdullah.gradlerepositorymanager.exceptions.MissingRepositoryCredentials;
import io.github.eyadabdullah.gradlerepositorymanager.extension.ManageableRepository;
import io.github.eyadabdullah.gradlerepositorymanager.extension.ManageableRepositoryImpl;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

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

  @Test
  void addRepository_fails_noCredentialsFoundButRepositoryRequiresAuthentication() {
    // arrange
    var repositoryManagerService = new RepositoryManagerService();

    var repoHandler = mock(RepositoryHandler.class);
    mockMavenCall(repoHandler);

    ManageableRepository repository = new ManageableRepositoryImpl("someName");
    repository.setUrl("https://gitlab.example.com/api/v4/groups/680/-/packages/maven");
    repository.setRequireAuthentication(true);

    // act
    var result = assertThatThrownBy(() -> repositoryManagerService.addRepository(repoHandler, repository));

    // assert
    result.isInstanceOf(MissingRepositoryCredentials.class);
  }

  @Test
  void addRepository_fails_credentialsFoundWithoutTokenAndPasswordButRepositoryRequiresAuthentication() {
    // arrange
    var repositoryManagerService = new RepositoryManagerService();

    var repoHandler = mock(RepositoryHandler.class);
    mockMavenCall(repoHandler);

    ManageableRepository repository = new ManageableRepositoryImpl("someName");
    repository.setUrl("https://gitlab.example.com/api/v4/groups/680/-/packages/maven");
    repository.setRequireAuthentication(true);

    var credential = new RepositoryCredentials("repository_manager_repo_name_url");
    credential.setUrl(repository.getUrl());

    repositoryManagerService.setRepositoryCredentials(List.of(credential));

    // act
    var result = assertThatThrownBy(() -> repositoryManagerService.addRepository(repoHandler, repository));

    // assert
    result.isInstanceOf(MissingRepositoryCredentials.class);
  }

  private static List<? super MavenArtifactRepository> mockMavenCall(RepositoryHandler repoHandler) {
    List<? super MavenArtifactRepository> mavenArtifactRepositories = new ArrayList<>();
    doAnswer(args -> {
        Action<? super MavenArtifactRepository> action = args.getArgument(0);
        var repo = mock(MavenArtifactRepository.class);
        action.execute(repo);
        mavenArtifactRepositories.add(repo);
        return repo;
      })
      .when(repoHandler)
      .maven(ArgumentMatchers.<Action<? super MavenArtifactRepository>>any());
    return mavenArtifactRepositories;
  }

  @Test
  void addRepository_successful_tokenCredentialsFoundAndRepositoryRequiresAuthentication() {
    // arrange
    var repoHandler = mock(RepositoryHandler.class);
    var processedRepositories = mockMavenCall(repoHandler);

    ManageableRepository repository = new ManageableRepositoryImpl("someName");
    repository.setUrl("https://gitlab.example.com/api/v4/groups/680/-/packages/maven");
    repository.setRequireAuthentication(true);

    var repositoryManagerService = new RepositoryManagerService();

    var credential = new RepositoryCredentials("repository_manager_repo_name_url");
    credential.setUrl(repository.getUrl());
    credential.setTokenName("token_name");
    credential.setTokenValue("token_value");

    repositoryManagerService.setRepositoryCredentials(List.of(credential));

    // act
    repositoryManagerService.addRepository(repoHandler, repository);

    // assert
    assertThat(processedRepositories).hasSize(1);
  }

  @Test
  void addRepository_successful_passwordCredentialsFoundAndRepositoryRequiresAuthentication() {
    // arrange
    var repoHandler = mock(RepositoryHandler.class);
    var processedRepositories = mockMavenCall(repoHandler);

    ManageableRepository repository = new ManageableRepositoryImpl("someName");
    repository.setUrl("https://gitlab.example.com/api/v4/groups/680/-/packages/maven");
    repository.setRequireAuthentication(true);

    var repositoryManagerService = new RepositoryManagerService();

    var credential = new RepositoryCredentials("repository_manager_repo_name_url");
    credential.setUrl(repository.getUrl());
    credential.setUsername("token_name");
    credential.setPassword("token_value");

    repositoryManagerService.setRepositoryCredentials(List.of(credential));

    // act
    repositoryManagerService.addRepository(repoHandler, repository);

    // assert
    assertThat(processedRepositories).hasSize(1);
  }

  @Test
  void addRepository_successful_noCredentialsFoundAndRepositoryNotRequiresAuthentication() {
    // arrange
    var repoHandler = mock(RepositoryHandler.class);
    var processedRepositories = mockMavenCall(repoHandler);

    ManageableRepository repository = new ManageableRepositoryImpl("someName");
    repository.setUrl("https://gitlab.example.com/api/v4/groups/680/-/packages/maven");

    var repositoryManagerService = new RepositoryManagerService();

    // act
    repositoryManagerService.addRepository(repoHandler, repository);

    // assert
    assertThat(processedRepositories).hasSize(1);
  }
}
