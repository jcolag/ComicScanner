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
	public static String archive = "archive", file = "file";
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public String name, type, hash, author;
	byte[] digest;
	int parentId;
	public int size;
	ArrayList<Integer> derivedFrom;
	public long createdOn;
	boolean older, rotated, edited, brightened, colors, other;

	/**
	 * @param buffer
	 * @param unknown
	 */
	public FileInfo(byte[] buffer, String unknown) {
		// Archive formats
		if (buffer.length > 3 && (char) buffer[0] == 'P'
				&& (char) buffer[1] == 'K' && buffer[2] == (byte) 0x03
				&& buffer[3] == (byte) 0x04) {
			type = "zip";
		} else if (buffer.length > 7
				&& (char) buffer[0] == 'R'
				&& (char) buffer[1] == 'a'
				&& (char) buffer[2] == 'r'
				&& (char) buffer[3] == '!'
				&& buffer[4] == (byte) 0x1A
				&& buffer[5] == (byte) 0x07
				&& ((buffer[6] == (byte) 0x00 || (buffer[6] == (byte) 0x01 && buffer[7] == (byte) 0x00)))) {
			type = "rar";
		} else if (buffer.length > 264
				&& buffer[257] == (byte) 0x75
				&& buffer[258] == (byte) 0x73
				&& buffer[259] == (byte) 0x74
				&& buffer[260] == (byte) 0x61
				&& buffer[261] == (byte) 0x72
				&& ((buffer[262] == (byte) 0x00 && buffer[263] == (byte) 0x30 && buffer[264] == (byte) 0x30) || (buffer[262] == (byte) 0x20
						&& buffer[263] == (byte) 0x20 && buffer[264] == (byte) 0x00))) {
			type = "tar";
		} else if (buffer.length > 5 && (char) buffer[0] == '7'
				&& (char) buffer[1] == 'z' && buffer[2] == (byte) 0xBC
				&& buffer[3] == (byte) 0xAF && buffer[4] == (byte) 0x27
				&& buffer[5] == (byte) 0x1C) {
			type = "7z";
			// Image formats
		} else if (buffer.length > 5 && buffer[0] == (byte) 0x47
				&& buffer[1] == (byte) 0x49 && buffer[2] == (byte) 0x46
				&& buffer[3] == (byte) 0x38
				&& (buffer[4] == (byte) 0x37 || buffer[4] == (byte) 0x39)
				&& buffer[5] == (byte) 0x61) {
			type = "gif";
		} else if (buffer.length > 3 && buffer[0] == (byte) 0x49
				&& buffer[1] == (byte) 0x49 && buffer[2] == (byte) 0x2A
				&& buffer[3] == (byte) 0x00) {
			type = "tiff";
		} else if (buffer.length > 3 && buffer[0] == (byte) 0x4D
				&& buffer[1] == (byte) 0x4D && buffer[2] == (byte) 0x00
				&& buffer[3] == (byte) 0x2A) {
			type = "tiff";
		} else if (buffer.length > 3 && buffer[0] == (byte) 0xFF
				&& buffer[1] == (byte) 0xD8 && buffer[2] == (byte) 0xFF
				&& buffer[3] == (byte) 0xE0) {
			type = "jpeg";
		} else if (buffer.length > 7 && buffer[0] == (byte) 0x89
				&& buffer[1] == (byte) 0x50 && buffer[2] == (byte) 0x4E
				&& buffer[3] == (byte) 0x47 && buffer[4] == (byte) 0x0D
				&& buffer[5] == (byte) 0x0A && buffer[6] == (byte) 0x1A
				&& buffer[7] == (byte) 0x0A) {
			type = "png";
		} else {
			type = unknown;
		}

		size = buffer.length;
		CalculateDigest(buffer);
	}

	/**
	 * 
	 * @param input
	 *            is the original file
	 * @return true if the hash is valid
	 */
	private boolean CalculateDigest(byte[] input) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(input);
			digest = md.digest();
			hash = "";
			for (int i = 0; i < digest.length; i++) {
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
