package io.github.eyadabdullah.gradlerepositorymanager.tokenvalidator;

import io.github.eyadabdullah.gradlerepositorymanager.RepositoryCredentials;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.github.eyadabdullah.gradlerepositorymanager.RepositoryManagerService.REPOSITORY_DOCUMENTATION_URL;

public class ValidationService {

    private static final Logger logger = Logging.getLogger(ValidationService.class);

    List<ValidatorInterface> validators = List.of(
            new GitlabValidator(),
            new JfrogValidator(),
            new NoneValidator()
    );

    String availableValidators = validators.stream()
            .map(ValidatorInterface::getValidatorID)
            .collect(Collectors.joining(", "));

    public void validateAll(List<RepositoryCredentials> repositoryCredentials) {
        logger.debug("> RepositoryManager - Validate credentials");
        repositoryCredentials.forEach(repositoryCredential -> {
            validate(repositoryCredential);
        });
    }

    public void validate(RepositoryCredentials credential) {
        var currentValidator = credential.getValidator();
        logger.debug("- Validation: validate credentials for {} with '{}'", credential.getUrl(), currentValidator);

        var selectedValidator = getValidator(currentValidator);
        if(selectedValidator.isEmpty()) {
            var message = String.format("- Validation: %s does not have a validator set!\n", credential.getUrl());
            message += String.format("              Available validators: %s\n", availableValidators);
            message += "              Check the documentation for more details\n";
            message += String.format("              Documentation %s#Validators", REPOSITORY_DOCUMENTATION_URL);
            logger.warn(message);
            return;
        }
        selectedValidator.get()
                .validate(credential);
    }

    private Optional<ValidatorInterface> getValidator(String validatorID) {
        return validators.stream()
                .filter(it -> it.getValidatorID().equals(validatorID))
                .findFirst();
    }
}
