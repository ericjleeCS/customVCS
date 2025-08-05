package gitlet;

import java.nio.file.Path;

public final class Constants {
  private Constants() {}

  public static final String dotDirName = ".gitlet";
  public static final String objectsDirName = "objects";
  public static final String refsDirName = "refs";
  public static final String headsDirName = "heads";
  public static final String headFileName = "HEAD";
  public static final String indexFileName = "index";
  public static final String defaultBranch = "master";

  public static Path dot(Path repoRoot) { 
    return repoRoot.resolve(dotDirName); 
}
  public static Path objects(Path repoRoot) { 
    return dot(repoRoot).resolve(objectsDirName);
}
  public static Path refs(Path repoRoot) { 
    return dot(repoRoot).resolve(refsDirName); 
}
  public static Path heads(Path repoRoot) { 
    return refs(repoRoot).resolve(headsDirName); 
}
  public static Path headFile(Path repoRoot) { 
    return dot(repoRoot).resolve(headFileName); 
}
  public static Path indexFile(Path repoRoot) { 
    return dot(repoRoot).resolve(indexFileName); 
}
  public static Path branchRef(Path repoRoot, String branch) { 
    return heads(repoRoot).resolve(branch); 
}
}
