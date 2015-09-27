package content;

import java.util.Comparator;

/**
 * @author john
 * 
 */
class FileInfoComparator implements Comparator<FileInfo> {
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(FileInfo arg0, FileInfo arg1) {
		return arg0.name.compareTo(arg1.name);
	}
}
