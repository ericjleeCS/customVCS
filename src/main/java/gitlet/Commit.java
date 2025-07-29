package gitlet;
import java.util.*;

public final class Commit {
    private final String message;
    private final long timestamp;
    private final List<String> parents;
    private final Map<String, String> trackedFiles;
    private final String id;

    public Commit(String message, List<String> parents, Map<String, String> tracked, String id, long timestamp){
        this.message = Objects.requireNonNull(message, "Message is required");
        this.parents = List.copyOf(parents);
        this.trackedFiles = Collections.unmodifiableMap(tracked);
        this.id = Objects.requireNonNull(id, "Message is required");
        this.timestamp = timestamp;
    }

    public String getMessage(){
        return message;
    }
    public long getTimeStamp(){
        return timestamp;
    }
    public List<String> getParents(){
        return parents;
    }
    public Map<String,String> getTrackedFiles(){
        return trackedFiles;
    }
    public String getId(){
        return id;
    }    

}
