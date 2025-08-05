package gitlet;

import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

public class HashingTest {

    @Test
    void emptyByteResultsInCorrectHash() throws Exception{
        byte[] bytes = new byte[0];
        String hex = Hashing.sha1(bytes);
        assertEquals("da39a3ee5e6b4b0d3255bfef95601890afd80709", hex);
    }

    @Test
    void abcReturnsExpectedSha() throws Exception{
        byte[] bytes = "abc".getBytes();
        String hex = Hashing.sha1(bytes);
        assertEquals("a9993e364706816aba3e25717850c26c9cd0d89d", hex);
    }

    @Test
    void sameInputSameOutput() throws Exception{
        byte[] testString = "Hello".getBytes();
        String testHex1 = Hashing.sha1(testString);
        String testHex2 = Hashing.sha1(testString);
        assertEquals(testHex1, testHex2);
    }

    @Test
    void diffInputDiffOutput() throws Exception{
        byte[] testString1 = "Hello".getBytes();
        byte[] testString2 = "Hello World".getBytes();
        String testHex1 = Hashing.sha1(testString1);
        String testHex2 = Hashing.sha1(testString2);
        assertNotEquals(testHex1, testHex2);
        assertEquals(40, testHex1.length());
        assertEquals(40, testHex2.length());
        assertTrue(testHex1.matches("^[0-9a-f]{40}$"));
        assertTrue(testHex2.matches("^[0-9a-f]{40}$"));
    }
}
