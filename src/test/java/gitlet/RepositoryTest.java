package gitlet;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class RepositoryTest {
  @TempDir Path tmp;

  private Path repoRoot() { return tmp.resolve("repo").toAbsolutePath().normalize(); }

  private String captureStdout(ThrowingRunnable r) throws Exception {
    PrintStream prev = System.out;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos, true, StandardCharsets.UTF_8));
    try { r.run(); } finally { System.setOut(prev); }
    return baos.toString(StandardCharsets.UTF_8);
  }

  @FunctionalInterface
  interface ThrowingRunnable { void run() throws Exception; }

  private void writeFile(Path root, String rel, String content) throws IOException {
    Path p = root.resolve(rel);
    Files.createDirectories(p.getParent() == null ? root : p.getParent());
    Files.writeString(p, content, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
  }

  @Test
  void initCreatesRepoStructureAndHead() throws Exception {
    Path root = repoRoot();
    Files.createDirectories(root);
    Repository repo = new Repository(root);
    String out = captureStdout(repo::init);
    assertTrue(Files.exists(Constants.dot(root)));
    assertTrue(Files.exists(Constants.objects(root)));
    assertTrue(Files.exists(Constants.headFile(root)));
    assertTrue(Files.exists(Constants.indexFile(root)));
    assertTrue(out.contains("Initialized empty repository"));
  }

  @Test
  void addCommitFlowTracksFile() throws Exception {
    Path root = repoRoot();
    Files.createDirectories(root);
    Repository repo = new Repository(root);
    repo.init();

    writeFile(root, "a.txt", "one");
    repo.add("a.txt");
    repo.commit("first");

    Refs refs = new Refs(root);
    String head = refs.resolveHeadCommitId();
    assertNotNull(head);

    ObjectStore store = new ObjectStore(root);
    Commit c = store.readCommit(head);
    assertEquals("first", c.getMessage());
    assertEquals(Map.of("a.txt", Hashing.sha1("one".getBytes())), c.getTrackedFiles());
  }

  @Test
  void removeBehavior() throws Exception {
    Path root = repoRoot();
    Files.createDirectories(root);
    Repository repo = new Repository(root);
    repo.init();

    writeFile(root, "b.txt", "x");
    repo.add("b.txt");
    repo.remove("b.txt"); // unstages addition
    String idxContent = Files.readString(Constants.indexFile(root), StandardCharsets.UTF_8);
    assertEquals("", idxContent);

    repo.add("b.txt");
    repo.commit("add b");
    repo.remove("b.txt"); // stages removal
    String staged = Files.readString(Constants.indexFile(root), StandardCharsets.UTF_8);
    assertTrue(staged.contains("R\tb.txt"));
  }

  @Test
  void commitWithNoChangesPrintsMessage() throws Exception {
    Path root = repoRoot();
    Files.createDirectories(root);
    Repository repo = new Repository(root);
    repo.init();
    String out = captureStdout(() -> repo.commit("no-op"));
    assertTrue(out.contains("No changes added to commit."));
  }
}
