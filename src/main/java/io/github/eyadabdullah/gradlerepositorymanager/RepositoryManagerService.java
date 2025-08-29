package io.github.eyadabdullah.gradlerepositorymanager;

import io.github.eyadabdullah.gradlerepositorymanager.extension.ManageableRepository;
import io.github.eyadabdullah.gradlerepositorymanager.extension.RepositoryManagerExtension;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.MavenRepositoryContentDescriptor;
import org.gradle.api.artifacts.repositories.PasswordCredentials;
import org.gradle.api.credentials.HttpHeaderCredentials;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.internal.authentication.DefaultHttpHeaderAuthentication;

import static io.github.eyadabdullah.gradlerepositorymanager.extension.RepositoryManagerExtension.REPOSITORY_DEFINITION_PREFIX;

public class RepositoryManagerService {

  private static final Logger logger = Logging.getLogger(RepositoryManagerService.class);
  private List<RepositoryCredentials> repositoryCredentials = new ArrayList<>();

  // Font Name: ANSI Shadow
  // ref: https://patorjk.com/software/taag/#p=display&v=1&f=ANSI%20Shadow&t=Repository%20Manager
  public static final String BANNER = """
      ==================================================================================================================================================
      
      ██████╗ ███████╗██████╗  ██████╗ ███████╗██╗████████╗ ██████╗ ██████╗ ██╗   ██╗    ███╗   ███╗ █████╗ ███╗   ██╗ █████╗  ██████╗ ███████╗██████╗\s
      ██╔══██╗██╔════╝██╔══██╗██╔═══██╗██╔════╝██║╚══██╔══╝██╔═══██╗██╔══██╗╚██╗ ██╔╝    ████╗ ████║██╔══██╗████╗  ██║██╔══██╗██╔════╝ ██╔════╝██╔══██╗
      ██████╔╝█████╗  ██████╔╝██║   ██║███████╗██║   ██║   ██║   ██║██████╔╝ ╚████╔╝     ██╔████╔██║███████║██╔██╗ ██║███████║██║  ███╗█████╗  ██████╔╝
      ██╔══██╗██╔══╝  ██╔═══╝ ██║   ██║╚════██║██║   ██║   ██║   ██║██╔══██╗  ╚██╔╝      ██║╚██╔╝██║██╔══██║██║╚██╗██║██╔══██║██║   ██║██╔══╝  ██╔══██╗
      ██║  ██║███████╗██║     ╚██████╔╝███████║██║   ██║   ╚██████╔╝██║  ██║   ██║       ██║ ╚═╝ ██║██║  ██║██║ ╚████║██║  ██║╚██████╔╝███████╗██║  ██║
      ╚═╝  ╚═╝╚══════╝╚═╝      ╚═════╝ ╚══════╝╚═╝   ╚═╝    ╚═════╝ ╚═╝  ╚═╝   ╚═╝       ╚═╝     ╚═╝╚═╝  ╚═╝╚═╝  ╚═══╝╚═╝  ╚═╝ ╚═════╝ ╚══════╝╚═╝  ╚═╝      
      
      By Eyad Abdullah                                         
      ==================================================================================================================================================
      """;

  /**
   * We are surpassing UnstableApiUsage as the systemPropertiesPrefixedBy feature is still a work in progress and my change in the future.
   */
  @SuppressWarnings("UnstableApiUsage")
  public void findRepositoryCredentialsFromGradleProperties(ProviderFactory provider) {
    var repositoriesToConfigure = new HashMap<String, RepositoryCredentials>();
    var repositorySystemProperties = provider.systemPropertiesPrefixedBy(REPOSITORY_DEFINITION_PREFIX).get();
      collectCredentialsInto(repositoriesToConfigure, repositorySystemProperties);

      var repositoryEnvProperties = provider.environmentVariablesPrefixedBy(REPOSITORY_DEFINITION_PREFIX).get();
      collectCredentialsInto(repositoriesToConfigure, repositoryEnvProperties);

      repositoriesToConfigure.values().forEach(repo ->
        logger.quiet("- found credential: {}", repo.toString()));
      if(repositoriesToConfigure.isEmpty()) {
        logger.warn("- no credentials configured! Please visit https://github.com/EyadAbdullah/gradle-repository-manager/#configure-repository-credentials if you need help configuring them.");
      }
    repositoryCredentials = repositoriesToConfigure.values().stream().toList();
  }

