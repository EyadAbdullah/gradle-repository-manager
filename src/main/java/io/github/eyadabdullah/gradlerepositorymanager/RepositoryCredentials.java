package io.github.eyadabdullah.gradlerepositorymanager;

import java.util.regex.Pattern;

import static io.github.eyadabdullah.gradlerepositorymanager.extension.RepositoryManagerExtension.SPLIT_ELEMENT;
import static io.github.eyadabdullah.gradlerepositorymanager.extension.RepositoryManagerExtension.REPOSITORY_DEFINITION_PREFIX;

public class RepositoryCredentials {

  private static final String REGEX_STRING =
      "^" + REPOSITORY_DEFINITION_PREFIX + SPLIT_ELEMENT + "(?<identifier>[a-z_\\d]+)" + SPLIT_ELEMENT
          + "(url|token_name|token_value|username|password)$";
  private static final Pattern REPOSITORY_PROPERTY_REGEX = Pattern.compile(REGEX_STRING);
  private static final String PROP_URL = SPLIT_ELEMENT + "url";
  private static final String PROP_KEY_NAME = SPLIT_ELEMENT + "token_name";
  private static final String PROP_KEY_VALUE = SPLIT_ELEMENT + "token_value";
  private static final String PROP_USERNAME = SPLIT_ELEMENT + "username";
  private static final String PROP_PASSWORD = SPLIT_ELEMENT + "password";

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
      var matcher = REPOSITORY_PROPERTY_REGEX.matcher(property);
      if (matcher.matches()) {
        this.identifier = matcher.group("identifier");
      } else {
        throw new IllegalArgumentException("Invalid repository property: '%s' (Expecting to match: '%s')".formatted(property, REPOSITORY_PROPERTY_REGEX));
      }
  }

  public void setProperty(String name, String value) {
    var repoPrefix = REPOSITORY_DEFINITION_PREFIX + SPLIT_ELEMENT + identifier;
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

  public Boolean doesUrlMatch(String url) {
    return Pattern.compile(this.url).matcher(url).matches();
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
