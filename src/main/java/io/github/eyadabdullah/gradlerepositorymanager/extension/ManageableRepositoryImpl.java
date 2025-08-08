package io.github.eyadabdullah.gradlerepositorymanager.extension;

public class ManageableRepositoryImpl implements ManageableRepository {

    private String name;
    private String url;
    private boolean secureProtocol = true;
    private boolean snapshotsOnly = false;
    private boolean releasesOnly = false;

    public ManageableRepositoryImpl(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String repoName) {
        this.name = repoName;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean isSecureProtocol() {
        return secureProtocol;
    }

    @Override
    public void setSecureProtocol(boolean secureProtocol) {
        this.secureProtocol = secureProtocol;
    }

    @Override
    public boolean isSnapshotsOnly() {
        return snapshotsOnly;
    }

    @Override
    public void setSnapshotsOnly(boolean snapshotsOnly) {
        this.snapshotsOnly = snapshotsOnly;
    }

    @Override
    public boolean isReleasesOnly() {
        return releasesOnly;
    }

    @Override
    public void setReleasesOnly(boolean releasesOnly) {
        this.releasesOnly = releasesOnly;
    }


}
