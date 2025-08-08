package io.github.eyadabdullah.gradlerepositorymanager.extension;

import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.model.ObjectFactory;

public class RepositoryManagerExtension {

  public static final String EXTENSION_NAME = "RepositoryManager";
  public static final String REPOSITORY_DEFINITION_PREFIX = "repository_manager.repo";
  private final NamedDomainObjectContainer<ManageableRepository> manageableRepositories;
  private final Set<String> gradlePluginsToSetupForLoadingInResolutionStrategy = new HashSet<>();
  private boolean gradlePluginPortal = true;
  private boolean mavenLocal = false;
  private boolean mavenCentral = false;
  private boolean validateDependencies = false;

  @Inject
  public RepositoryManagerExtension(ObjectFactory objectFactory) {
    this.manageableRepositories = objectFactory.domainObjectContainer(ManageableRepository.class, ManageableRepositoryImpl::new);
  }

  public NamedDomainObjectContainer<ManageableRepository> getManageableRepositories() {
    return manageableRepositories;
  }

  public void repository(String url) {
    manageableRepositories.create(url, repository -> repository.setUrl(url));
  }

  public void repository(String url, Action<? super ManageableRepository> action) {
    var res = manageableRepositories.create(url, action);
    res.setUrl(url);
  }

  public void repository(String name, String url) {
    manageableRepositories.create(name, repository -> repository.setUrl(url));
  }

  public void repository(String name, String url, Action<? super ManageableRepository> action) {
    var res = manageableRepositories.create(name, action);
    res.setUrl(url);
  }

  public void setupResolutionStrategyToLoadGradlePlugin(String gradlePluginNamespace) {
    gradlePluginsToSetupForLoadingInResolutionStrategy.add(gradlePluginNamespace);
  }

  public Set<String> getGradlePluginsToSetupForLoadingInResolutionStrategy() {
    return gradlePluginsToSetupForLoadingInResolutionStrategy;
  }

  public boolean isGradlePluginPortal() {
    return gradlePluginPortal;
  }

  public void gradlePluginPortal() {
    this.gradlePluginPortal = true;
  }

  public void gradlePluginPortal(boolean gradlePluginPortal) {
    this.gradlePluginPortal = gradlePluginPortal;
  }

  public boolean isMavenLocal() {
    return mavenLocal;
  }

  public void mavenLocal() {
    this.mavenLocal = true;
  }

  public void mavenLocal(boolean mavenLocal) {
    this.mavenLocal = mavenLocal;
  }

  public boolean isMavenCentral() {
    return mavenCentral;
  }

  public void mavenCentral() {
    this.mavenCentral = true;
  }

  public void mavenCentral(boolean mavenCentral) {
    this.mavenCentral = mavenCentral;
  }

  public boolean isValidateDependencies() {
    return validateDependencies;
  }

  public void validateDependencies() {
    this.validateDependencies = true;
  }

  public void validateDependencies(boolean validateDependencies) {
    this.validateDependencies = validateDependencies;
  }
}
