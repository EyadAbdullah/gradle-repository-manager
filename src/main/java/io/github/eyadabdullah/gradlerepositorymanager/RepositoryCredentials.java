package io.github.eyadabdullah.gradlerepositorymanager;

import java.util.regex.Pattern;

import static io.github.eyadabdullah.gradlerepositorymanager.extension.RepositoryManagerExtension.REPOSITORY_DEFINITION_PREFIX;

public class RepositoryCredentials {

  private static final Pattern REPOSITORY_PROPERTY_REGEX = Pattern.compile("^" + REPOSITORY_DEFINITION_PREFIX + ".[a-z_\\d]+.(url|token_name|token_value|username|password)$");
  private static final String PROP_URL = ".url";
  private static final String PROP_KEY_NAME = ".token_name";
  private static final String PROP_KEY_VALUE = ".token_value";
  private static final String PROP_USERNAME = ".username";
  private static final String PROP_PASSWORD = ".password";

  private final String identifier;
  private String url;
  private String tokenName;
  private String tokenValue;
  private String username;
  private String password;

  public static boolean isValidRepository(String property) {
    return REPOSITORY_PROPERTY_REGEX.matcher(property).matches();
  }

  RepositoryCredentials(String property) {
    this.identifier = property.split("\\.")[2];
  }

  public void setProperty(String name, String value) {
    var repoPrefix = REPOSITORY_DEFINITION_PREFIX + "." + identifier;
    if (!name.startsWith(repoPrefix)) {
      return;
    }
    if (name.endsWith(PROP_URL)) {
      this.url = value;
    } else if (name.endsWith(PROP_KEY_NAME)) {
      this.tokenName = value;
    } else if (name.endsWith(PROP_KEY_VALUE)) {
      this.tokenValue = value;
    } else if (name.endsWith(PROP_USERNAME)) {
      this.username = value;
    } else if (name.endsWith(PROP_PASSWORD)) {
      this.password = value;
    }
  }

  public String getIdentifier() {
    return identifier;
  }

  public String getUrl() {
    return url;
  }

  public String getTokenName() {
    return tokenName;
  }

  public String getTokenValue() {
    return tokenValue;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public void setTokenName(String tokenName) {
    this.tokenName = tokenName;
  }

  public void setTokenValue(String tokenValue) {
    this.tokenValue = tokenValue;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  @Override
  public String toString() {
    return "RepositoryCredentials{" +
        "identifier='" + identifier + '\'' +
        ", url='" + url + '\'' +
        ", tokenName='" + tokenName + '\'' +
        ", username='" + username + '\'' +
        '}';
  }
}
