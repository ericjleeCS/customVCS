package gitlet;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

public class StagingAreaTest {

  @TempDir Path tmp;

  private Path repoRoot() throws IOException {
    Path repo = tmp.resolve("repo");
    Files.createDirectories(repo);
    return repo.toAbsolutePath().normalize();
  }

  private Path indexPath() { return tmp.resolve("index"); }

  @Test
  void saveLoadRoundTripPreservesContent() throws Exception {
    Path repo = repoRoot();
    Path idx = indexPath();

    StagingArea s1 = new StagingArea(repo);
    s1.stageForAddition("src/A.txt", "blobA");
    s1.stageForAddition("B.txt", "blobB");
    s1.stageForRemoval("C.txt");
    s1.stageForRemoval("dir/D.txt");
    s1.save(idx);

    StagingArea s2 = new StagingArea(repo);
    s2.load(idx);

    assertEquals(s1.getAdditions(), s2.getAdditions());
    assertEquals(s1.getRemovals(), s2.getRemovals());
    assertFalse(s2.isEmpty());
  }

  @Test
  void addRemoveInterplay() throws Exception {
    StagingArea s = new StagingArea(repoRoot());

    s.stageForAddition("a.txt", "id1");
    assertTrue(s.isStagedForAddition("a.txt"));
    assertFalse(s.isStagedForRemoval("a.txt"));

    s.stageForRemoval("a.txt");
    assertFalse(s.isStagedForAddition("a.txt"));
    assertTrue(s.isStagedForRemoval("a.txt"));

    s.stageForRemoval("b.txt");
    s.stageForAddition("b.txt", "id2");
    assertTrue(s.isStagedForAddition("b.txt"));
    assertFalse(s.isStagedForRemoval("b.txt"));
    assertEquals("id2", s.getStagedBlob("b.txt"));
  }

  @Test
  void unstageMethodsClearEntries() throws Exception {
    StagingArea s = new StagingArea(repoRoot());
    s.stageForAddition("c.txt", "id3");
    s.unstageAddition("c.txt");
    assertFalse(s.isStagedForAddition("c.txt"));

    s.stageForRemoval("d.txt");
    s.unstageRemoval("d.txt");
    assertFalse(s.isStagedForRemoval("d.txt"));
  }

  @Test
  void normalizationAndEscapes() throws Exception {
    StagingArea s = new StagingArea(repoRoot());
    s.stageForAddition("./dir/../file.txt", "blobX");
    assertTrue(s.isStagedForAddition("file.txt"));
    assertEquals("blobX", s.getStagedBlob("file.txt"));
    assertThrows(IllegalArgumentException.class, () -> s.stageForAddition("../../x.txt", "id"));
  }

  @Test
  void tabsOrNewlinesRejected() throws Exception {
    StagingArea s = new StagingArea(repoRoot());
    assertThrows(IllegalArgumentException.class, () -> s.stageForAddition("a\tb.txt", "id"));
    assertThrows(IllegalArgumentException.class, () -> s.stageForAddition("a.txt", "bad\tid"));
    assertThrows(IllegalArgumentException.class, () -> s.stageForRemoval("bad\nname"));
  }

  @Test
  void pruneAgainstRemovesNoops() throws Exception {
    StagingArea s = new StagingArea(repoRoot());
    Map<String,String> head = new TreeMap<>();
    head.put("same.txt", "B1");
    head.put("keepAdd.txt", "B2");
    head.put("keepRemove.txt", "B9");

    s.stageForAddition("same.txt", "B1");
    s.stageForAddition("keepAdd.txt", "B3");
    s.stageForRemoval("gone.txt");
    s.stageForRemoval("keepRemove.txt");

    s.pruneAgainst(head);

    assertFalse(s.isStagedForAddition("same.txt"));
    assertTrue(s.isStagedForAddition("keepAdd.txt"));
    assertTrue(s.isStagedForRemoval("keepRemove.txt"));
    assertFalse(s.isStagedForRemoval("gone.txt"));
  }

  @Test
  void applyToAppliesDeltaOverHead() throws Exception {
    StagingArea s = new StagingArea(repoRoot());
    Map<String,String> head = new TreeMap<>();
    head.put("A.txt", "1");
    head.put("B.txt", "2");

    s.stageForAddition("C.txt", "3");
    s.stageForAddition("B.txt", "22");
    s.stageForRemoval("A.txt");

    Map<String,String> out = s.applyTo(head);
    assertEquals(2, out.size());
    assertEquals("22", out.get("B.txt"));
    assertEquals("3", out.get("C.txt"));
    assertNull(out.get("A.txt"));
  }

  @Test
  void saveOrdersRecordsDeterministically() throws Exception {
    Path repo = repoRoot();
    Path idx = indexPath();

    StagingArea s = new StagingArea(repo);
    s.stageForAddition("c.txt", "C");
    s.stageForAddition("a.txt", "A");
    s.stageForAddition("b.txt", "B");
    s.stageForRemoval("z.txt");
    s.stageForRemoval("x.txt");
    s.stageForRemoval("y.txt");
    s.save(idx);

    String content = Files.readString(idx, StandardCharsets.UTF_8);
    String expected =
        "A\ta.txt\tA\n" +
        "A\tb.txt\tB\n" +
        "A\tc.txt\tC\n" +
        "R\tx.txt\n" +
        "R\ty.txt\n" +
        "R\tz.txt\n";
    assertEquals(expected, content);
  }

  @Test
  void loadErrorsOnCorruptLinesAndUnknownTags() throws Exception {
    Path idx = indexPath();

    Files.writeString(idx, "X\tfoo\n", StandardCharsets.UTF_8);
    StagingArea s1 = new StagingArea();
    assertThrows(IOException.class, () -> s1.load(idx));

    Files.writeString(idx, "A\tpathOnly\n", StandardCharsets.UTF_8);
    StagingArea s2 = new StagingArea();
    assertThrows(IOException.class, () -> s2.load(idx));

    Files.writeString(idx, "R\tpath\textra\n", StandardCharsets.UTF_8);
    StagingArea s3 = new StagingArea();
    assertThrows(IOException.class, () -> s3.load(idx));
  }
}
