package io.github.eyadabdullah.gradlerepositorymanager.tokenvalidator;

import io.github.eyadabdullah.gradlerepositorymanager.RepositoryCredentials;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.IOException;
import java.net.*;

public class GitlabValidator implements ValidatorInterface {

    private static final Logger logger = Logging.getLogger(GitlabValidator.class);

    @Override
    public String getValidatorID() {
        return "gitlab";
    }

    @Override
    public void validate(RepositoryCredentials repositoryCredential) {
        int status = 0;
        try {
            var uri = new URI(repositoryCredential.getUrl());

            var baseUrl = new URL(uri.getScheme(), uri.getHost(), uri.getPort(), "");
            var userUrl = new URL(baseUrl, "/api/v4/projects");

            logger.debug("- Gitlab Token Validation: {} {}", userUrl, repositoryCredential.getTokenName());

            var con = (HttpURLConnection) userUrl.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty(
                    repositoryCredential.getTokenName(),
                    repositoryCredential.getTokenValue());

            status = con.getResponseCode();
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }

        if (status == 200) {
            logger.debug("- Gitlab Token Validation: Token is valid for {}", repositoryCredential.getUrl());
        } else {
            logger.error("- Gitlab Token Validation: Token is invalid! (HTTP {})", status);
            throw new RuntimeException(String.format("Token for %s is invalid!", repositoryCredential.getUrl()));
        }
    }
}
