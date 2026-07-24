package io.github.eyadabdullah.gradlerepositorymanager.tokenvalidator;

import io.github.eyadabdullah.gradlerepositorymanager.RepositoryCredentials;

public class NoneValidator implements ValidatorInterface {

    @Override
    public String getValidatorID() {
        return "none";
    }

    @Override
    public void validate(RepositoryCredentials repositoryCredential) {
        // does not do anything.
    }
}
