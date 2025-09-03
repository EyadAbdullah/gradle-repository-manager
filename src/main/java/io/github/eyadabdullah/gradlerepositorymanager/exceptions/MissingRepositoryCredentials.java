package io.github.eyadabdullah.gradlerepositorymanager.exceptions;

public class MissingRepositoryCredentials extends RuntimeException {

    public MissingRepositoryCredentials(String message) {
        super(message);
    }
}
