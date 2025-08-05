package gitlet;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.nio.file.Files;
import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;

public class FileObjectStoreTest {
    @TempDir Path tmp;

    private FileObjectStore newStore() throws IOException{
        Path repoDir = tmp.resolve("gitlet");
        Files.createDirectories(repoDir);
        return new FileObjectStore(repoDir);
    }



    @Test
    void writeBlobCalledTwiceLeavesFileUnchanged() throws Exception{
        FileObjectStore store = newStore();

        Path f = tmp.resolve("foo.txt");
        Files.writeString(f, "Hello World");
        Blob blob = Blob.fromFile(f);

        String id1 = store.writeBlob(blob.getContent());
        String id2 = store.writeBlob(blob.getContent());

        assertEquals(id1, id2);
        assertArrayEquals(blob.getContent(), store.readBlob(id1));

    }

    @Test
    void writeThenReadReturnsSameBytes() throws Exception{
        FileObjectStore store = newStore();
        byte[] data = "Hello World".getBytes();
        String id = store.writeBlob(data);
        assertArrayEquals(data, store.readBlob(id));
    }

    @Test
    void differentContentProducesDifferentIds() throws Exception{
        FileObjectStore store = newStore();

        byte[] store1Data = "Hello".getBytes();
        byte[] store2Data = "Hello World".getBytes();

        String store1Id = store.writeBlob(store1Data);
        String store2Id = store.writeBlob(store2Data);

        assertNotEquals(store1Id, store2Id);
    }

    @Test
    void existsReflectsWrittenObjects() throws Exception{
        FileObjectStore store = newStore();
        byte[] data = "Hello".getBytes();
        String id = store.writeBlob(data);
        assertTrue(store.exists(id));
        assertFalse(store.exists("randomwordsthataren'ttrue"));
        
    }

    @Test
    void resolvePrefixHandlesUniqueAmbiguousAndNone() throws Exception{
        FileObjectStore store = newStore();
        String id1 = store.writeBlob("prefixOne".getBytes());
        String id2 = store.writeBlob("prefixTwo".getBytes());
        String unique = id1.substring(0,8);
        assertEquals(id1, store.resolvePrefix(unique));
        if (id1.substring(0, 2).equals(id2.substring(0, 2))) {
            String ambiguous2 = id1.substring(0, 2);  
            assertNull(store.resolvePrefix(ambiguous2));
        }
        assertNull(store.resolvePrefix("ffff"));

    }

    @Test
    void readBlobThrowsWhenMissing() throws Exception{
        FileObjectStore store = newStore();
        String bogus = "0123456789abcdef0123456789abcdef01234567";
        assertThrows(IOException.class, () -> store.readBlob(bogus));
    }



    

}
