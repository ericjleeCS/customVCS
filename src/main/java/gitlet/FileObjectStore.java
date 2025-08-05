package gitlet;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.io.IOException;

public final class FileObjectStore {
      private final Path objectsDir;

    public FileObjectStore(Path repoDir){
        this.objectsDir = repoDir.resolve("objects");
    }

    public String writeBlob(byte[] content) throws IOException{
        String id = Hashing.sha1(content);
        String fanOutDir = id.substring(0, 2);
        String fileName = id.substring(2);
        Path dirPath = objectsDir.resolve(fanOutDir);
        Path filePath = dirPath.resolve(fileName);
        Files.createDirectories(dirPath);
        if (!Files.exists(filePath)){
            Files.write(filePath, content, StandardOpenOption.CREATE_NEW);
        }

        return id;
    }

    public byte[] readBlob(String id) throws IOException{
        Path blobPath = pathForId(id);
        if (Files.exists(blobPath)){
            return Files.readAllBytes(blobPath);
        } else {
            throw new IOException("Object " + id + " not found");  
        }
    }

    private Path pathForId(String id){
        String fanOutDir = id.substring(0, 2);
        String fileName = id.substring(2);
        return objectsDir.resolve(fanOutDir).resolve(fileName);
    }

    public boolean exists(String id){
        return Files.exists(pathForId(id));
    }

    public String resolvePrefix(String prefix) throws IOException{
        if (prefix.length() < 2){
            return null;
        }
        Path dir = objectsDir.resolve(prefix.substring(0,2));
        if (!Files.isDirectory(dir)){
            return null;
        }
        try (var stream = Files.list(dir)){
            var matches = stream
            .map(p -> prefix.substring(0, 2) + dir.relativize(p).toString())
            .filter(id -> id.startsWith(prefix))
            .toList();
            return matches.size() == 1 ? matches.get(0): null;
        }
    }


    
}
