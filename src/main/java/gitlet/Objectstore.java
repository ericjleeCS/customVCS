package gitlet;

import java.io.IOException;
import java.nio.file.Path;

public class ObjectStore {
  private final FileObjectStore store;

  public ObjectStore(Path repoRoot) throws IOException {
    Path objDir = Constants.objects(repoRoot);
    // No filesystem writes here; init() creates layout.
    this.store = new FileObjectStore(objDir);
  }

  public String writeBlob(byte[] content) throws IOException {
    return store.writeBlob(content);
  }

  public byte[] readBlob(String blobId) throws IOException {
    return store.readBlob(blobId);
  }

  public String writeCommit(Commit commit) throws IOException {
    byte[] data = commit.serialize();
    String id = store.writeBlob(data);
    if (!id.equals(commit.getId())) throw new IllegalStateException("Commit id mismatch");
    return id;
  }

  public Commit readCommit(String id) throws IOException {
    byte[] data = store.readBlob(id);
    return Commit.deserialize(data);
  }
}
