package gitlet;

import java.io.IOException;
import java.nio.file.*;

public class Repository {
  private final Path workTree;
  private final Path repoDir;

  public Repository() { 
    this(Paths.get(".")); 
  }
  public Repository(Path workTree) {
    this.workTree = workTree;
    this.repoDir  = workTree.resolve(".gitlet");
  }

  public void init() throws IOException {
    if (Files.exists(repoDir)) {
      System.out.println("A Gitlet version-control system already exists in the current directory.");
      return;
    }
    Files.createDirectories(repoDir.resolve("objects"));
    Files.createDirectories(repoDir.resolve("refs").resolve("heads"));

    Files.writeString(repoDir.resolve("HEAD"),  "ref: refs/heads/master\n", StandardOpenOption.CREATE_NEW);
    Files.writeString(repoDir.resolve("index"), "", StandardOpenOption.CREATE_NEW);


    String initialId = writeInitialCommit(); // implement later
    Files.writeString(repoDir.resolve("refs/heads/master"), initialId + "\n", StandardOpenOption.CREATE_NEW);
  }

  // Public API (stubs for later)
  public void add(String path) throws IOException {
    Files.createDirectories(repoDir.resolve(path));
  }
  
  public void commit(String message) throws IOException {}
  public void remove(String path) throws IOException {}
  public void status() throws IOException {}
  public void checkoutFile(String path) throws IOException {}
  public void checkoutCommitFile(String commitId, String path) throws IOException {}
  public void checkoutBranch(String branch) throws IOException {}
  public void branch(String name) throws IOException {}
  public void rmBranch(String name) throws IOException {}
  public void reset(String commitId) throws IOException {}
  public void merge(String branch) throws IOException {}
  public void log() throws IOException {}
  public void globalLog() throws IOException {}
  public void find(String message) throws IOException {}
  public String resolveAbbrev(String prefix) throws IOException { return null; }

  // Private helpers (signatures only for now)
  private String writeInitialCommit() throws IOException { return null; } // returns the new commit’s id
}
