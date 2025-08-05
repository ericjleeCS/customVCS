package gitlet;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class ConstantsTest {
  @TempDir Path tmp;

  @Test
  void pathHelpersResolveAsExpected() {
    Path dot = Constants.dot(tmp);
    assertEquals(dot, tmp.resolve(".gitlet"));
    assertEquals(dot.resolve("objects"), Constants.objects(tmp));
    assertEquals(dot.resolve("refs"), Constants.refs(tmp));
    assertEquals(dot.resolve("refs").resolve("heads"), Constants.heads(tmp));
    assertEquals(dot.resolve("HEAD"), Constants.headFile(tmp));
    assertEquals(dot.resolve("index"), Constants.indexFile(tmp));
    assertEquals(dot.resolve("refs").resolve("heads").resolve("main"), Constants.branchRef(tmp, "main"));
  }
}
