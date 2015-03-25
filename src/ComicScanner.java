import java.awt.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * 
 */

/**
 * @author john
 * 
 */
public class ComicScanner extends JApplet {
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
}

	/**
	 * 
	 */
    public void addControlToContainer(Container pane, int x, int y, JComponent control, boolean tall) {
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

	private void RetrieveFile() {
	    JFileChooser chooser = new JFileChooser();
	    FileNameExtensionFilter filter = new FileNameExtensionFilter(
	        "JPG & GIF Images", "jpg", "gif");
	    chooser.setFileFilter(filter);
	    int returnVal = chooser.showOpenDialog(this);
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
	       System.out.println("You chose to open this file: " +
	            chooser.getSelectedFile().getName());
	    }
	}
}
