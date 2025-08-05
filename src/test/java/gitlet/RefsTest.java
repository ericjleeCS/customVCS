package gitlet;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.*;

public class RefsTest {
  @TempDir Path tmp;

  private Path repoRoot() throws IOException {
    Path repo = tmp.resolve("repo");
    Files.createDirectories(repo);
    Files.createDirectories(Constants.dot(repo));
    Files.createDirectories(Constants.heads(repo));
    return repo.toAbsolutePath().normalize();
  }

  @Test
  void currentBranchNameAndHeadModes() throws Exception {
    Path repo = repoRoot();
    Refs refs = new Refs(repo);

    refs.pointHeadToBranch(Constants.defaultBranch);
    assertFalse(refs.isHeadDetached());

    assertEquals(Constants.defaultBranch, refs.currentBranchName());

    assertNull(refs.resolveHeadCommitId());
    refs.updateCurrentBranch("abc123");
    assertEquals("abc123", refs.resolveHeadCommitId());

    refs.detachHeadToCommit("deadbeef");
    assertTrue(refs.isHeadDetached());
    assertNull(refs.currentBranchName());
    assertEquals("deadbeef", refs.resolveHeadCommitId());
  }

  @Test
  void readWriteRefs() throws Exception {
    Path repo = repoRoot();
    Refs refs = new Refs(repo);

    refs.pointHeadToBranch("dev");
    refs.updateRef(Constants.refsDirName + "/" + Constants.headsDirName + "/dev", "c0");
    assertEquals("c0", refs.readRef(Constants.refsDirName + "/" + Constants.headsDirName + "/dev"));
  }
}