    private static void collectCredentialsInto(HashMap<String, RepositoryCredentials> repositoriesToConfigure, Map<String, String> repositoryProperties) {
        repositoryProperties.forEach((propertyName, propertyValue) -> {
          if (RepositoryCredentials.isValidRepository(propertyName)) {
            var repository = new RepositoryCredentials(propertyName);
            repository = repositoriesToConfigure.getOrDefault(repository.getIdentifier(), repository);
            repository.setProperty(propertyName, propertyValue);
            repositoriesToConfigure.put(repository.getIdentifier(), repository);
          }
        });
    }

    public void addRepositories(RepositoryHandler repoHandler, List<ManageableRepository> repositories) {
    repositories.forEach(repo -> addRepository(repoHandler, repo));
  }

  public void addRepository(RepositoryHandler repoHandler, ManageableRepository repository) {
    repoHandler.maven(mavenArtifactRepository -> {
      // set repo information
      mavenArtifactRepository.setName(repository.getName());
      mavenArtifactRepository.setUrl(URI.create(repository.getUrl()));
      mavenArtifactRepository.setAllowInsecureProtocol(!repository.isSecureProtocol());

      if (repository.isSnapshotsOnly()) {
        mavenArtifactRepository.mavenContent(MavenRepositoryContentDescriptor::snapshotsOnly);
      } else if (repository.isReleasesOnly()) {
        mavenArtifactRepository.mavenContent(MavenRepositoryContentDescriptor::releasesOnly);
      }

      var repositoryCredentialList = repositoryCredentials.stream()
          .filter(it -> it.doesUrlMatch(repository.getUrl()))
          .toList();

      if (repositoryCredentialList.size() > 1) {
        logger.warn("> [Warning] You have more than one configured credential for '{}', using the first one.", repository.getUrl());
      }

      var repositoryCredential = repositoryCredentialList.stream().findFirst();
      if (repositoryCredential.isPresent()) {
        var username = repositoryCredential.get().getUsername();
        var password = repositoryCredential.get().getPassword();
        var tokenName = repositoryCredential.get().getTokenName();
        var tokenValue = repositoryCredential.get().getTokenValue();
        if (username != null && !username.isBlank() && password != null && !password.isBlank()) {
          mavenArtifactRepository.credentials(PasswordCredentials.class, action -> {
            action.setUsername(username);
            action.setPassword(password);
          });
        } else if (tokenName != null && !tokenName.isBlank() && tokenValue != null && !tokenValue.isBlank()) {
          mavenArtifactRepository.credentials(HttpHeaderCredentials.class, action -> {
            action.setName(tokenName);
            action.setValue(tokenValue);
          });
          // set an authentication type
          mavenArtifactRepository.authentication(authentications ->
              authentications.add(new DefaultHttpHeaderAuthentication("header")));
        }
      }
    });
    logger.debug("- configured repository: " + repository.getName());
  }

  public void addMavenLocalRepoIfEnabled(RepositoryManagerExtension extension, RepositoryHandler repoHandler) {
    if (extension.isMavenLocal()) {
      repoHandler.mavenLocal();
      logger.debug("- configured repository: mavenLocal");
    }
  }

  public void addMavenCentralRepoIfEnabled(RepositoryManagerExtension extension, RepositoryHandler repoHandler) {
    if (extension.isMavenCentral()) {
      repoHandler.mavenCentral();
      logger.debug("- configured repository: mavenCentral");
    }
  }

  public void addGradlePluginPortalRepoIfEnabled(RepositoryManagerExtension extension, RepositoryHandler repoHandler) {
    if (extension.isGradlePluginPortal()) {
      repoHandler.gradlePluginPortal();
      logger.debug("- configured repository: gradlePluginPortal");
    }
  }

  public void validateDependenciesIfEnabled(RepositoryManagerExtension extension, Project project) {
    if (!extension.isValidateDependencies()) {
      return;
    }
    for (var config : project.getConfigurations()) {
      if (config.getName().equals("mainSourceElements") ||
          config.getName().equals("testResultsElementsForTest")) {
        continue;
      }
      logger.quiet("- Validating dependencies: {}", config.getName());
      config.setCanBeResolved(true);
      config.getDependencies().forEach(dependency -> {
        logger.quiet("\t* resolve dependency: {}:{}:{}:{}",
            dependency.getGroup(), dependency.getName(), dependency.getVersion(), dependency.getReason());
        project.getConfigurations().detachedConfiguration(dependency).resolve();
      });
    }
  }
}
