package gitlet;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ObjectStoreTest {
  @TempDir Path tmp;

  private Path repoRoot() throws IOException {
    Path repo = tmp.resolve("repo");
    Files.createDirectories(Constants.objects(repo));
    return repo.toAbsolutePath().normalize();
  }

  @Test
  void writeReadBlob() throws Exception {
    Path repo = repoRoot();
    ObjectStore store = new ObjectStore(repo);
    String id = store.writeBlob("hello".getBytes());
    byte[] back = store.readBlob(id);
    assertEquals("hello", new String(back));
  }

  @Test
  void writeReadCommit() throws Exception {
    Path repo = repoRoot();
    ObjectStore store = new ObjectStore(repo);

    Map<String,String> tracked = new TreeMap<>();
    tracked.put("a", "A");
    Commit c = Commit.create("m", List.of(), tracked, 42L);

    String id = store.writeCommit(c);
    assertEquals(c.getId(), id);

    Commit d = store.readCommit(id);
    assertEquals(c.getId(), d.getId());
    assertEquals(c.getTrackedFiles(), d.getTrackedFiles());
  }
}
