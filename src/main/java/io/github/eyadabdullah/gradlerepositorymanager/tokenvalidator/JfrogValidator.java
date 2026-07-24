package io.github.eyadabdullah.gradlerepositorymanager.tokenvalidator;

import io.github.eyadabdullah.gradlerepositorymanager.RepositoryCredentials;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class JfrogValidator implements ValidatorInterface {

    private static final Logger logger = Logging.getLogger(JfrogValidator.class);

    @Override
    public String getValidatorID() {
        return "jfrog";
    }

    @Override
    public void validate(RepositoryCredentials repositoryCredential) {
        int status = 0;
        try {
            var uri = new URI(repositoryCredential.getUrl());

            var baseUrl = new URL(uri.getScheme(), uri.getHost(), uri.getPort(), "");
            var userUrl = new URL(baseUrl, "/artifactory/api/system/version");

            logger.debug("- Jfrog Credential Validation: {} {}", userUrl, repositoryCredential.getUsername());

            var con = (HttpURLConnection) userUrl.openConnection();
            con.setRequestMethod("GET");

            // Basic Authentication
            var auth = repositoryCredential.getUsername() + ":" + repositoryCredential.getPassword();
            var encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

            con.setRequestProperty("Authorization", "Basic " + encodedAuth);
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
