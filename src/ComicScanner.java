import java.awt.*;
import java.awt.event.*;
import java.io.*;

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
			filetype = ArchiveType(filename);
		}

	}
	
	/**
	 * Returns the sort of archive.
	 * r => RAR file
	 * t => TAR file
	 * z => ZIP file
	 * x => Unknown
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

		if ((char)buffer[0] == 'P' && (char)buffer[1] == 'K') {
			System.out.println("Zip file!");
			return 'z';
		} else if ((char)buffer[0] == 'R' && (char)buffer[1] == 'a'
				&& (char)buffer[2] == 'r' && (char)buffer[3] == '!'
				&& buffer[4] == 0x1a) {
			System.out.println("Rar file!");
			return 'r';
		} else if (buffer[257] == 0x75 && buffer[258] == 0x73 && buffer[259] == 0x74
				&& buffer[260] == 0x61 && buffer[261] == 0x72 && (
						(buffer[262] == 0x00 && buffer[263] == 0x30 && buffer[264] == 0x30) ||
						(buffer[262] == 0x20 && buffer[263] == 0x20 && buffer[264] == 0x00)
						)) {
			System.out.println("Tar file!");
			return 't';
		}
		
		return 'x';
	}
}
