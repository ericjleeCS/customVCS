package gitlet;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hashing {
    private Hashing() {
    }
    public static String sha1(byte[] data){
        try{
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            byte[] messageDigestAsByte = messageDigest.digest(data);
            BigInteger signum = new BigInteger(1, messageDigestAsByte);
            String hashtext = signum.toString(16);
            while (hashtext.length() < 40){
                hashtext = "0" + hashtext;
            }
            return hashtext;
        }

        catch (NoSuchAlgorithmException e){
            throw new RuntimeException(e);
        }


    }
}
