package io.github.eyadabdullah.gradlerepositorymanager;

import io.github.eyadabdullah.gradlerepositorymanager.extension.RepositoryManagerExtension;
import org.gradle.api.Plugin;
import org.gradle.api.initialization.Settings;
import org.gradle.api.initialization.dsl.ScriptHandler;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import static io.github.eyadabdullah.gradlerepositorymanager.RepositoryManagerService.BANNER;
import static io.github.eyadabdullah.gradlerepositorymanager.extension.RepositoryManagerExtension.EXTENSION_NAME;

public class RepositoryManagerPlugin implements Plugin<Settings> {

  private static final Logger logger = Logging.getLogger(RepositoryManagerPlugin.class);
  private final RepositoryManagerService repositoryManagerService = new RepositoryManagerService();

  @Override
  public void apply(Settings settings) {
    logger.quiet(BANNER);
    // apply plugin extension
    settings.getExtensions().create(EXTENSION_NAME, RepositoryManagerExtension.class);
    settings.getGradle().settingsEvaluated(evaluatedSettings -> {
      var repositoryExtension = evaluatedSettings.getExtensions().getByType(RepositoryManagerExtension.class);
      var repositoriesList = repositoryExtension.getManageableRepositories().stream().toList();
      // get configured repos
      logger.quiet("\n> RepositoryManager - settings.gradle repositories : ");
      repositoriesList.forEach(repository -> {
        var repositoryLog = String.format("%s\t- %s", repository.getName(), repository.getUrl());
        logger.quiet("- found repository: " + repositoryLog);
      });

      var settingsProvider = evaluatedSettings.getProviders();
      var settingsRepoHandler = evaluatedSettings.getPluginManagement().getRepositories();

      logger.quiet("\n> RepositoryManager - local gradle.properties credentials : ");
      repositoryManagerService.findRepositoryCredentialsFromGradleProperties(settingsProvider);
      // add plugin specific repositories
      logger.quiet("\n> RepositoryManager - PluginManagement repositories : ");
      repositoryManagerService.addRepositories(settingsRepoHandler, repositoriesList);
      repositoryManagerService.addMavenLocalRepoIfEnabled(repositoryExtension, settingsRepoHandler);
      repositoryManagerService.addMavenCentralRepoIfEnabled(repositoryExtension, settingsRepoHandler);
      repositoryManagerService.addGradlePluginPortalRepoIfEnabled(repositoryExtension, settingsRepoHandler);
      // consume all specified Gradle plugins (could be prevented if the plugins published their marker)
      repositoryExtension.getGradlePluginsToSetupForLoadingInResolutionStrategy().forEach(gradlePlugin -> {
        setupResolutionStrategyToLoadGradlePlugins(evaluatedSettings, gradlePlugin);
      });
      // define project repos to download dependencies for all projects from
      evaluatedSettings.getGradle().allprojects(project -> {
        var projectRepoHandler = project.getRepositories();
        // add dependency specific repos
        logger.quiet("\n> RepositoryManager - " + project.getName() + " repositories : ");
        repositoryManagerService.addRepositories(projectRepoHandler, repositoriesList);
        repositoryManagerService.addMavenLocalRepoIfEnabled(repositoryExtension, projectRepoHandler);
        repositoryManagerService.addMavenCentralRepoIfEnabled(repositoryExtension, projectRepoHandler);
        repositoryManagerService.addGradlePluginPortalRepoIfEnabled(repositoryExtension, projectRepoHandler);
      });
      // log configured classpath for more details
      evaluatedSettings.getGradle().afterProject(project -> {
        var classpath = project.getBuildscript().getConfigurations().getByName(ScriptHandler.CLASSPATH_CONFIGURATION);
        classpath.getAllDependencies().forEach(dep ->
            logger.quiet("- found classpath: {}:{}:{}:{}",
                dep.getGroup(), dep.getName(), dep.getVersion(), dep.getReason()));
        // try to resolve all dependencies to validate configured repositories
        repositoryManagerService.validateDependenciesIfEnabled(repositoryExtension, project);
      });
    });
  }

  private void setupResolutionStrategyToLoadGradlePlugins(Settings target, String gradlePluginNamespace) {
    target.getPluginManagement().getResolutionStrategy().eachPlugin(plugin -> {
      var pluginModule = String.format("%s:%s:%s",
          plugin.getRequested().getId().getNamespace(),
          plugin.getRequested().getId().getName(),
          plugin.getRequested().getVersion());
      logger.quiet("- found gradle plugin: " + pluginModule);
      if (gradlePluginNamespace.equals(plugin.getRequested().getId().getNamespace())) {
        plugin.useModule(pluginModule);
      }
    });
  }
}
