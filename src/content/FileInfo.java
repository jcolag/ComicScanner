/**
 * 
 */
package content;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import javax.imageio.*;

/**
 * @author john
 * 
 */
public class FileInfo {
	public static String archive = "archive", file = "file";
	public static ArrayList<FileInfo> fileData = new ArrayList<FileInfo>();
	private static Hashtable<String, Integer> hashes = new Hashtable<String, Integer>(),
			warnings = new Hashtable<String, Integer>();
	private static HashMap<String, String> signatures = new HashMap<String, String>() {
		{
			put("504b0304", "zip");
			put("526172211A0700", "rar");
			put("526172211A070100", "rar");
			put("7573746172003030:257", "tar");
			put("7573746172202000:257", "tar");
			put("377ABCAF271C", "7z");
			put("474946383761", "gif");
			put("474946383961", "gif");
			put("49492A00", "tiff");
			put("4D4D002A", "tiff");
			put("FFD8FFE0", "jpeg");
			put("FFD8FFE1", "jpeg");
			put("FFD8FFDB", "jpeg");
			put("89504E470D0A1A0A", "png");
		}
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
	};
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

	/**
	 * @return
	 */
	public static int nWarnings() {
		int total = 0;
		Iterator<Integer> iter = warnings.values().iterator();
		while (iter.hasNext()) {
			total += iter.next();
		}
		return total;
	}

	/**
	 * 
	 */
	public static void resetTracking() {
		fileData.clear();
		hashes.clear();
		warnings.clear();
	}

	public String name, type, hash, author;
	byte[] digest;
	int parentId;
	public int size, height = -1, width = -1;
	ArrayList<Integer> derivedFrom;
	public long createdOn;
	public boolean folder = false;

	boolean older, rotated, edited, brightened, colors, other;

	public boolean duplicate;
	
	/**
	 * @param buffer
	 * @param unknown
	 */
	public FileInfo(byte[] buffer, String unknown) {
		// Archive formats
		type = unknown;
		size = buffer.length;
		calculateDigest(buffer);
		Set<String> keys = signatures.keySet();
		Iterator<String> iter = keys.iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			int offset = 0;
			int colon = key.indexOf(':');
			if (colon >= 0) {
				String keypart = key.substring(0, colon);
				String offpart = key.substring(colon + 1);
				key = keypart;
				offset = Integer.parseInt(offpart);
			}
			if (compareBuffer(buffer, offset, key)) {
				type = signatures.get(key);
				break;
			}
		}

		try {
			InputStream in = new ByteArrayInputStream(buffer);
			BufferedImage image = ImageIO.read(in);
			height = image.getHeight();
			width = image.getWidth();
		} catch (IOException | NullPointerException | ArrayIndexOutOfBoundsException e) {
			// Probably not an image.
		}
		
		fileData.add(this);
		duplicate = hashes.containsKey(hash) && hashes.get(hash) == size;
		hashes.put(hash, size);
	}
	
	/**
	 * 
	 * @param input
	 *            is the original file
	 * @return true if the hash is valid
	 */
	private boolean calculateDigest(byte[] input) {
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
	
	/**
	 * @param buffer
	 * @param offset
	 * @param signature
	 * @return
	 */
	private boolean compareBuffer(byte[] buffer, int offset, String signature) {
		byte[] sig = javax.xml.bind.DatatypeConverter.parseHexBinary(signature);
		if (buffer.length < offset + sig.length) {
			return false;
		}
		for (int i = 0; i < sig.length; i++) {
			if (buffer[offset + i] != sig[i]) {
				return false;
			}
		}
		return true;
	}
	
	public String report() {
		String sz = height > 0 ? ("" + width + "x" + height + " ") : "";
		String rpt = name + " (" + sz + type + ")\n";
//		report += type + " " + hash;
//		report += " (" + size + ") - ";
//		report += name + "\n";
		return rpt;
	}
	
	/**
	 * @param condition
	 * @return
	 */
	private String warn(boolean condition, String mesg) {
		if (condition && !warnings.containsKey(hash)) {
			warnings.put(hash, 1);
		} else if (condition) {
			warnings.put(hash, warnings.get(hash) + 1);
		}
		return condition ? "\n" + mesg + ".\n" : "";
	}

	/**
	 * @return
	 */
	public String warnDuplicate() {
		return warn(duplicate, "Duplicate page");
	}
	
	/**
	 * @return
	 */
	public String warnFolder() {
		return warn(folder && !(name == ".DS_STORE" || name == "__MACOSX"),
				"Folder");
	}
	
	/**
	 * @return
	 */
	public String warnMac() {
		return warn(folder && (name == ".DS_STORE" || name == "__MACOSX"),
				"Mac OS archive");
	}
	
	/**
	 * @return
	 */
	public String warnOdd() {
		return warn(type == "file" && !folder && name != ".DS_STORE"
				&& name != "__MACOSX",
				"Non-Image file in archive");
	}
}
