import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

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
	JScrollPane scrollPublisher, scrollSeries;
	JTextField textNumber, textUsername;
	JPasswordField textPassword;
	JFileChooser chooseComic;
	JButton buttonChoose, buttonCheck, buttonSend;
	Container cPane;

	DefaultListModel<String> listModel;

	String filename;
	char filetype = 'x';
	ArrayList<String> compressedFiles;

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

		textNumber = new JTextField("0", 4);
		textUsername = new JTextField(32);
		textPassword = new JPasswordField(32);
		buttonChoose = new JButton("Choose Comic...");
		buttonCheck = new JButton("Analyze");
		buttonSend = new JButton("Send");

		listModel.addElement("Ace");
		listModel.addElement("Ajax-Farrell");
		listModel.addElement("American Comics Group");
		listModel.addElement("Avon");
		listModel.addElement("Better/Nedor/Standard/Pines");
		listModel.addElement("Centaur");
		listModel.addElement("Charlton");
		listModel.addElement("Chesler");
		listModel.addElement("Columbia");
		listModel.addElement("Dell");
		listPublisher.setModel(listModel);

		cPane = getContentPane();
		cPane.setLayout(new GridBagLayout());

		addControlToContainer(cPane, 0, 0, scrollPublisher, true);
		addControlToContainer(cPane, 1, 0, scrollSeries, true);
		addControlToContainer(cPane, 2, 0, textNumber, false);
		addControlToContainer(cPane, 0, 1, textUsername, false);
		addControlToContainer(cPane, 1, 1, textPassword, false);
		addControlToContainer(cPane, 0, 2, buttonChoose, false);
		addControlToContainer(cPane, 0, 3, buttonCheck, false);
		addControlToContainer(cPane, 1, 3, buttonSend, false);

		buttonChoose.addActionListener(this);
		buttonCheck.addActionListener(this);
		buttonSend.addActionListener(this);
	}

	/**
	 * 
	 */
	public void addControlToContainer(Container pane, int x, int y,
			JComponent control, boolean tall) {
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
		pane.add(control, gbc);
	}

	/**
	 * @throws HeadlessException
	 */
	public ComicScanner() throws HeadlessException {
		// TODO Auto-generated constructor stub
		compressedFiles = new ArrayList<String>();
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
				filename = chooser.getSelectedFile().getCanonicalPath();
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
			filetype = ArchiveType(filename);
			switch (filetype) {
			case 'z':
				ZipFile zipFile = null;
				Enumeration<?> entries;

				try {
					zipFile = new ZipFile(filename);
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
							char type;
							InputStream in;
							in = zipFile.getInputStream(entry);
							len = in.read(buffer);
							type = ImageType(buffer);
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
	private char ImageType(byte[] image) {
		if (image[0] == (byte) 0x47 && image[1] == (byte) 0x49
				&& image[2] == (byte) 0x46 && image[3] == (byte) 0x38
				&& (image[4] == (byte) 0x37 || image[4] == (byte) 0x39)
				&& image[5] == (byte) 0x61) {
			return 'g';
		} else if (image[0] == (byte) 0x49 && image[1] == (byte) 0x49
				&& image[2] == (byte) 0x2A && image[3] == (byte) 0x00) {
			return 't';
		} else if (image[0] == (byte) 0x4D && image[1] == (byte) 0x4D
				&& image[2] == (byte) 0x00 && image[3] == (byte) 0x2A) {
			return 't';
		} else if (image[0] == (byte) 0xFF && image[1] == (byte) 0xD8
				&& image[2] == (byte) 0xFF && image[3] == (byte) 0xE0) {
			return 'j';
		} else if (image[0] == (byte) 0x89 && image[1] == (byte) 0x50
				&& image[2] == (byte) 0x4E && image[3] == (byte) 0x47
				&& image[4] == (byte) 0x0D && image[5] == (byte) 0x0A
				&& image[6] == (byte) 0x1A && image[7] == (byte) 0x0A) {
			return 'p';
		}
		return 'x';
	}

	/**
	 * Returns the sort of archive.
	 *  r => RAR file
	 *  t => TAR file
	 *  z => ZIP file
	 *  x => Unknown
	 */
	private char ArchiveType(String filename) {
		byte[] buffer = new byte[270];
		try {
			InputStream is = new FileInputStream(filename);
			int count = is.read(buffer);
			is.close();
			if (count != buffer.length) {
				return 'x';
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 'x';
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 'x';
		}

		if ((char) buffer[0] == 'P' && (char) buffer[1] == 'K'
				&& buffer[2] == (byte) 0x03 && buffer[3] == (byte) 0x04) {
			return 'z';
		} else if ((char) buffer[0] == 'R' && (char) buffer[1] == 'a'
				&& (char) buffer[2] == 'r' && (char) buffer[3] == '!'
				&& buffer[4] == 0x1a) {
			return 'r';
		} else if (buffer[257] == (byte) 0x75
				&& buffer[258] == (byte) 0x73
				&& buffer[259] == (byte) 0x74
				&& buffer[260] == (byte) 0x61
				&& buffer[261] == (byte) 0x72
				&& ((buffer[262] == (byte) 0x00 && buffer[263] == (byte) 0x30 && buffer[264] == (byte) 0x30) || (buffer[262] == (byte) 0x20
						&& buffer[263] == (byte) 0x20 && buffer[264] == (byte) 0x00))) {
			return 't';
		}

		return 'x';
	}
}
