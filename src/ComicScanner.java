import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.MaskFormatter;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import content.FileInfo;
import content.RestClient;
import de.innosystec.unrar.Archive;
import de.innosystec.unrar.exception.RarException;
import de.innosystec.unrar.rarfile.FileHeader;

/**
 * @author john
 * 
 */
public class ComicScanner extends JApplet implements ActionListener,
		DocumentListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	JPanel panelMaster;
	JLabel lblApiKey, lblRootUrl;
	JList<String> listPublisher, listSeries;
	JScrollPane scrollPublisher, scrollSeries, scrollReport;
	JTextField textNumber, textUsername, textChoose, textCheck, textSend,
			textXmit, textRootUrl;
	JFormattedTextField textApiKey;
	JPasswordField textPassword;
	JFileChooser chooseComic;
	JButton buttonChoose, buttonCheck, buttonSend, buttonXmit, buttonAbort;
	JTextPane textReport;
	Container cPane;
	MaskFormatter maskHash;

	DefaultListModel<String> listModel;

	String pathname, filename;

	boolean abort_process = false;

	/**
	 * @throws HeadlessException
	 */
	public ComicScanner() throws HeadlessException {
	}

	/**
	 * 
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		Thread queryThread = null;
		if (e.getSource() == buttonChoose) {
			queryThread = new Thread() {
				public void run() {
					retrieveFile();
				}
			};
		} else if (e.getSource() == buttonCheck) {
			queryThread = new Thread() {
				public void run() {
					unpackArchive();
				}
			};
		} else if (e.getSource() == buttonSend) {
			queryThread = new Thread() {
				public void run() {
					pageReport();
				}
			};
		} else if (e.getSource() == buttonXmit) {
			queryThread = new Thread() {
				public void run() {
					updateProgress(false);
					int files = FileInfo.fileData.size();
					int count = 0;
					Iterator<FileInfo> iter = FileInfo.fileData.iterator();
					while (FileInfo.fileData != null && iter.hasNext()) {
						if (abort_process) {
							abort_process = false;
							break;
						}
						FileInfo fi = iter.next();
						if (!fi.sendSubmission()) {
							updateProgress(textXmit, "Server is not available");
							break;
						}
						updateProgress(textXmit, "" + count + " files of "
								+ files + ".");
						count += 1;
					}
					updateProgress(true);
				}
			};
		} else if (e.getSource() == buttonAbort) {
			abort_process = true;
		}
		if (queryThread != null) {
			queryThread.start();
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.
	 * DocumentEvent)
	 */
	@Override
	public void changedUpdate(DocumentEvent arg0) {
		commonTextHandler(arg0);
	}

	/**
	 * 
	 */
	private void commonTextHandler(DocumentEvent arg0) {
		if (arg0.getDocument() == textApiKey.getDocument()) {
			String apikey = textApiKey.getText().trim();
			CharSequence spaces = " ";
			Color bg, fg;
			if (apikey.length() == 64 && !apikey.contains(spaces)) {
				FileInfo.apikey = apikey;
				bg = new Color(0, 127, 0);
				fg = new Color(255, 255, 255);
			} else {
				bg = new Color(255, 233, 233);
				fg = new Color(0, 0, 0);
			}
			textApiKey.setBackground(bg);
			textApiKey.setForeground(fg);
		} else if (arg0.getDocument() == textRootUrl.getDocument()) {
			Color bg, fg;
			String rootUrl = textRootUrl.getText().trim();
			try {
				URL root = new URL(rootUrl);
				URI uri = root.toURI();
				if (!RestClient.isUrlValid(rootUrl)) {
					throw new MalformedURLException();
				}
				bg = new Color(0, 127, 0);
				fg = new Color(255, 255, 255);
				FileInfo.defaultBaseUrl = rootUrl;
			} catch (MalformedURLException | URISyntaxException e) {
				bg = new Color(255, 233, 233);
				fg = new Color(0, 0, 0);
			}
			if (rootUrl.startsWith("]") && rootUrl.endsWith("[")) {
				// Override validation, in case of testing
				rootUrl = rootUrl.substring(1, rootUrl.length() - 1);
				FileInfo.defaultBaseUrl = rootUrl;
			}
			textRootUrl.setBackground(bg);
			textRootUrl.setForeground(fg);
		} else {
		}
	}

	/**
	 * @param infile
	 */
	private void extractRar(File infile) {
		byte[] buffer;
		Archive rarFile = null;
		List<FileHeader> headers = null;
		try {
			rarFile = new Archive(infile, null, false);
			headers = rarFile.getFileHeaders();
			int files = headers.size();
			int count = 0;
			FileInfo.networkFailed = false;
			Iterator<FileHeader> iter = headers.iterator();
			while (headers != null && iter.hasNext()) {
				if (abort_process) {
					abort_process = false;
					break;
				}
				FileHeader head = iter.next();
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				rarFile.extractFile(head, os);
				buffer = os.toByteArray();
				count += 1;
				String status = "" + count + " archived files of " + files
						+ ".";
				updateProgress(textCheck, status);
				FileInfo info = new FileInfo(buffer, FileInfo.file);
				info.name = head.getFileNameString();
				info.createdOn = head.getMTime().getTime();
				info.folder = head.isDirectory();
			}
			rarFile.close();
		} catch (RarException | IOException e) {
		}
	}

	/**
	 * 
	 */
	private void extractZip() {
		byte[] buffer;
		ZipFile zipFile = null;
		Enumeration<?> entries;

		try {
			zipFile = new ZipFile(pathname);
			entries = zipFile.entries();
			int count = 0;
			int files = zipFile.size();
			while (entries.hasMoreElements()) {
				if (abort_process) {
					abort_process = false;
					break;
				}
				ZipEntry entry = (ZipEntry) entries.nextElement();
				long fileSize = entry.getSize();
				InputStream in = zipFile.getInputStream(entry);
				buffer = readFromStream(in, fileSize);
				count += 1;
				String status = "" + count + " archived files of " + files
						+ ".";
				updateProgress(textCheck, status);
				FileInfo info = new FileInfo(buffer, FileInfo.file);
				info.name = entry.getName();
				info.createdOn = entry.getTime();
				info.folder = entry.isDirectory();
			}
			zipFile.close();
		} catch (IOException e1) {
		}
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
		buttonXmit = new JButton("Transmit");
		buttonAbort = new JButton("Abort Process");
		buttonAbort.setEnabled(false);

		textChoose = new JTextField(128);
		textCheck = new JTextField(128);
		textSend = new JTextField(128);
		textXmit = new JTextField(128);
		textChoose.setEditable(false);
		textCheck.setEditable(false);
		textSend.setEditable(false);
		textXmit.setEditable(false);

		try {
			maskHash = new MaskFormatter(
					"HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH");
		} catch (ParseException e) {
		}
		lblApiKey = new JLabel("Your API Key");
		textApiKey = new JFormattedTextField(maskHash);

		lblRootUrl = new JLabel("Your ScanData Server");
		textRootUrl = new JTextField("http://localhost:3000");

		// listModel.addElement("Ace");
		// listModel.addElement("Ajax-Farrell");
		// listModel.addElement("American Comics Group");
		// listModel.addElement("Avon");
		// listModel.addElement("Better/Nedor/Standard/Pines");
		// listModel.addElement("Centaur");
		// listModel.addElement("Charlton");
		// listModel.addElement("Chesler");
		// listModel.addElement("Columbia");
		// listModel.addElement("Dell");
		listPublisher.setModel(listModel);

		cPane = getContentPane();
		cPane.setLayout(new GridBagLayout());

		// addControlToContainer(cPane, 0, 0, scrollPublisher, true, 0);
		// addControlToContainer(cPane, 1, 0, scrollSeries, true, 0);
		// addControlToContainer(cPane, 2, 0, textNumber, false, 0);
		// addControlToContainer(cPane, 0, 1, textUsername, false, 0);
		// addControlToContainer(cPane, 1, 1, textPassword, false, 0);
		addControlToContainer(cPane, 0, 0, lblApiKey, false, 0);
		addControlToContainer(cPane, 0, 1, buttonChoose, false, 0);
		addControlToContainer(cPane, 0, 2, buttonCheck, false, 0);
		addControlToContainer(cPane, 0, 3, buttonSend, false, 0);
		addControlToContainer(cPane, 0, 4, buttonXmit, false, 0);
		addControlToContainer(cPane, 0, 5, lblRootUrl, false, 0);
		addControlToContainer(cPane, 1, 0, textApiKey, false, 0);
		addControlToContainer(cPane, 1, 1, textChoose, false, 0);
		addControlToContainer(cPane, 1, 2, textCheck, false, 0);
		addControlToContainer(cPane, 1, 3, textSend, false, 0);
		addControlToContainer(cPane, 1, 4, textXmit, false, 0);
		addControlToContainer(cPane, 1, 5, textRootUrl, false, 0);
		addControlToContainer(cPane, 0, 6, scrollReport, true, 1);
		Component strut = Box.createHorizontalStrut(125);
		addControlToContainer(cPane, 1, 7, (JComponent) strut, false, 0);
		addControlToContainer(cPane, 0, 8, buttonAbort, false, 1);

		buttonChoose.addActionListener(this);
		buttonCheck.addActionListener(this);
		buttonSend.addActionListener(this);
		buttonXmit.addActionListener(this);
		buttonAbort.addActionListener(this);
		textApiKey.getDocument().addDocumentListener(this);
		textRootUrl.getDocument().addDocumentListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.
	 * DocumentEvent)
	 */
	@Override
	public void insertUpdate(DocumentEvent arg0) {
		commonTextHandler(arg0);
	}

	/**
	 * 
	 */
	private void pageReport() {
		updateProgress(false);
		updateProgress(null, TextFormat.Normal, true);
		int pages = FileInfo.fileData.size();
		for (int i = 0; i < pages; i++) {
			if (abort_process) {
				abort_process = false;
				break;
			}
			FileInfo fi = FileInfo.fileData.get(i);
			updateProgress(textSend, "" + (i + 1) + " files of " + pages + ".");
			updateProgress(fi.warnDuplicate(), TextFormat.Warning, false);
			updateProgress(fi.warnMac(), TextFormat.Warning, false);
			updateProgress(fi.warnOdd(), TextFormat.Warning, false);
			updateProgress(fi.warnFolder(), TextFormat.Warning, false);
			updateProgress(fi.warnWidth(), TextFormat.Warning, false);
			updateProgress(fi.warnHeight(), TextFormat.Warning, false);
			updateProgress(fi.warnSize(), TextFormat.Warning, false);
			updateProgress(fi.warnOrder(), TextFormat.Warning, false);
			updateProgress(fi.report(), TextFormat.Normal, false);
		}
		int warnings = FileInfo.nWarnings();
		String summary = "\n" + warnings + " issue"
				+ (warnings == 1 ? "" : "s") + " found.";
		updateProgress(summary, TextFormat.Normal, false);
		updateProgress(true);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.
	 * DocumentEvent)
	 */
	@Override
	public void removeUpdate(DocumentEvent arg0) {
		commonTextHandler(arg0);
	}

	/**
	 * 
	 */
	private void retrieveFile() {
		updateProgress(false);
		filename = null;
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
			}
		}
		if (filename != null) {
			updateProgress(textChoose, filename);
		}
		updateProgress(true);
	}

	/**
	 * 
	 */
	private void unpackArchive() {
		updateProgress(false);
		updateProgress(textCheck, "Preparing archive...");
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
		case "rar":
			extractRar(infile);
			break;
		case "zip":
			extractZip();
			break;
		}
		FileInfo.sortFiles();
		updateProgress(true);
	}

	/**
	 * @param active
	 */
	public void updateProgress(final boolean active) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				buttonChoose.setEnabled(active);
				buttonCheck.setEnabled(active);
				buttonSend.setEnabled(active);
				buttonXmit.setEnabled(active);
				buttonAbort.setEnabled(!active);
			}
		});
	}

	/**
	 * @param control
	 * @param info
	 */
	public void updateProgress(final JTextComponent control, final String info) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				control.setText(info);
			}
		});
	}

	/**
	 * @param info
	 * @param reset
	 */
	public void updateProgress(final String info, final TextFormat fmt,
			final boolean reset) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (reset) {
					textReport.setText("");
					return;
				}
				StyledDocument doc = textReport.getStyledDocument();
				Style normal = StyleContext.getDefaultStyleContext().getStyle(
						StyleContext.DEFAULT_STYLE);
				Style regular = doc.addStyle("regular", normal);
				Style warning = doc.addStyle("highlight", regular);
				Style style = regular;
				switch (fmt) {
				case Warning:
					style = warning;
					break;
				case Normal:
					break;
				default:
					break;
				}
				StyleConstants.setForeground(warning, Color.orange);
				StyleConstants.setBold(warning, true);
				try {
					doc.insertString(doc.getLength(), info, style);
				} catch (BadLocationException e) {
				}
			}
		});
	}
}
