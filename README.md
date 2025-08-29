# GradleRepositoryManager

This Gradle Repository Manager plugin allows you to configure public and private repositories for
your project dependencies and plugin dependencies, and also supports Java root projects and submodules.

[![GitHub](https://img.shields.io/github/license/EyadAbdullah/gradle-repository-manager)](https://github.com/EyadAbdullah/gradle-repository-manager/blob/main/LICENSE)
[![Publish Plugin](https://github.com/EyadAbdullah/gradle-repository-manager/actions/workflows/publish-plugin.yml/badge.svg)](https://github.com/EyadAbdullah/gradle-repository-manager/actions/workflows/publish-plugin.yml)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/io.github.eyadabdullah.gradle-repository-manager.svg)](https://plugins.gradle.org/plugin/io.github.eyadabdullah.gradle-repository-manager)

## Usage

Add the following plugin configuration at the top of your `settings.gradle.kts` file:

```groovy
plugins {
  id("io.github.eyadabdullah.gradle-repository-manager") version("2.0.0")
}
```

> Note: You can remove any repository definitions in pluginManagement and dependencyResolutionManagement.
> <br/>We are going to configure these via RepositoryManager extension in order to make them available to your project again.

Easy! That's it. You can now [configure your repositories](#how-to-configure-gradle-repositories)

## How to configure gradle repositories

To configure your project repositories correctly, you need to:
1. [Configure repositories via plugin in your `settings.gradle`](#configure-repositories-via-plugin)
2. [Configure repository credentials in your global `gradle.properties`](#configure-repository-credentials-in-global-gradleproperties)

### Configure repositories via plugin

Plugin configurations can be defined in your `settings.gradle.kts` file.
<br/>Here is a recommended sample configuration for your projects:

```groovy
RepositoryManager {

    repository("https://source.example.com/api/v4/groups/1/-/packages/maven") {
        name = "my private repository"
    }
    repository("https://source.example.com/api/v4/groups/2/-/packages/maven") {
        name = "my another private repository"
    }
    repository("http://localhost:8081/artifactory/release-local") {
        name = "my internal private repository - release only"
        secureProtocol = false
        releasesOnly = true
    }
    repository("http://localhost:8081/artifactory/snapshot-local") {
        name = "my internal private repository - snapshot only"
        secureProtocol = false
        snapshotsOnly = true
    }
    
    // or simplified
    // repository("my private repository", "https://source.example.com/api/v4/groups/1/-/packages/maven")
    // repository("my another private repository", "https://source.example.com/api/v4/groups/2/-/packages/maven")

    mavenCentral()
}
```

### Configure repository credentials

To configure your repository credentials, you first need to either create or locate your global Gradle properties file.
Alternatively, you can use environment-variables.


#### Using the global gradle.properties:

This can be found in:
- Linux,Mac,Unices under: `$HOME/.gradle/gradle.properties`
- Windows: `%userprofile%\.gradle\gradle.properties`

Here is a recommended sample configuration:
```properties
systemProp.repository_manager_repo_0_url=https://source.example.com/api/v4/groups/1/-/packages/maven
systemProp.repository_manager_repo_0_token_name=Private-Token
systemProp.repository_manager_repo_0_token_value=YOUR PERSONAL GITLAB TOKEN HERE

systemProp.repository_manager_repo_1_url=https://source.example.com/api/v4/groups/2/-/packages/maven
systemProp.repository_manager_repo_1_token_name=Private-Token
systemProp.repository_manager_repo_1_token_value=YOUR PERSONAL GITLAB TOKEN HERE

# gradle-plugins-local on Artifactory
systemProp.repository_manager_repo_3_url=http://localhost:8081/artifactory/release-local
systemProp.repository_manager_repo_3_username=USERNAME
systemProp.repository_manager_repo_3_password=PASSWORD

# libs-release-local on Artifactory
systemProp.repository_manager_repo_4_url=http://localhost:8081/artifactory/snapshot-local
systemProp.repository_manager_repo_4_username=USERNAME
systemProp.repository_manager_repo_4_password=PASSWORD


# You can also use wildcard matching.
# The following would replace repo 3&4:
systemProp.repository_manager_repo_5_url=http://localhost:8081/*
systemProp.repository_manager_repo_5_username=USERNAME
systemProp.repository_manager_repo_5_password=PASSWORD

```

#### Using environment variables:

You can just specify the values as an environment variable:

`repository_manager_repo_5_url=http://localhost:8081/*`

This follows the same schema as the `gradle.properties`.



## Logging
If you set logging to `debug`, eg. via gradle.properties:

`org.gradle.logging.level=debug`

the repository manager will give you more detailed information about configured projects and repositories.

## How to test the plugin locally

1. clone this project to your machine
2. run the gradle task `publishToMavenLocal`
3. configure the following in your `settings.gradle` file
   ```groovy
   pluginManagement {
      repositories {
          mavenLocal()
      }
    }
    
    plugins {
      id("io.github.eyadabdullah.gradle-repository-manager") version("1.0.0")
    }
    ```
4. [configure your gradle repositories](#how-to-configure-gradle-repositories).
5. refresh dependencies and reload your project.

Now you should be ready to import dependencies from the configured repositories.

## All configurable properties

```groovy
RepositoryManager {

    // This needs to be set up if you are going to define a plugin dependency in your project that comes from one of the configured repositories. This should only be configured if you have published your Gradle plugin without the marker
    setupResolutionStrategyToLoadGradlePlugin("com.example.gradle.plugin")

    repository {
        // a unique name for your repo (required)
        name = "my private repository"
        // repository url (required)
        url = "https://source.example.com/api/v4/groups/1/-/packages/maven"
        // if in any case your accessing insecure repo. 'http', you need then to set this to false. default is true
        secureProtocol = false
        // sets if the repo should only retrieve dependencies with snapshot versions. default false
        snapshotsOnly = false
        // sets if the repo should only retrieve dependencies with snapshot versions. default false
        releasesOnly = false
    }
    // makes local maven repository available. default inactive
    mavenLocal()
    // makes central maven repository available. default inactive
    mavenCentral()
    // makes gradle plugin portal repository available. default inactive
    gradlePluginPortal()
    // allows your project to validate dependencies and throws an error if a single dependency could not be resolved.
    // for example if the dependency does not exist in the configured repositories
    // default inactive
    validateDependencies()
}
```
## Contributors

Merge Requests are welcome.

When you add a feature, please add tests accordingly.

This project uses semantic versioning.