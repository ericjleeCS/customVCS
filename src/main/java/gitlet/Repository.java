package gitlet;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class Repository {
  private final Path repoRoot;
  private final ObjectStore objects;
  private final StagingArea index;
  private final Refs refs;

  public Repository(Path repoRoot) throws IOException {
    this.repoRoot = repoRoot.toAbsolutePath().normalize();
    this.objects = new ObjectStore(this.repoRoot);
    this.index = new StagingArea(this.repoRoot);
    this.refs = new Refs(this.repoRoot);
    Path indexFile = Constants.indexFile(repoRoot);
    if (Files.exists(indexFile)) this.index.load(indexFile);
  }

public void init() throws IOException {
  Path dot  = Constants.dot(repoRoot);
  Path head = Constants.headFile(repoRoot);

  if (Files.exists(head)) {
    System.out.println("A Gitlet version-control system already exists in the current directory.");
    return;
  }

  Files.createDirectories(dot);
  Files.createDirectories(Constants.objects(repoRoot));
  Files.createDirectories(Constants.refs(repoRoot));
  Files.createDirectories(Constants.heads(repoRoot));

  Path masterRef = Constants.branchRef(repoRoot, Constants.defaultBranch);
  Files.createDirectories(masterRef.getParent());
  if (!Files.exists(masterRef)) {
    Files.writeString(masterRef, "", StandardCharsets.UTF_8,
        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
  }

  refs.pointHeadToBranch(Constants.defaultBranch);

  Files.writeString(Constants.indexFile(repoRoot), "", StandardCharsets.UTF_8,
      StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

  System.out.println("Initialized empty repository in " + dot.toAbsolutePath());
}



  public void add(String path) throws IOException {
    Path abs = repoRoot.resolve(path).normalize();
    if (!abs.startsWith(repoRoot) || !Files.isRegularFile(abs)) throw new IOException("File not found: " + path);
    Blob blob = Blob.fromFile(abs);
    String blobId = objects.writeBlob(blob.getContent());
    Map<String,String> headTracked = loadHeadTracked();
    String normPath = repoRoot.relativize(abs).toString().replace('\\', '/');

    if (blobId.equals(headTracked.get(normPath))) {
      index.unstageRemoval(normPath);
      index.unstageAddition(normPath);
    } else {
      index.stageForAddition(normPath, blobId);
    }
    saveIndex();
  }

  public void remove(String path) throws IOException {
    String normPath = indexPathNormalize(path);
    Map<String,String> headTracked = loadHeadTracked();

    if (index.isStagedForAddition(normPath)) {
      index.unstageAddition(normPath);
      saveIndex();
      return;
    }
    if (headTracked.containsKey(normPath)) {
      index.stageForRemoval(normPath);
      saveIndex();
      return;
    }
    System.out.println("No reason to remove the file.");
  }

 public void commit(String message) throws IOException {
  if (message == null || message.isBlank()) throw new IllegalArgumentException("Please enter a commit message.");
  if (index.isEmpty()) { System.out.println("No changes added to commit."); return; }

  String parentId = refs.resolveHeadCommitId();
  Map<String,String> headTracked = loadHeadTracked();

  index.pruneAgainst(headTracked);
  if (index.isEmpty()) { 
    System.out.println("No changes added to commit."); 
    return; 
  }

  Map<String,String> newTracked = index.applyTo(headTracked);
  List<String> parents = parentId == null ? List.of() : List.of(parentId);

  Commit c = Commit.create(message, parents, newTracked, System.currentTimeMillis());
  String id = objects.writeCommit(c);
  if (!id.equals(c.getId())) throw new IllegalStateException("commit id mismatch");

  refs.updateCurrentBranch(id);
  index.clear();
  saveIndex();
  System.out.println(id);
}

  public void status() throws IOException {
    System.out.println("=== Branches ===");
    String cur = refs.currentBranchName();
    for (String b : refs.listBranches()) {
      System.out.println(b.equals(cur) ? "*" + b : b);
    }
    System.out.println();

    System.out.println("=== Staged Files ===");
    index.getAdditions().keySet().forEach(System.out::println);
    System.out.println();

    System.out.println("=== Removed Files ===");
    index.getRemovals().forEach(System.out::println);
    System.out.println();
  }


  private Map<String,String> loadHeadTracked() throws IOException {
    String headId = refs.resolveHeadCommitId();
    if (headId == null) return Collections.emptyMap();
    Commit head = objects.readCommit(headId);
    return head.getTrackedFiles();
  }

  private String indexPathNormalize(String path) {
    Path abs = repoRoot.resolve(path).normalize();
    if (!abs.startsWith(repoRoot)) throw new IllegalArgumentException("Path escapes repo");
    return repoRoot.relativize(abs).toString().replace('\\', '/');
  }

  private void saveIndex() throws IOException {
    index.save(Constants.indexFile(repoRoot));
  }

  public void checkoutFile(String path) throws IOException { throw new UnsupportedOperationException(); }
  public void checkoutCommitFile(String commitId, String path) throws IOException { throw new UnsupportedOperationException(); }
  public void checkoutBranch(String branch) throws IOException { throw new UnsupportedOperationException(); }
  public void branch(String name) throws IOException { throw new UnsupportedOperationException(); }
  public void rmBranch(String name) throws IOException { throw new UnsupportedOperationException(); }
  public void reset(String commitId) throws IOException { throw new UnsupportedOperationException(); }
  public void merge(String branch) throws IOException { throw new UnsupportedOperationException(); }
  public void log() throws IOException { throw new UnsupportedOperationException(); }
  public void globalLog() throws IOException { throw new UnsupportedOperationException(); }
  public void find(String message) throws IOException { throw new UnsupportedOperationException(); }
  public String resolveAbbrev(String prefix) throws IOException { throw new UnsupportedOperationException(); }
}
