package io.github.eyadabdullah.gradlerepositorymanager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RepositoryCredentialsTest {

    @ParameterizedTest
    @ValueSource(strings = {"url", "token_name", "token_value", "username", "password"})
    void init_correctIdentifier_alphaNumericName(String elementType) {
        // arrange
        var element = "repository_manager_repo_myname123_%s".formatted(elementType);

        // act
        var repositoryCredentials = new RepositoryCredentials(element);

        // assert
        assertThat(repositoryCredentials.getIdentifier()).isEqualTo("myname123");
    }

    @Test
    void init_correctIdentifier_nameWithUnderscore() {
        // arrange
        var element = "repository_manager_repo_my_name123_url";

        // act
        var repositoryCredentials = new RepositoryCredentials(element);

        // assert
        assertThat(repositoryCredentials.getIdentifier()).isEqualTo("my_name123");
    }

    @Test
    void init_exception_invalidName() {
        // arrange
        var element = "repository_manager_repo_my_Name123_url";

        // act
        var error = assertThatThrownBy(() -> new RepositoryCredentials(element));

        // assert
        error.isInstanceOf(IllegalArgumentException.class)
            .hasMessageStartingWith("Invalid repository property: '" + element + "'");
    }

    @Test
    void setProperty_correctTokenName_tokenNameProvided() {
        // arrange
        var element = "repository_manager_repo_myname123_token_name";
        var repositoryCredentials = new RepositoryCredentials(element);

        // act
        repositoryCredentials.setProperty(element, "foo");

        // assert
        assertThat(repositoryCredentials.getTokenName()).isEqualTo("foo");
    }

    @Test
    void setProperty_correctTokenName_tokenValueProvided() {
        // arrange
        var element = "repository_manager_repo_myname123_token_value";
        var repositoryCredentials = new RepositoryCredentials(element);

        // act
        repositoryCredentials.setProperty(element, "foo");

        // assert
        assertThat(repositoryCredentials.getTokenValue()).isEqualTo("foo");
    }

    @Test
    void setProperty_correctTokenName_usernameProvided() {
        // arrange
        var element = "repository_manager_repo_myname123_username";
        var repositoryCredentials = new RepositoryCredentials(element);

        // act
        repositoryCredentials.setProperty(element, "foo");

        // assert
        assertThat(repositoryCredentials.getUsername()).isEqualTo("foo");
    }

    @Test
    void setProperty_correctTokenName_passwordProvided() {
        // arrange
        var element = "repository_manager_repo_myname123_password";
        var repositoryCredentials = new RepositoryCredentials(element);

        // act
        repositoryCredentials.setProperty(element, "foo");

        // assert
        assertThat(repositoryCredentials.getPassword()).isEqualTo("foo");
    }

    @Test
    void setProperty_correctTokenName_urlProvided() {
        // arrange
        var element = "repository_manager_repo_myname123_url";
        var repositoryCredentials = new RepositoryCredentials(element);

        // act
        repositoryCredentials.setProperty(element, "foo");

        // assert
        assertThat(repositoryCredentials.getUrl()).isEqualTo("foo");
    }

    @Test
    void isValidRepository_true_validDefinitionProvided() {
        // arrange
        var element = "repository_manager_repo_myname123_url";

        // act
        var result = RepositoryCredentials.isValidRepository(element);

        // assert
        assertThat(result).isTrue();
    }

    @Test
    void isValidRepository_false_invalidDefinitionProvided() {
        // arrange
        var element = "repository_manager_repo_myName123_url";

        // act
        var result = RepositoryCredentials.isValidRepository(element);

        // assert
        assertThat(result).isFalse();
    }

}
