package gitlet;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class CommitTest {

  @Test
  void createSerializeDeserializeRoundTrip() {
    String msg = "init";
    List<String> parents = List.of();
    Map<String,String> tracked = Map.of("a.txt", "blobA", "b.txt", "blobB");

    Commit c1 = Commit.create(msg, parents, tracked, 123456789L);
    byte[] bytes = c1.serialize();
    Commit c2 = Commit.deserialize(bytes);

    assertEquals(c1.getMessage(), c2.getMessage());
    assertEquals(c1.getTimestamp(), c2.getTimestamp());
    assertEquals(c1.getParents(), c2.getParents());
    assertEquals(c1.getTrackedFiles(), c2.getTrackedFiles());
    assertEquals(c1.getId(), c2.getId());
    assertArrayEquals(bytes, c2.serialize());
  }

  @Test
  void deterministicIdForSamePayload() {
    String msg = "commit";
    List<String> parents = List.of("p1", "p2");
    Map<String,String> tracked = Map.of("x", "X", "y", "Y");

    Commit c1 = Commit.create(msg, parents, tracked, 42L);
    Commit c2 = Commit.create(msg, parents, tracked, 42L);

    assertEquals(c1.getId(), c2.getId());
  }

  @Test
  void invalidMessageRejected() {
    assertThrows(IllegalArgumentException.class,
        () -> Commit.create("bad\nline", List.of(), Map.of(), 1L));
    assertThrows(IllegalArgumentException.class,
        () -> Commit.create("bad\tline", List.of(), Map.of(), 1L));
  }

  @Test
  void deserializeRejectsGarbage() {
    assertThrows(IllegalArgumentException.class,
        () -> Commit.deserialize("nope".getBytes(StandardCharsets.UTF_8)));
  }
}
