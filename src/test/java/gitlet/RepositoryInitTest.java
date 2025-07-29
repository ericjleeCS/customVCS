package gitlet;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.*;

class RepositoryInitTest {

  @TempDir Path tmp;

  @Test
  void initCreatesExpectedLayout() throws Exception {
    Repository repo = new Repository(tmp);
    repo.init();

    Path g = tmp.resolve(".gitlet");
    assertTrue(Files.isDirectory(g), ".gitlet should exist");
    assertTrue(Files.isDirectory(g.resolve("objects")));
    assertTrue(Files.isDirectory(g.resolve("refs").resolve("heads")));
    assertTrue(Files.isRegularFile(g.resolve("HEAD")));
    assertTrue(Files.isRegularFile(g.resolve("index")));
    assertTrue(Files.isRegularFile(g.resolve("refs").resolve("heads").resolve("master")));
  }

  @Test
  void headPointsToMaster() throws Exception {
    Repository repo = new Repository(tmp);
    repo.init();

    String head = Files.readString(tmp.resolve(".gitlet").resolve("HEAD")).trim();
    assertEquals("ref: refs/heads/master", head);
  }

  @Test
  void initTwicePrintsExactMessageAndDoesNotChangeHead() throws Exception {
    Repository repo = new Repository(tmp);
    repo.init();

    // capture stdout
    var prev = System.out;
    var out = new ByteArrayOutputStream();
    System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));
    try {
      repo.init(); // second init
    } finally {
      System.setOut(prev);
    }

    String printed = out.toString(StandardCharsets.UTF_8).trim();
    assertEquals("A Gitlet version-control system already exists in the current directory.", printed);

    // HEAD unchanged
    String head = Files.readString(tmp.resolve(".gitlet").resolve("HEAD")).trim();
    assertEquals("ref: refs/heads/master", head);
  }
}
