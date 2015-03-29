/**
 * 
 */
package content;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

/**
 * @author john
 * 
 */
public class FileInfo {
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public String name, type, hash, author;
	byte[] digest;
	int parentId;
	public int size;
	ArrayList<Integer> derivedFrom;
	public long createdOn;
	boolean older, rotated, edited, brightened, colors, other;

	/**
	 * 
	 */
	public FileInfo() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 * @param input is the original file
	 * @return true if the hash is valid
	 */
	public boolean CalculateDigest(byte[] input) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(input);
			digest = md.digest();
			hash = "";
			for (int i=0; i < digest.length; i++) {
				int v = digest[i] & 0xFF;
				hash += hexArray[v >>> 4];
				hash += hexArray[v & 0x0F];
			}
		} catch (NoSuchAlgorithmException e) {
			return false;
		}
		return true;
	}
}
