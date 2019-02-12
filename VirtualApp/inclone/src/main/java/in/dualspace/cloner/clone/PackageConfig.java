package in.dualspace.cloner.clone;

public class PackageConfig {
    public String packageName;
    public int allowedCloneCount;

    public PackageConfig() {
        packageName = "";
        allowedCloneCount = 0;
    }

    public PackageConfig(String packageName, int allowedCloneCount) {
        this.packageName = packageName;
        this.allowedCloneCount = allowedCloneCount;
    }
}
