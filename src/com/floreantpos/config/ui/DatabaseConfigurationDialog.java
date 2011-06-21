package com.floreantpos.config.ui;

import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import com.floreantpos.POSConstants;
import com.floreantpos.config.ApplicationConfig;
import com.floreantpos.ui.TitlePanel;
import com.floreantpos.ui.dialog.POSDialog;
import com.floreantpos.ui.dialog.POSMessageDialog;

public class DatabaseConfigurationDialog extends POSDialog implements ActionListener {
	
	private static final String CLOSE = "close";
	private static final String SAVE = "save";
	private static final String TEST = "test";
	private JTextField tfDatabaseDriver;
	private JTextField tfDatabaseURL;
	private JTextField tfDatabaseName;
	private JTextField tfUserName;
	private JPasswordField tfPassword;
	private JButton btnTestConnection;
	private JButton btnFinish;
	private JButton btnExit;
	
	private TitlePanel titlePanel;
	
	private boolean exitOnClose;

	public DatabaseConfigurationDialog() throws HeadlessException {
		super();
	}

	public DatabaseConfigurationDialog(Dialog owner, boolean modal) {
		super(owner, modal);
	}

	public DatabaseConfigurationDialog(Dialog owner, String title, boolean modal) {
		super(owner, title, modal);
	}

	public DatabaseConfigurationDialog(Frame owner, boolean modal, boolean unDecorated) throws HeadlessException {
		super(owner, modal, unDecorated);
	}

	public DatabaseConfigurationDialog(Frame owner, boolean modal) throws HeadlessException {
		super(owner, modal);
	}
	
	protected void initUI() {
		setLayout(new MigLayout("fill","[][fill, grow]",""));
	
		titlePanel = new TitlePanel();
		tfDatabaseDriver = new JTextField();
		tfDatabaseURL = new JTextField();
		tfDatabaseName = new JTextField();
		tfUserName = new JTextField();
		tfPassword = new JPasswordField();

		add(titlePanel, "span, grow, wrap");
		
		add(new JLabel("Database Driver" + ":"));
		add(tfDatabaseDriver, "grow, wrap");
		add(new JLabel("Database URL" + ":"));
		add(tfDatabaseURL, "grow, wrap");
		add(new JLabel("Database Name" + ":"));
		add(tfDatabaseName, "grow, wrap");
		add(new JLabel("User Name" + ":"));
		add(tfUserName, "grow, wrap");
		add(new JLabel("Database Password" + ":"));
		add(tfPassword, "grow, wrap");
		add(new JSeparator(),"span, grow, gaptop 10");
		
		btnTestConnection = new JButton("Test Connection");
		btnTestConnection.setActionCommand(TEST);
		btnFinish = new JButton(POSConstants.SAVE);
		btnFinish.setActionCommand(SAVE);
		btnExit = new JButton(POSConstants.CLOSE);
		btnExit.setActionCommand(CLOSE);
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(btnTestConnection);
		buttonPanel.add(btnFinish);
		buttonPanel.add(btnExit);
		
		add(buttonPanel, "span, grow");
		
		btnTestConnection.addActionListener(this);
		btnFinish.addActionListener(this);
		btnExit.addActionListener(this);
		
		tfDatabaseDriver.setText(ApplicationConfig.getDatabaseDriver());
		tfDatabaseURL.setText(ApplicationConfig.getDatabaseURL());
		tfDatabaseName.setText(ApplicationConfig.getDatabaseName());
		tfUserName.setText(ApplicationConfig.getDatabaseUser());
		tfPassword.setText(ApplicationConfig.getDatabasePassword());
	}
	
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		
		String databaseDriver = tfDatabaseDriver.getText();
		String databaseURL = tfDatabaseURL.getText();
		String databaseName = tfDatabaseName.getText();
		String user = tfUserName.getText();
		String pass = new String(tfPassword.getPassword());
		
		if(TEST.equalsIgnoreCase(command)) {
			if(ApplicationConfig.checkDatabaseConnection(databaseDriver, databaseURL, databaseName, user, pass)) {
				JOptionPane.showMessageDialog(this, "Connection Successfull!");
			}
			else {
				JOptionPane.showMessageDialog(this, "Connection Failed!");
			}
		}
		else if(SAVE.equalsIgnoreCase(command)) {
			if(ApplicationConfig.checkDatabaseConnection(databaseDriver, databaseURL, databaseName, user, pass)) {
				ApplicationConfig.setDatabaseDriver(databaseDriver);
				ApplicationConfig.setDatabaseURL(databaseURL);
				ApplicationConfig.setDatabaseName(databaseName);
				ApplicationConfig.setDatabaseUser(user);
				ApplicationConfig.setDatabasePassword(pass);
				
				ApplicationConfig.serialize();
				
				dispose();
			}
			else {
				JOptionPane.showMessageDialog(this, "Connection Failed!");
			}
		}
		else if(CLOSE.equalsIgnoreCase(command)) {
			if(exitOnClose) {
				POSMessageDialog.showError("Database connection error, application will now exit.");
				System.exit(1);
			}
			else {
				dispose();
			}
		}
	}

	public boolean isExitOnClose() {
		return exitOnClose;
	}

	public void setExitOnClose(boolean exitOnClose) {
		this.exitOnClose = exitOnClose;
	}
	
	public void setTitle(String title) {
		super.setTitle("Configure database");
		
		titlePanel.setTitle(title);
	}
}
