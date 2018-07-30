//java code belongs to this website
//http://www.sha1-online.com/sha1-java/
//However, there are some changes to it

package MyChordPackage;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
 
public class Hasher {
     
    static String sha1(String input) throws NoSuchAlgorithmException {
        MessageDigest mDigest = MessageDigest.getInstance("SHA1");
        byte[] result = mDigest.digest(input.getBytes());
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < result.length; i++) {
            sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
        }
         
        return sb.toString();
    }
    
    
    String returnHashString(String input) throws NoSuchAlgorithmException
    {
    	return sha1(input);
    }
}