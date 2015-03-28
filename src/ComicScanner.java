import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import content.FileInfo;

/**
 * 
 */

/**
 * @author john
 * 
 */
public class ComicScanner extends JApplet implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	JPanel panelMaster;
	JList<String> listPublisher, listSeries;
	JScrollPane scrollPublisher, scrollSeries, scrollReport;
	JTextField textNumber, textUsername;
	JPasswordField textPassword;
	JFileChooser chooseComic;
	JButton buttonChoose, buttonCheck, buttonSend;
	JTextPane textReport;
	Container cPane;

	DefaultListModel<String> listModel;

	String pathname, filename;
	ArrayList<String> compressedFiles;
	ArrayList<FileInfo> fileData;

	// Called when this applet is loaded into the browser.
	public void init() {
		// Execute a job on the event-dispatching thread; creating this applet's
		// GUI.
		listModel = new DefaultListModel<String>();

		listPublisher = new JList<String>();
		scrollPublisher = new JScrollPane();
		scrollPublisher.setViewportView(listPublisher);

		listSeries = new JList<String>();
		scrollSeries = new JScrollPane();
		scrollSeries.setViewportView(listSeries);

		textReport = new JTextPane();
		scrollReport = new JScrollPane();
		scrollReport.setViewportView(textReport);
		textReport.setEditable(false);

		textNumber = new JTextField("0", 4);
		textUsername = new JTextField(32);
		textPassword = new JPasswordField(32);
		buttonChoose = new JButton("Choose Comic...");
		buttonCheck = new JButton("Analyze");
		buttonSend = new JButton("Send");

//		listModel.addElement("Ace");
//		listModel.addElement("Ajax-Farrell");
//		listModel.addElement("American Comics Group");
//		listModel.addElement("Avon");
//		listModel.addElement("Better/Nedor/Standard/Pines");
//		listModel.addElement("Centaur");
//		listModel.addElement("Charlton");
//		listModel.addElement("Chesler");
//		listModel.addElement("Columbia");
//		listModel.addElement("Dell");
		listPublisher.setModel(listModel);

		cPane = getContentPane();
		cPane.setLayout(new GridBagLayout());

//		addControlToContainer(cPane, 0, 0, scrollPublisher, true, 0);
//		addControlToContainer(cPane, 1, 0, scrollSeries, true, 0);
//		addControlToContainer(cPane, 2, 0, textNumber, false, 0);
//		addControlToContainer(cPane, 0, 1, textUsername, false, 0);
//		addControlToContainer(cPane, 1, 1, textPassword, false, 0);
		addControlToContainer(cPane, 0, 0, buttonChoose, false, 0);
		addControlToContainer(cPane, 0, 1, buttonCheck, false, 0);
		addControlToContainer(cPane, 1, 1, buttonSend, false, 0);
		addControlToContainer(cPane, 0, 2, scrollReport, true, 1);

		buttonChoose.addActionListener(this);
		buttonCheck.addActionListener(this);
		buttonSend.addActionListener(this);
	}

	/**
	 * 
	 */
	public void addControlToContainer(Container pane, int x, int y,
			JComponent control, boolean tall, int extraColumns) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = x;
		gbc.gridy = y;
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.ipadx = 150;
		if (tall) {
			gbc.fill = GridBagConstraints.BOTH;
			gbc.ipady = 200;
			gbc.weighty = 0.5;
		} else {
			gbc.fill = GridBagConstraints.HORIZONTAL;
		}
		gbc.gridwidth = extraColumns + 1;
		pane.add(control, gbc);
	}

	/**
	 * @throws HeadlessException
	 */
	public ComicScanner() throws HeadlessException {
		// TODO Auto-generated constructor stub
		compressedFiles = new ArrayList<String>();
		fileData = new ArrayList<FileInfo>();
	}

	/**
	 * 
	 */
	private void RetrieveFile() {
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"Comic Book Archives", "cbr", "cbz", "cbt");
		chooser.setFileFilter(filter);
		int returnVal = chooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			try {
				pathname = chooser.getSelectedFile().getCanonicalPath();
				filename = chooser.getSelectedFile().getName();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * 
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == buttonChoose) {
			RetrieveFile();
		} else if (e.getSource() == buttonCheck) {
			compressedFiles.clear();
			FileInfo archInfo = ArchiveType(pathname);
			archInfo.name = filename;
			fileData.add(archInfo);
			switch (archInfo.type) {
			case 'z':
				ZipFile zipFile = null;
				Enumeration<?> entries;

				try {
					zipFile = new ZipFile(pathname);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if (zipFile != null) {
					entries = zipFile.entries();
					while (entries.hasMoreElements()) {
						ZipEntry entry = (ZipEntry) entries.nextElement();
						String fname = entry.getName();
						long fileSize = entry.getSize();
						compressedFiles.add(fname);

						try {
							byte[] buffer = new byte[(int) fileSize];
							int len;
							FileInfo info;
							InputStream in;
							in = zipFile.getInputStream(entry);
							len = in.read(buffer);
							info = ImageType(buffer);
							info.name = entry.getName();
							info.createdOn = entry.getTime();
							info.size = len;
							info.CalculateDigest(buffer);
							fileData.add(info);
							in.close();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}

					}
				}

				try {
					zipFile.close();
				} catch (IOException e1) {
					// Don't bother
				}

				break;
			}
		}

	}

	/**
	 * g => GIF
	 * j => JPEG
	 * p => PNG
	 * t => TIFF
	 */
	private FileInfo ImageType(byte[] image) {
		FileInfo fi = new FileInfo();
		if (image.length > 5 && image[0] == (byte) 0x47 && image[1] == (byte) 0x49
				&& image[2] == (byte) 0x46 && image[3] == (byte) 0x38
				&& (image[4] == (byte) 0x37 || image[4] == (byte) 0x39)
				&& image[5] == (byte) 0x61) {
			fi.type = 'g';
		} else if (image.length > 3 && image[0] == (byte) 0x49
				&& image[1] == (byte) 0x49 && image[2] == (byte) 0x2A
				&& image[3] == (byte) 0x00) {
			fi.type = 't';
		} else if (image.length > 3 && image[0] == (byte) 0x4D
				&& image[1] == (byte) 0x4D && image[2] == (byte) 0x00
				&& image[3] == (byte) 0x2A) {
			fi.type = 't';
		} else if (image.length > 3 && image[0] == (byte) 0xFF
				&& image[1] == (byte) 0xD8 && image[2] == (byte) 0xFF
				&& image[3] == (byte) 0xE0) {
			fi.type = 'j';
		} else if (image.length > 7 && image[0] == (byte) 0x89
				&& image[1] == (byte) 0x50 && image[2] == (byte) 0x4E
				&& image[3] == (byte) 0x47 && image[4] == (byte) 0x0D
				&& image[5] == (byte) 0x0A && image[6] == (byte) 0x1A
				&& image[7] == (byte) 0x0A) {
			fi.type = 'p';
		}
		return fi;
	}

	/**
	 * Returns the sort of archive.
	 *  n => No file
	 *  r => RAR file
	 *  t => TAR file
	 *  z => ZIP file
	 *  x => Unknown
	 */
	private FileInfo ArchiveType(String filename) {
		FileInfo fi = new FileInfo();
		File infile = new File(filename);
		byte[] buffer = new byte[(int)infile.length()];
		try {
			InputStream is = new FileInputStream(filename);
			int count = is.read(buffer);
			is.close();
			if (count != buffer.length) {
				fi.type = 'n';
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fi.type = 'n';
		} catch (IOException e) {
			e.printStackTrace();
			fi.type = 'n';
		}
		if (fi.type == 'n') {
			return fi;
		}

		if ((char) buffer[0] == 'P' && (char) buffer[1] == 'K'
				&& buffer[2] == (byte) 0x03 && buffer[3] == (byte) 0x04) {
			fi.type = 'z';
		} else if ((char) buffer[0] == 'R' && (char) buffer[1] == 'a'
				&& (char) buffer[2] == 'r' && (char) buffer[3] == '!'
				&& buffer[4] == (byte)0x1a && buffer[5] == (byte)0x07 &&
				((buffer[6] == (byte)0x00
				|| (buffer[6] == (byte)0x01 && buffer[7] == (byte)0x00)))) {
			fi.type = 'r';
		} else if (buffer[257] == (byte) 0x75
				&& buffer[258] == (byte) 0x73
				&& buffer[259] == (byte) 0x74
				&& buffer[260] == (byte) 0x61
				&& buffer[261] == (byte) 0x72
				&& ((buffer[262] == (byte) 0x00 && buffer[263] == (byte) 0x30 && buffer[264] == (byte) 0x30) || (buffer[262] == (byte) 0x20
						&& buffer[263] == (byte) 0x20 && buffer[264] == (byte) 0x00))) {
			fi.type = 't';
		} else {
			fi.type = 'x';
		}
		
		fi.size = buffer.length;
		fi.createdOn = infile.lastModified();
		fi.CalculateDigest(buffer);

		return fi;
	}
}
