package io.github.eyadabdullah.gradlerepositorymanager.tokenvalidator;

import io.github.eyadabdullah.gradlerepositorymanager.RepositoryCredentials;

public interface ValidatorInterface {

    public String getValidatorID();
    public void validate(RepositoryCredentials repositoryCredential);
}