package gitlet;

import java.nio.charset.StandardCharsets;
import java.util.*;
import static java.util.Objects.requireNonNull;

public final class Commit {
  private final String message;
  private final long timestamp;
  private final List<String> parents;
  private final Map<String, String> trackedFiles;
  private final String id;

  private Commit(String message, long timestamp, List<String> parents, Map<String,String> trackedFiles, String id) {
    this.message = requireNonNull(message, "message");
    this.timestamp = timestamp;
    this.parents = Collections.unmodifiableList(parents);
    this.trackedFiles = Collections.unmodifiableMap(trackedFiles);
    this.id = requireNonNull(id, "id");
  }

  public static Commit create(String message, List<String> parents, Map<String,String> tracked, long timestamp) {
    if (message.indexOf('\n') >= 0 || message.indexOf('\r') >= 0 || message.indexOf('\t') >= 0)
      throw new IllegalArgumentException("message must be single-line without tabs");
    List<String> ps = parents == null ? List.of() : List.copyOf(parents);
    Map<String,String> tf = new TreeMap<>(requireNonNull(tracked, "tracked"));
    byte[] payload = serializePayload(message, timestamp, ps, tf);
    String id = Hashing.sha1(payload);
    return new Commit(message, timestamp, ps, tf, id);
  }

  public static Commit deserialize(byte[] data) {
    String s = new String(requireNonNull(data, "data"), StandardCharsets.UTF_8);
    String[] lines = s.split("\n", -1);
    if (lines.length < 2 || !"commit".equals(lines[0])) throw new IllegalArgumentException("bad commit payload");
    String[] m = splitOnce(lines[1], '\t');
    if (m.length != 2 || !"message".equals(m[0])) throw new IllegalArgumentException("bad message field");
    String message = m[1];
    String[] t = splitOnce(lines[2], '\t');
    if (t.length != 2 || !"timestamp".equals(t[0])) throw new IllegalArgumentException("bad timestamp field");
    long timestamp = Long.parseLong(t[1]);

    List<String> parents = new ArrayList<>();
    Map<String,String> tracked = new TreeMap<>();
    for (int i = 3; i < lines.length; i++) {
      if (lines[i].isEmpty()) continue;
      String[] parts = splitOnce(lines[i], '\t');
      if (parts.length < 2) throw new IllegalArgumentException("bad line: " + lines[i]);
      if ("parent".equals(parts[0])) {
        parents.add(parts[1]);
      } else if ("file".equals(parts[0])) {
        String[] fb = splitOnce(parts[1], '\t');
        if (fb.length != 2) throw new IllegalArgumentException("bad file line: " + lines[i]);
        tracked.put(fb[0], fb[1]);
      } else {
        throw new IllegalArgumentException("unknown field: " + parts[0]);
      }
    }
    byte[] payload = serializePayload(message, timestamp, parents, tracked);
    String id = Hashing.sha1(payload);
    return new Commit(message, timestamp, parents, tracked, id);
  }

  public byte[] serialize() {
    return serializePayload(message, timestamp, parents, trackedFiles);
  }

  public String getMessage() { return message; }
  public long getTimestamp() { return timestamp; }
  public List<String> getParents() { return parents; }
  public Map<String,String> getTrackedFiles() { return trackedFiles; }
  public String getId() { return id; }

  private static byte[] serializePayload(String message, long timestamp, List<String> parents, Map<String,String> tracked) {
    StringBuilder sb = new StringBuilder();
    sb.append("commit").append('\n');
    sb.append("message").append('\t').append(message).append('\n');
    sb.append("timestamp").append('\t').append(timestamp).append('\n');
    for (String p : parents) sb.append("parent").append('\t').append(p).append('\n');
    for (Map.Entry<String,String> e : tracked.entrySet())
      sb.append("file").append('\t').append(e.getKey()).append('\t').append(e.getValue()).append('\n');
    return sb.toString().getBytes(StandardCharsets.UTF_8);
  }

  private static String[] splitOnce(String s, char sep) {
    int i = s.indexOf(sep);
    return i < 0 ? new String[]{s} : new String[]{s.substring(0, i), s.substring(i + 1)};
  }
}
