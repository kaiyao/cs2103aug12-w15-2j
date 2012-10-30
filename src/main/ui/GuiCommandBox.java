package main.ui;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Rectangle;

import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import java.awt.BorderLayout;
import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import main.LogHandler;
import main.shared.LogicToUi;
import javax.swing.JPopupMenu;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Logger;

public class GuiCommandBox extends UI{
	
	Logger log = LogHandler.getLogInstance();

	private JFrame dummyFrame;
	protected JTextField txtCmd;
	protected JEditorPane txtStatus;
	protected JPopupMenu popupCmdHint;
	protected JEditorPane txtCmdHint;
	
	protected List<String> commandHistory;
	protected ListIterator<String> commandHistoryIterator;
	
	private enum HintPosEnum {ABOVE, BELOW, UNDEFINED};
	private HintPosEnum hintPos = HintPosEnum.UNDEFINED;
	private Rectangle previousWindowRect;
	private String hintPreviousCommand = "";

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GuiCommandBox window = new GuiCommandBox();
					window.initialize();
					window.dummyFrame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public GuiCommandBox() {
		try {
			UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	protected void initialize() {
		
		log.entering(this.getClass().getName(), "initialize");
		
		// Window
		dummyFrame = new JFrame();
		dummyFrame.setBounds(100, 100, 450, 300);
		dummyFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// Text box for commands
		txtCmd = new JTextField();
		dummyFrame.getContentPane().add(txtCmd, BorderLayout.NORTH);
				
		// Popup for hints
		popupCmdHint = new JPopupMenu();
		popupCmdHint.setFocusable(false);
		addPopup(txtCmd, popupCmdHint);
		
		// Text box in popup for hints
		txtCmdHint = new JEditorPane();
		txtCmdHint.setFocusTraversalKeysEnabled(false);
		txtCmdHint.setFocusCycleRoot(false);
		txtCmdHint.setFocusable(false);
		popupCmdHint.add(txtCmdHint);
		
        // Text box for status
		txtStatus = new JEditorPane();
				
		dummyFrame.getContentPane().add(txtStatus, BorderLayout.CENTER);
		
		configureWidgets(txtCmd, txtStatus, txtCmdHint, popupCmdHint);
	}
	
	protected void configureWidgets(final JTextField txtCmd, JEditorPane txtStatus, final JEditorPane txtCmdHint, final JPopupMenu popupCmdHint){
		log.entering(this.getClass().getName(), "configureWidgets");
		
		this.txtCmd = txtCmd;
		this.txtStatus = txtStatus;
		this.txtCmdHint = txtCmdHint;
		this.popupCmdHint = popupCmdHint;
		
		txtCmd.setColumns(10);
		
		txtCmdHint.setEditable(false);
		txtCmdHint.setContentType("text/html");
		txtCmdHint.setBackground(new Color(0, 0, 0, 0));
		txtCmdHint.setOpaque(false);		
		
		// Handler for key presses in command box
		commandHistory = new LinkedList<String>();
		commandHistoryIterator = commandHistory.listIterator();		
		txtCmd.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent arg0) {
				log.fine("Command box key recevied: keyCode " + arg0.getKeyCode());
				if (arg0.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
					log.info("Enter key pressed");
					
					commandHistory.add(txtCmd.getText());
					commandHistoryIterator = commandHistory.listIterator(commandHistory.size());
					
					executeCommand(txtCmd.getText());
					popupCmdHint.setVisible(false);
				} else if (arg0.getKeyCode() == java.awt.event.KeyEvent.VK_UP) {
					log.info("Up key pressed");
					
					if (commandHistoryIterator.hasPrevious()) {
						txtCmd.setText(commandHistoryIterator.previous());
						showHint();
					}
				} else if (arg0.getKeyCode() == java.awt.event.KeyEvent.VK_DOWN) {
					log.info("Down key pressed");
					
					if (commandHistoryIterator.hasNext()) {
						txtCmd.setText(commandHistoryIterator.next());
						showHint();
					}
				} else {					
					showHint();
				}
			}

			private void showHint() {
				log.entering(this.getClass().getName(), "showHint");
				
				JFrame frame = (JFrame) SwingUtilities.getRoot(txtCmd);
				Rectangle currentWindowRect = new Rectangle(frame.getLocationOnScreen(), frame.getSize());
				log.finer("Checking window position");
				if(!currentWindowRect.equals(previousWindowRect)){
					log.finer("Window position has changed, setting hint box position to undefined");
					hintPos = HintPosEnum.UNDEFINED;
					previousWindowRect = currentWindowRect;
				}
				
				boolean isCommand = false;
				for (String command : commandList){
					log.finest("Checking if command in command box is " + command);
					if (txtCmd.getText().startsWith(command)) {
						log.finer("Command in command box matches " + command);
						if(!hintPreviousCommand.equals(command)){
							log.finer("Command has changed, reset hint position to undefined");
							hintPos = HintPosEnum.UNDEFINED;
							hintPreviousCommand = command;
						}						
						
						log.fine("Getting hint text for command " + command);
						log.finest(Hint.getInstance().helpForThisCommandHTML(command));
						String hintText = "<html>"+Hint.getInstance().helpForThisCommandHTML(command)+"</html>";
						log.finest("Setting hint text to " + hintText);
						txtCmdHint.setText(hintText);

						log.fine("Hint box size is " +popupCmdHint.getSize()); 
						
						popupCmdHint.setPopupSize(500, (int) txtCmdHint.getPreferredSize().getHeight() + 20);
						log.fine("Setting hint box size to " +popupCmdHint.getSize()); 
						if(hintPos == HintPosEnum.BELOW){
							log.finer("Hint box is to be shown below");
							popupCmdHint.show(txtCmd, 5, txtCmd.getHeight());
						}else if(hintPos == HintPosEnum.ABOVE){
							log.finer("Hint box is to be shown above");
							popupCmdHint.show(txtCmd, 5, -1 * popupCmdHint.getHeight());
						}else{ // undefined
							log.finer("Hint box position to be measured");
							log.finer("Attempting to show hint box below");
							popupCmdHint.show(txtCmd, 5, txtCmd.getHeight());
							hintPos = HintPosEnum.BELOW;
							log.finer("Checking hint box is really below or not");
							if(popupCmdHint.getLocationOnScreen().getY() < txtCmd.getLocationOnScreen().getY() + txtCmd.getSize().getHeight()){
								log.finer("Hint box cannot fit below, switching to above text box");
								popupCmdHint.setVisible(false);
								popupCmdHint.show(txtCmd, 5, -1 * popupCmdHint.getHeight());
								hintPos = HintPosEnum.ABOVE;
							}
						}
						txtCmd.requestFocus();
						isCommand = true;
					} 
				}
				if(isCommand == false){
					log.finer("Text box does not start with valid command, hide hint box");
					popupCmdHint.setVisible(false);
				}
			}
		});
		
