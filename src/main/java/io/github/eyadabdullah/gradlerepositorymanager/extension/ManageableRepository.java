package io.github.eyadabdullah.gradlerepositorymanager.extension;

import org.gradle.api.Named;

public interface ManageableRepository extends Named {

  String getUrl();

  void setUrl(String url);

  boolean isSecureProtocol();

  void setSecureProtocol(boolean secureProtocol);

  boolean isSnapshotsOnly();

  void setSnapshotsOnly(boolean snapshotsOnly);

  boolean isReleasesOnly();

  void setReleasesOnly(boolean releasesOnly);

}
