/**
 * 
 */
package content;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.imageio.*;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.google.gson.*;

import borrowed.diff_match_patch;
import borrowed.diff_match_patch.Diff;
import content.DocumentRecord;

/**
 * @author john
 * 
 */
public class FileInfo {
	public static String archive = "archive", file = "file", apikey = "",
			defaultBaseUrl = "http://localhost:3000/";
	public static ArrayList<FileInfo> fileData = new ArrayList<FileInfo>();
	public static boolean networkFailed = false;
	private static int avgHt = 0, avgWd = 0, avgSz = 0, imgCount = 0;
	private static final float tolerance = 1.1F;
	private static Gson gson = new Gson();
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
		networkFailed = false;
	}

	/**
	 * 
	 */
	public static void sortFiles() {
		Collections.sort(fileData, new FileInfoComparator());
		avgSz /= fileData.size();
		if (imgCount == 0) {
			imgCount = 1;
		}
		avgHt /= imgCount;
		avgWd /= imgCount;
		ArrayList<String> namesPersonSort = new ArrayList<String>();
		Iterator<FileInfo> iter = fileData.iterator();
		while (iter.hasNext()) {
			namesPersonSort.add(iter.next().name);
		}
		Collections.sort(namesPersonSort, new NaturalOrderComparator());
		for (int i = 0; i < fileData.size(); i++) {
			FileInfo file = fileData.get(i);
			file.sortOffset = namesPersonSort.indexOf(file.name) - i;
		}
		assignPageNumbers();
	}

	private static void assignPageNumbers() {
		/*
		 * Find the common string between image file names. Starts with the
		 * middle file in hopes of avoiding alternate covers and scanner tag
		 * pages.
		 */
		diff_match_patch dmp = new diff_match_patch();
		String common = fileData.get(fileData.size() / 2).name;
		for (int i = 0; i < fileData.size(); i++) {
			if (fileData.get(i).width <= 0) {
				continue;
			}
			String name = fileData.get(i).name;
			LinkedList<Diff> diffs = dmp.diff_main(common, name);
			Iterator<Diff> df = diffs.iterator();
			String test = "";
			/*
			 * Collect similarities between names
			 */
			while (df.hasNext()) {
				Diff d = df.next();
				if (d.operation != diff_match_patch.Operation.EQUAL) {
					continue;
				}
				test += d.text + "*";
			}
			/*
			 * Early on, look for approximate similarities, but then shift to
			 * very minor differences, for example, only changing page numbers
			 */
			if ((i < 3 && test.length() - diffs.size() > name.length() * 2 / 3)
					|| Math.abs(common.length() - test.length()) < 3) {
				common = test;
			}
		}
		/*
		 * Find differences between individual image file names and the common
		 * naming convention
		 */
		int prev = 0;
		for (int i = 0; i < fileData.size(); i++) {
			if (fileData.get(i).width <= 0) {
				continue;
			}
			LinkedList<Diff> diffs = dmp
					.diff_main(common, fileData.get(i).name);
			Iterator<Diff> df = diffs.iterator();
			String test = "";
			/*
			 * Collect insertions that begin with a digit, which are hopefully
			 * only page numbers
			 */
			while (df.hasNext()) {
				Diff d = df.next();
				if (d.operation != diff_match_patch.Operation.INSERT
						|| !Character.isDigit(d.text.charAt(0))) {
					continue;
				}
				test += d.text + "*";
			}
			if (test.length() > 1) {
				test = test.substring(0, test.length() - 1);
			}
			try {
				int page = Integer.parseInt(test);
				fileData.get(i).pageNumber = page;
				if (page > prev + 1) {
					System.out.println("Missing page!");
				} else if (page == prev) {
					System.out.println("Duplicate page number!");
				}
				prev = page;
			} catch (NumberFormatException e) {
			}
			System.out.println(test);
		}
	}

	public String name, type, hash, author, imageError = "";
	byte[] digest;
	int parentId;
	public int size, height = -1, width = -1, sortOffset = 0, pageNumber = -1;
	ArrayList<Integer> derivedFrom;
	public long createdOn;
	public boolean folder = false;

	boolean older, rotated, edited, brightened, colors, other;

	public boolean duplicate;

	private DocumentRecord officialDocument;
	private RestClient rest;

	/**
	 * @param buffer
	 * @param unknown
	 */
	public FileInfo(byte[] buffer, String unknown) {
		// Archive formats
		type = unknown;
		size = buffer.length;
		avgSz += size;
		calculateDigest(buffer);
		rest = new RestClient(defaultBaseUrl);
		setOfficialDocument(getDocumentWithDigest(hash));
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
			avgHt += height;
			avgWd += width;
			imgCount++;
		} catch (IOException | NullPointerException
				| ArrayIndexOutOfBoundsException e) {
			// Probably not an image.
			imageError = e.getMessage();
		}

		fileData.add(this);
		duplicate = hashes.containsKey(hash) && hashes.get(hash) == size;
		hashes.put(hash, size);
	}

	/**
	 * @param digest
	 * @return
	 */
	private DocumentRecord getDocumentWithDigest(String digest) {
		DocumentRecord doc = null;
		if (networkFailed) {
			return doc;
		}
		try {
			String aliases = rest.getFromUrl("/documents/" + digest + ".json");
			doc = gson.fromJson(aliases, DocumentRecord.class);
		} catch (ConnectException e1) {
			networkFailed = true;
			e1.printStackTrace();
		} catch (com.google.gson.JsonSyntaxException e1) {
			System.out.println("Malformed response");
		}
		return doc;
	}

	public List<NameValuePair> prepareSubmission() {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("name", name));
		params.add(new BasicNameValuePair("file_type", type));
		params.add(new BasicNameValuePair("digest", hash));
		params.add(new BasicNameValuePair("imageError", imageError));
		params.add(new BasicNameValuePair("modified", ""));
		params.add(new BasicNameValuePair("size", new Integer(size).toString()));
		params.add(new BasicNameValuePair("height", new Integer(height)
				.toString()));
		params.add(new BasicNameValuePair("width", new Integer(width)
				.toString()));
		params.add(new BasicNameValuePair("page", new Integer(pageNumber)
				.toString()));
		params.add(new BasicNameValuePair("folder", new Boolean(folder)
				.toString()));
		params.add(new BasicNameValuePair("apikey", apikey));
		return params;
	}

	public boolean sendSubmission() {
		List<NameValuePair> params = prepareSubmission();
		try {
			String result = rest.postToUrl("/submissions.json", params);
			System.out.println(result);
		} catch (ConnectException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
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
		String sz = height > 0 ? ("" + width + "x" + height + " ")
				: ("" + size + " byte ");
		String rpt = name + " (" + sz + type + ")\n";
		// report += type + " " + hash;
		// report += " (" + size + ") - ";
		// report += name + "\n";
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
	public String warnHeight() {
		return warn(width > 0
				&& (height > tolerance * avgHt || height < tolerance / avgHt),
				"Image height is inconsistent with remainder of archive");
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
				&& name != "__MACOSX", "Non-Image file in archive");
	}

	public String warnOrder() {
		return warn(sortOffset != 0 && width > 0,
				"File name does not match natural sort order");
	}

	/**
	 * @return
	 */
	public String warnSize() {
		return warn(width > 0
				&& (size > tolerance * avgSz || size < tolerance / avgSz),
				"File size is inconsistent with remainder of archive");
	}

	/**
	 * @return
	 */
	public String warnWidth() {
		return warn(width > 0
				&& (width > tolerance * avgWd || width < tolerance / avgWd),
				"Image width is inconsistent with remainder of archive");
	}

	/**
	 * @return the officialDocument
	 */
	public DocumentRecord getOfficialDocument() {
		return officialDocument;
	}

	/**
	 * @param officialDocument
	 *            the officialDocument to set
	 */
	public void setOfficialDocument(DocumentRecord officialDocument) {
		this.officialDocument = officialDocument;
	}
}
