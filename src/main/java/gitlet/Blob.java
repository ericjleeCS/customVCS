package gitlet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

public class Blob {
    private final String id;
    private final byte[] content;


    private Blob(byte[] content){
        this.content = Objects.requireNonNull(content, "content");
        this.id = Hashing.sha1HashString(content);
    }

    public static Blob fromFile(Path path) throws IOException{
        return new Blob(Files.readAllBytes(path));
    }

    public static Blob fromBytes(byte[] data){
        return new Blob(Arrays.copyOf(data, data.length));
    }

    public String getId(){
        return id;
    }

    public byte[] getContent(){
        return Arrays.copyOf(content, content.length);
    }

    public long getSize(){
        return content.length;
    }

    public boolean isEmpty(){
        return content.length == 0;
    }
}
