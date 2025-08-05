package gitlet;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public final class StagingArea {
  private final TreeMap<String, String> additions = new TreeMap<>();
  private final TreeSet<String> removals = new TreeSet<>();
  private final Path repoRoot;

  public StagingArea() {
    this.repoRoot = null;
  }

  public StagingArea(Path repoRoot) {
    this.repoRoot = Objects.requireNonNull(repoRoot, "repoRoot").toAbsolutePath().normalize();
  }

  public void stageForAddition(String path, String blobId) {
    if (path == null || blobId == null) throw new IllegalArgumentException("path/blobId required");
    String normPath = normalizeAndValidate(path);
    validateField(blobId, "blobId");
    removals.remove(normPath);
    additions.put(normPath, blobId);
  }

  public void stageForRemoval(String path) {
    if (path == null) throw new IllegalArgumentException("path required");
    String normPath = normalizeAndValidate(path);
    additions.remove(normPath);
    removals.add(normPath);
  }

  public void unstageAddition(String path) {
    if (path == null) return;
    additions.remove(normalizeAndValidate(path));
  }

  public void unstageRemoval(String path) {
    if (path == null) return;
    removals.remove(normalizeAndValidate(path));
  }

  public boolean isStagedForAddition(String path) {
    if (path == null) return false;
    return additions.containsKey(normalizeAndValidate(path));
  }

  public boolean isStagedForRemoval(String path) {
    if (path == null) return false;
    return removals.contains(normalizeAndValidate(path));
  }

  public String getStagedBlob(String path) {
    if (path == null) return null;
    return additions.get(normalizeAndValidate(path));
  }

  public Map<String, String> getAdditions() {
    return Collections.unmodifiableMap(additions);
  }

  public Set<String> getRemovals() {
    return Collections.unmodifiableSet(removals);
  }

  public boolean isEmpty() {
    return additions.isEmpty() && removals.isEmpty();
  }

  public void clear() {
    additions.clear();
    removals.clear();
  }

  public Map<String, String> applyTo(Map<String, String> baseTracked) {
    TreeMap<String, String> out = new TreeMap<>(baseTracked);
    for (var e : additions.entrySet()) out.put(e.getKey(), e.getValue());
    for (var p : removals) out.remove(p);
    return out;
  }

  public void pruneAgainst(Map<String, String> headTracked) {
    additions.entrySet().removeIf(e -> e.getValue().equals(headTracked.get(e.getKey())));
    removals.removeIf(p -> !headTracked.containsKey(p));
  }

  public void save(Path indexFile) throws IOException {
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, String> e : additions.entrySet()) {
      sb.append('A').append('\t').append(e.getKey()).append('\t').append(e.getValue()).append('\n');
    }
    for (String p : removals) {
      sb.append('R').append('\t').append(p).append('\n');
    }
    Path parent = indexFile.getParent();
    if (parent != null) Files.createDirectories(parent);
    Files.writeString(
        indexFile,
        sb.toString(),
        StandardCharsets.UTF_8,
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING,
        StandardOpenOption.WRITE
    );
  }

  public void load(Path indexFile) throws IOException {
    clear();
    if (!Files.exists(indexFile)) return;
    for (String line : Files.readAllLines(indexFile, StandardCharsets.UTF_8)) {
      if (line.isEmpty()) continue;
      String[] parts = line.split("\t", 3);
      if (parts.length == 0 || parts[0].isEmpty()) throw new IOException("Corrupt index line: " + line);
      char tag = parts[0].charAt(0);
      switch (tag) {
        case 'A': {
          if (parts.length != 3) throw new IOException("Corrupt index line (A): " + line);
          String path = normalizeAndValidate(parts[1]);
          String blobId = parts[2];
          validateField(blobId, "blobId");
          removals.remove(path);
          additions.put(path, blobId);
          break;
        }
        case 'R': {
          if (parts.length != 2) throw new IOException("Corrupt index line (R): " + line);
          String path = normalizeAndValidate(parts[1]);
          additions.remove(path);
          removals.add(path);
          break;
        }
        default:
          throw new IOException("Unknown index record: " + line);
      }
    }
  }

  private String normalizeAndValidate(String pathString) {
    validateField(pathString, "path");
    String clean;
    if (repoRoot != null) {
      Path resolved = repoRoot.resolve(pathString).normalize();
      if (!resolved.startsWith(repoRoot)) throw new IllegalArgumentException("Path escapes repo: " + pathString);
      clean = repoRoot.relativize(resolved).toString();
    } else {
      clean = pathString;
    }
    clean = clean.replace('\\', '/');
    if (clean.isEmpty() || clean.equals(".") || clean.startsWith("../") || clean.contains("/../") || clean.endsWith("/.."))
      throw new IllegalArgumentException("Invalid path: " + pathString);
    if (containsForbiddenTsvChars(clean)) throw new IllegalArgumentException("Path contains tab/newline: " + pathString);
    return clean;
  }

  private static void validateField(String value, String name) {
    if (value == null) throw new IllegalArgumentException(name + " required");
    if (value.isEmpty()) throw new IllegalArgumentException(name + " must not be empty");
    if (containsForbiddenTsvChars(value)) throw new IllegalArgumentException(name + " contains tab/newline");
  }

  private static boolean containsForbiddenTsvChars(String s) {
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == '\t' || c == '\n' || c == '\r') return true;
    }
    return false;
  }
}
