import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Enumeration;
import java.util.zip.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import content.FileInfo;

/**
 * 
 */

/**
 * @author john
 * 
 */
/**
 * @author john
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

	/**
	 * @throws HeadlessException
	 */
	public ComicScanner() throws HeadlessException {
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == buttonChoose) {
			retrieveFile();
		} else if (e.getSource() == buttonCheck) {
			unpackArchive();
		} else if (e.getSource() == buttonSend) {
			pageReport();
		}

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

	// Called when this applet is loaded into the browser.
	@Override
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
		buttonSend = new JButton("Report");

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
	private void pageReport() {
		StyledDocument doc = textReport.getStyledDocument();
		Style normal = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
		Style regular = doc.addStyle("regular", normal);
		Style warning = doc.addStyle("highlight", regular);
		StyleConstants.setForeground(warning, Color.orange);
		StyleConstants.setBold(warning, true);

		for (int i = 0; i < FileInfo.fileData.size(); i++) {
			FileInfo fi = FileInfo.fileData.get(i);
			try {
				doc.insertString(doc.getLength(), fi.warnDuplicate(), warning);
				doc.insertString(doc.getLength(), fi.warnMac(), warning);
				doc.insertString(doc.getLength(), fi.warnOdd(), warning);
				doc.insertString(doc.getLength(), fi.report(), normal);
			} catch (BadLocationException e1) {
				// Ignore and continue
			}
		}
		try {
			int warnings = FileInfo.nWarnings();
			doc.insertString(doc.getLength(), "\n" + warnings
					+ " issue" + (warnings == 1 ? "" : "s")
					+ " found.", normal);
		} catch (BadLocationException e1) {
			// Ignore and continue
		}
	}

	/**
	 * @param in
	 * @param fileSize
	 * @return
	 */
	private byte[] readFromStream(InputStream in, long fileSize) {
		byte[] buffer = new byte[(int) fileSize];
		int offset = 0, len = 0;
		try {
			while (offset >= 0 && len < fileSize) {
				len += offset;
				offset = in.read(buffer, offset, in.available());
			}
			in.close();
		} catch (IOException e) {
			return new byte[0];
		}
		return buffer;
	}

	/**
	 * 
	 */
	private void retrieveFile() {
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
	private void unpackArchive() {
		textReport.setText("");
		FileInfo.resetTracking();
		File infile = new File(pathname);
		byte[] buffer;
		try {
			InputStream is = new FileInputStream(pathname);
			buffer = readFromStream(is, (int) infile.length());
		} catch (FileNotFoundException err) {
			buffer = new byte[0];
		}
		FileInfo archInfo = new FileInfo(buffer, FileInfo.archive);
		archInfo.name = filename;
		archInfo.createdOn = infile.lastModified();
		switch (archInfo.type) {
		case "zip":
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
					long fileSize = entry.getSize();

					try {
						InputStream in = zipFile.getInputStream(entry);
						buffer = readFromStream(in, fileSize);
						FileInfo info = new FileInfo(buffer, FileInfo.file);
						info.name = entry.getName();
						info.createdOn = entry.getTime();
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
