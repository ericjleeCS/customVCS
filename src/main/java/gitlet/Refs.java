package gitlet;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class Refs {
  private final Path repoRoot;

  public Refs(Path repoRoot) { this.repoRoot = repoRoot.toAbsolutePath().normalize(); }

  public boolean isHeadDetached() throws IOException {
    String s = readHeadRaw();
    return !s.startsWith("ref: ");
  }

public String currentBranchName() throws IOException {
  String s = readHeadRaw();
  if (!s.startsWith("ref: ")) return null;
  String ref = s.substring("ref: ".length()).trim();          // e.g. "refs/heads/master"
  String prefix = Constants.refsDirName + "/" + Constants.headsDirName + "/"; // "refs/heads/"
  if (!ref.startsWith(prefix)) return null;
  String name = ref.substring(prefix.length());
  return name.isEmpty() ? null : name;
}

public String resolveHeadCommitId() throws IOException {
  String s = readHeadRaw();
  if (s.startsWith("ref: ")) {
    String ref = s.substring("ref: ".length()).trim();
    Path refPath = Constants.dot(repoRoot).resolve(ref);
    if (!Files.exists(refPath)) return null;
    String v = readString(refPath).trim();
    return v.isEmpty() ? null : v;
  } else {
    s = s.trim();
    return s.isEmpty() ? null : s;
  }
}

public void pointHeadToBranch(String branch) throws IOException {
  Path head = Constants.headFile(repoRoot);
  String line = "ref: " + Constants.refsDirName + "/" + Constants.headsDirName + "/" + branch;
  Files.writeString(head, line, StandardCharsets.UTF_8,
      StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

  Path refPath = Constants.branchRef(repoRoot, branch);
  Files.createDirectories(refPath.getParent());
  if (!Files.exists(refPath)) {
    Files.writeString(refPath, "", StandardCharsets.UTF_8,
        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
  }
}
  public void detachHeadToCommit(String commitId) throws IOException {
    writeString(Constants.headFile(repoRoot), commitId);
  }

public String readRef(String refName) throws IOException {
  Path p = Constants.dot(repoRoot).resolve(refName);
  if (!Files.exists(p)) return null;
  return readString(p).trim();
}

  public void updateRef(String refName, String commitId) throws IOException {
    Path p = Constants.dot(repoRoot).resolve(refName);
    Files.createDirectories(p.getParent());
    writeString(p, commitId); // <- no newline
  }

public void updateCurrentBranch(String commitId) throws IOException {
  String s = readHeadRaw();
  if (!s.startsWith("ref: ")) throw new IllegalStateException("HEAD is detached");
  String ref = s.substring("ref: ".length()).trim(); // "refs/heads/<name>"
  Path refPath = Constants.dot(repoRoot).resolve(ref);
  Files.createDirectories(refPath.getParent());
  writeString(refPath, commitId); // no trailing newline
}
  public List<String> listBranches() throws IOException {
    Path heads = Constants.heads(repoRoot);
    List<String> out = new ArrayList<>();
    if (!Files.exists(heads)) return out;
    try (DirectoryStream<Path> ds = Files.newDirectoryStream(heads)) {
      for (Path p : ds) if (Files.isRegularFile(p)) out.add(p.getFileName().toString());
    }
    out.sort(String::compareTo);
    return out;
  }

private String readHeadRaw() throws IOException {
  Path head = Constants.headFile(repoRoot);
  if (!Files.exists(head)) return "";
  return readString(head).trim();
}


  private static String readString(Path p) throws IOException {
    return Files.readString(p, StandardCharsets.UTF_8);
  }

  private static void writeString(Path p, String s) throws IOException {
    Files.writeString(p, s, StandardCharsets.UTF_8,
        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
  }
}