		// Use HTMLEditorKit to style the HTML
		HTMLEditorKit kit = new HTMLEditorKit();
		txtCmdHint.setEditorKit(kit);

		StyleSheet hintStyleSheet = kit.getStyleSheet();
		hintStyleSheet.addRule("body, p {font-family:Segoe UI;}");
		hintStyleSheet
				.addRule("h1 {font-family:Segoe UI; margin:0px 0px 0px 0px; padding:0px 0px 0px 0px;}");
		hintStyleSheet
				.addRule("h2 {font-family:Segoe UI; margin:10px 0px 0px 0px; padding:0px 0px 0px 0px;}");
		hintStyleSheet.addRule("p {margin-top:5px;}");

		Document doc = kit.createDefaultDocument();
		log.finest("Setting txtCmdHint with hintStyleSheet");
		txtCmdHint.setDocument(doc);
		
		txtStatus.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent event) {
				log.finer("Hyperlink in hint box received: " + event);
				if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					log.finer("Hyperlink event is of type activated");
					if (event.getURL().toString().endsWith("/undo")) {
						log.finer("Hyperlink is undo command");
						executeCommand("undo");
					}
				}
			}
		});
		txtStatus.setBackground(new Color(0, 0, 0, 0));
		txtStatus.setOpaque(false);
		txtStatus.setEditable(false);
		txtStatus.setContentType("text/html");
		
		log.exiting(this.getClass().getName(), "configureWidgets");
	}
	
	public void executeCommand(String text) {
		
		log.entering(this.getClass().getName(), "executeCommmand");
		
		// Call command parser
		log.info("Sending the following command to logic: " + text);
		LogicToUi returnValue = sendCommandToLogic(text);

		// Set command text box to empty
		log.finer("Set text box to empty after sending command to logic");
		txtCmd.setText("");
		
		update(returnValue);
		
		log.exiting(this.getClass().getName(), "executeCommmand");
	}

	protected void showStatus(String status) {
		txtStatus
				.setText("<html><table align=\"center\"><tr><td valign=\"middle\" align=\"center\"><font size=\"4\">"
						+ HTMLEncoder.encode(status)
						+ " &nbsp;&nbsp;&nbsp;<a href=\"http://doit/undo\">undo</a></font></td></tr></table></html>");
	}
	
	public void update(LogicToUi returnValue){
		log.entering(this.getClass().getName(), "update(LogicToUi returnValue)");
		log.info("Setting status message to " + returnValue.getString());
		showStatus(returnValue.getString());
		log.exiting(this.getClass().getName(), "update(LogicToUi returnValue)");
	}

	@Override
	public void runUI() {
		log.entering(this.getClass().getName(), "runUI");
		initialize();
		log.exiting(this.getClass().getName(), "runUI");
	}

	protected static void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			private void showMenu(MouseEvent e) {
				//popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}
}
